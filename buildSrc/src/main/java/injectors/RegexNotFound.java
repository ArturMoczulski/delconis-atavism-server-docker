package atavism.buildSrc.injectors;

import java.lang.Exception;

public class RegexNotFound extends Exception {
  public RegexNotFound(String regex, String filePath) {
    super("regex not found " + regex + " not found in " + filePath);
  }
}
