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

public class JavaInjector {

  public static void addToAdsMerger(String filePath, String blockName, String newContent)
      throws IOException, CodeBlockNotFound {
    String codeBlockRegex = "AdvertisementFileMerger\\.merge\\(\\s*\"all_in_one\"\\s*,\\s*\"arena\"\\s*,\\s*\"auction\"\\s*,\\s*\"builder\"\\s*,\\s*\"chat\"\\s*,\\s*\"combat\"\\s*,\\s*\"faction\"\\s*,\\s*\"instance\"\\s*,\\s*\"login_manager\"\\s*,\\s*\"mobserver\"\\s*,\\s*\"objmgr\"\\s*,\\s*\"prefab_manager\"\\s*,\\s*\"quest\"\\s*,\\s*\"weather\"\\s*,\\s*\"wmgr\"\\s*\\)\\s*;";

    String contentRegex = "\"wmgr\"";

    try {
      CodeInjector.modifyCodeBlock(filePath, blockName, codeBlockRegex, contentRegex, "\"wmgr\", " + newContent,
          "//");
    } catch (RuntimeException e) { // Assuming RuntimeException is thrown when block is not found
      throw new CodeBlockNotFound(filePath, blockName, codeBlockRegex);
    }
  }

  public static void addToPostScript(String filePath, String blockName, String newContent)
      throws IOException, CodeBlockNotFound {
    String codeBlockRegex = "private\\s+static\\s+void\\s+postScript\\(\\)\\s*\\{\\s*setGlobalProperties\\(\\)\\s*;";

    String contentRegex = "setGlobalProperties\\(\\);";

    try {
      CodeInjector.modifyCodeBlock(filePath, blockName, codeBlockRegex, contentRegex,
          "setGlobalProperties();\n" + newContent, "//");
    } catch (RuntimeException e) { // Assuming RuntimeException is thrown when block is not found
      throw new CodeBlockNotFound(filePath, blockName, codeBlockRegex);
    }
  }

  public static void injectMessageInitialzers(String filePath, String blockName, String newContent)
      throws IOException, FunctionNotFound {
    JavaInjector.injectAtEndOfMethod(filePath, "init", blockName, newContent);
  }

  public static void injectBeforeMethod(String filePath, String methodName, String blockName, String newContent)
      throws IOException, FunctionNotFound {

    String regex = buildRegexForMethod(methodName);

    // Use the CodeInjector to inject the code before the matched line
    try {
      CodeInjector.injectCodeBlockBeforeRegex(filePath, blockName, newContent, "//", regex);
    } catch (RegexNotFound e) {
      throw new FunctionNotFound(methodName, filePath);
    }
  }

  public static String buildRegexForMethod(String methodName) {
    return "\\s*(public|protected|private|static|final|abstract|synchronized)?\\s*" +
        "(static)?\\s*\\w+\\s+" + Pattern.quote(methodName) + "\\s*\\(" +
        "[^)]*" + // Matches anything except the closing parenthesis to handle multi-line
                  // parameters
        "\\)\\s*" +
        "(throws\\s+([\\w\\s,]+))?\\s*\\{";
  }

  public static void injectAtEndOfMethod(String filePath, String methodName, String blockName, String newContent)
      throws IOException, FunctionNotFound {

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    int braceCount = 0;
    boolean inMethod = false;
    boolean inComment = false;
    boolean inString = false;

    Pattern methodPattern = Pattern.compile(buildRegexForMethod(methodName));
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (methodPattern.matcher(line).find()) {
        inMethod = true;
      }
      if (inMethod) {
        for (int j = 0; j < line.length(); j++) {
          char ch = line.charAt(j);

          if (ch == '/' && j + 1 < line.length() && line.charAt(j + 1) == '/') {
            break; // Skip the rest of the line (it's a comment)
          }
          if (ch == '"' && (j == 0 || line.charAt(j - 1) != '\\')) {
            inString = !inString; // Toggle string state
          }
          if (!inString && ch == '/' && j + 1 < line.length() && line.charAt(j + 1) == '*') {
            inComment = true; // Start of block comment
          }
          if (!inString && ch == '*' && j + 1 < line.length() && line.charAt(j + 1) == '/') {
            inComment = false; // End of block comment
            j++; // Skip the '/' character
            continue;
          }
          if (inComment || inString) {
            continue;
          }
          if (ch == '{') {
            braceCount++;
          } else if (ch == '}') {
            braceCount--;
            if (braceCount == 0) {
              CodeInjector.injectCodeBlock(filePath, blockName, newContent + "\n", "//", i + 1);
              return;
            }
          }
        }
      }
    }
    throw new FunctionNotFound(methodName, filePath);
  }
}
