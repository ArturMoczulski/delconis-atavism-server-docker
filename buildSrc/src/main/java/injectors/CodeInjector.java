package atavism.buildSrc.injectors;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.*;
import atavism.buildSrc.injectors.*;

public class CodeInjector {

  public static void modifyCodeBlock(String filePath, String blockName, String codeBlockRegex, String contentRegex,
      String newContent, String commentSyntax) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String fileContent = String.join("\n", lines);
    String startMarker = commentSyntax + " @GeneratedByAtavismServerDockerStart(\"" + blockName + "\")";
    String endMarker = commentSyntax + " @GeneratedByAtavismServerDockerEnd(\"" + blockName + "\")";
    String originalStartMarker = commentSyntax + " @AtavismServerDockerOriginalCodeBlockStart(\"" + blockName + "\")";
    String originalEndMarker = commentSyntax + " @AtavismServerDockerOriginalCodeBlockEnd(\"" + blockName + "\")";

    Pattern blockPattern = Pattern.compile(Pattern.quote(startMarker) + "(.*?)" + Pattern.quote(endMarker),
        Pattern.DOTALL);
    Matcher blockMatcher = blockPattern.matcher(fileContent);

    if (blockMatcher.find()) {
      // Existing annotated block found, modify it
      String existingBlock = blockMatcher.group(0);
      Pattern originalPattern = Pattern
          .compile(Pattern.quote(originalStartMarker) + "(.*?)" + Pattern.quote(originalEndMarker), Pattern.DOTALL);
      Matcher originalMatcher = originalPattern.matcher(existingBlock);
      if (originalMatcher.find()) {
        String originalContent = originalMatcher.group(1);
        String modifiedActiveCode = originalContent.replaceAll(contentRegex, newContent).replaceFirst(
            commentSyntax + " ",
            "");
        String newBlock = startMarker + "\n" + commentSyntax + " DO NOT EDIT CODE WITHIN THE BLOCK\n"
            + originalMatcher.group(0) + "\n" + modifiedActiveCode + "\n" + endMarker;
        fileContent = blockMatcher.replaceFirst(Matcher.quoteReplacement(newBlock));
      } else {
        throw new RuntimeException("Original code block not found within the annotated block.");
      }
    } else {
      // No annotated block found, look for the target pattern to create a new block
      Pattern targetPattern = Pattern.compile(codeBlockRegex, Pattern.MULTILINE);
      Matcher targetMatcher = targetPattern.matcher(fileContent);
      if (targetMatcher.find()) {
        String matchedCodeBlock = targetMatcher.group();
        String formattedOriginalBlock = formatOriginalCodeBlock(matchedCodeBlock, blockName, commentSyntax);
        String modifiedMatchedCode = matchedCodeBlock.replaceAll(contentRegex, newContent);
        String newBlock = startMarker + "\n" + commentSyntax + " DO NOT EDIT CODE WITHIN THE BLOCK\n"
            + formattedOriginalBlock + "\n" + modifiedMatchedCode + "\n" + endMarker;
        fileContent = targetMatcher.replaceFirst(Matcher.quoteReplacement(newBlock));
      } else {
        throw new RuntimeException("Target code block not found.");
      }
    }

