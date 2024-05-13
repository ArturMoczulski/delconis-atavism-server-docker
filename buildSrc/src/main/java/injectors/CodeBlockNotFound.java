package atavism.buildSrc.injectors;

import java.lang.Exception;

public class CodeBlockNotFound extends Exception {
  public CodeBlockNotFound(String filePath, String codeBlockName, String regex) {
    super("Code block " + codeBlockName + " at " + filePath + "\n\n" + regex);
  }

}
