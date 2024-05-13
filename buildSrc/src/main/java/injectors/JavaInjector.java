package atavism.buildSrc.injectors;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import atavism.buildSrc.injectors.*;
import java.util.regex.*;

public class JavaInjector {
  public static void injectMessageInitialzers(String filePath, String blockName, String newContent)
      throws IOException, RegexNotFound {
    String regex = "\\s*MessageCatalog\\s+aoMessageCatalog\\s*=\\s*MessageCatalog\\.addMsgCatalog\\(\\s*\"aoMessageCatalog\"\\s*,\\s*1\\s*,\\s*500\\s*\\);\\s*";

    CodeInjector.injectCodeBlockAfterRegex(filePath, blockName, newContent, "//", regex);
  }

  public static void injectBeforeMethod(String filePath, String methodName, String blockName, String newContent)
      throws IOException, FunctionNotFound {

    String regex = buildRegexForMethod("startDomain");

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
}