    Files.write(Paths.get(filePath), fileContent.getBytes(), StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

  private static String formatOriginalCodeBlock(String codeBlock, String blockName, String commentSyntax) {
    String[] lines = codeBlock.split("\n");
    StringBuilder builder = new StringBuilder();
    builder.append(commentSyntax + " @AtavismServerDockerOriginalCodeBlockStart(\"" + blockName + "\")");
    for (String line : lines) {
      builder.append("\n" + commentSyntax + " " + line);
    }
    builder.append("\n" + commentSyntax + " @AtavismServerDockerOriginalCodeBlockEnd(\"" + blockName + "\")");
    return builder.toString();
  }

  public static void injectCodeBlockAtTheTop(String filePath, String blockName, String newContent,
      String commentSyntax) {
    injectCodeBlock(filePath, blockName, newContent, commentSyntax, 1);
  }

  public static void injectCodeBlockAtTheEnd(String filePath, String blockName, String newContent,
      String commentSyntax) {
    try {

      List<String> lines = new ArrayList<>();
      if (Files.exists(Paths.get(filePath))) {
        lines = Files.readAllLines(Paths.get(filePath));
      }
      injectCodeBlock(filePath, blockName, newContent, commentSyntax, lines.size() + 1);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void injectCodeBlockAfterRegex(String filePath, String blockName, String newContent,
      String commentSyntax, String regex) throws RegexNotFound, IOException {

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String content = String.join("\n", lines); // Combine lines into a single String

    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
    Matcher matcher = pattern.matcher(content);

    int lineToInject = -1;

    if (matcher.find()) {
      int end = matcher.end(); // Get the index of the end of the regex match
      lineToInject = content.substring(0, end).split("\n").length; // Count lines up to the end of the match
    }

    if (lineToInject != -1) {
      injectCodeBlock(filePath, blockName, newContent, commentSyntax, lineToInject);
    } else {
      throw new RegexNotFound(regex, filePath);
    }
  }

  public static void injectCodeBlockBeforeRegex(String filePath, String blockName, String newContent,
      String commentSyntax, String regex) throws RegexNotFound, IOException {

    List<String> lines = new ArrayList<>();
    lines = Files.readAllLines(Paths.get(filePath));

    int lineToInject = findLineBeforeRegex(lines, regex);

    if (lineToInject != -1) {
      injectCodeBlock(filePath, blockName, newContent, commentSyntax, lineToInject);
    } else {
      throw new RegexNotFound(regex, filePath);
    }
  }

  public static void injectCodeBlock(String filePath, String blockName, String newContent, String commentSyntax,
      int lineNumber) {
    // Define start and end markers with the block name using the provided comment
    // syntax
    String startMarkerSimple = String.format("%s @GeneratedByAtavismServerDockerStart(\"%s\")", commentSyntax,
        blockName);
    String startMarkerFull = startMarkerSimple + "\n" + commentSyntax + " DO NOT EDIT CODE WITHIN THE BLOCK";
    String endMarker = String.format("%s @GeneratedByAtavismServerDockerEnd(\"%s\")", commentSyntax, blockName);

    List<String> lines = new ArrayList<>();
    Path path = Paths.get(filePath);
    try {
      // Check if the file exists and read from it; otherwise, start with an empty
      // list
      if (Files.exists(path)) {
        lines = Files.readAllLines(path);
      }

      int startIndex = -1;
      int endIndex = -1;

      // Scan for the start and end index of the block
      for (int i = 0; i < lines.size(); i++) {
        if (lines.get(i).trim().equals(startMarkerSimple)) {
          startIndex = i;
        }
        if (lines.get(i).trim().equals(endMarker) && startIndex != -1) {
          endIndex = i;
          break;
        }
      }

      // Construct the new content block with the full start marker
      String fullBlock = startMarkerFull + "\n" + newContent + "\n" + endMarker;

      if (startIndex != -1 && endIndex != -1) {
        // Replace the existing block
        List<String> newLines = new ArrayList<>(lines.subList(0, startIndex));
        newLines.add(fullBlock); // Adds the entire new block including start, content, and end markers
        newLines.addAll(lines.subList(endIndex + 1, lines.size()));
        lines = newLines;
      } else {
        // Append the new block at the specified line number, adjusting for 0-based
        // index
        int insertIndex = lineNumber > 0 ? lineNumber - 1 : 0;
        // Ensure insert index does not exceed file size
        insertIndex = Math.min(insertIndex, lines.size());
        lines.add(insertIndex, fullBlock);
      }

      // Write the modified content back to the file
      Files.write(Paths.get(filePath), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static int findLineBeforeRegex(List<String> lines, String regex) {
    // Convert the list of lines into a single string with line breaks
    String content = String.join("\n", lines);

    // Compile the multi-line regex pattern
    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);

    // Create a matcher for the entire content
    Matcher matcher = pattern.matcher(content);

    // Find the first occurrence of the pattern
    if (matcher.find()) {
      // Count the number of lines up to the start of the match
      int pos = matcher.start();
      String upToMatch = content.substring(0, pos);
      return (int) upToMatch.chars().filter(ch -> ch == '\n').count() + 1; // Return the line number
    }

    return -1; // Return -1 if no match is found
  }

}
