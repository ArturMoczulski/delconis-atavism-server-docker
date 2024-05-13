package atavism.buildSrc.injectors;

import java.lang.Exception;

public class FunctionNotFound extends Exception {
  public FunctionNotFound(String functionName, String filePath) {
    super("function " + functionName + " not found in " + filePath);
  }

}
