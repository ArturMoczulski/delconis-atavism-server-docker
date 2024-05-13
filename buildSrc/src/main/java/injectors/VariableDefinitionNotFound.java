package atavism.buildSrc.injectors;

import java.lang.Exception;

public class VariableDefinitionNotFound extends Exception {
  public VariableDefinitionNotFound(String varName, String filePath) {
    super("variable definition " + varName + " not found in " + filePath);
  }

}
