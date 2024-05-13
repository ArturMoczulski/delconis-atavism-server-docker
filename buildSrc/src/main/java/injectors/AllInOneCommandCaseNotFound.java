package atavism.buildSrc.injectors;

import java.lang.Exception;

public class AllInOneCommandCaseNotFound extends Exception {
  public AllInOneCommandCaseNotFound(String filePath) {
    super("world.sh command param all_in_one to use for code injection location not found in " + filePath);
  }

}
