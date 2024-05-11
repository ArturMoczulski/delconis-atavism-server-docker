package atavism.buildSrc.injectors;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class CodeInjector {

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

}
