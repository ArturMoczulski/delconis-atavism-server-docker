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

public class ShellInjector {
  public static void injectPluginTypesForStartDomain(String filePath, String blockName, String newContent)
      throws IOException, RegexNotFound {
    String codeBlockRegex = "^\\s*PLUGIN_TYPES=\".*\"\\s*$";

    String contentRegex = "\"$";

    CodeInjector.modifyCodeBlock(filePath, blockName, codeBlockRegex, contentRegex, " " + newContent + "\"", "##");
  }

  public static void injectAgentNamesForStartDomain(String filePath, String blockName, String newContent)
      throws IOException, RegexNotFound {
    String codeBlockRegex = "^\\s*AGENT_NAMES=\".*\"\\s*$";

    String contentRegex = "\"$";

    CodeInjector.modifyCodeBlock(filePath, blockName, codeBlockRegex, contentRegex, " " + newContent + "\"", "##");
  }

  public static void injectServerStartHooks(String filePath, String blockName, String newContent)
      throws IOException, RegexNotFound {
    String regex = "^\\s*echo \"Wait for finished initializing msg... \"\\s*$";

    CodeInjector.injectCodeBlockBeforeRegex(filePath, blockName, newContent, "##", regex);
  }

  public static void injectStopServerHooks(String filePath, String blockName, String newContent)
      throws IOException, RegexNotFound {
    String regex = "^\\s*if \\[ -e \"\\$\\{AO_RUN\\}\"/domain.pid \\]; then\\s*$";

    CodeInjector.injectCodeBlockBeforeRegex(filePath, blockName, newContent, "##", regex);
  }

  public static void injectServerStatusHooks(String filePath, String blockName, String newContent)
      throws IOException, RegexNotFound {
    String regex = "^\\s*status_process \"domain server  \" \\$\\(cat \"\\$\\{AO_RUN\\}\"/domain.pid\\)\\s*";

    CodeInjector.injectCodeBlockAfterRegex(filePath, blockName, newContent, "##", regex);
  }

  public static void injectCommandUsage(String filePath, String blockName, String newCommands)
      throws IOException, CodeBlockNotFound {
    String codeBlockRegex = "^\\s*echo\\s+\"usage\\s+\\$0\\s+-v\\s+-C\\s+start\\|stop\\|status\\|restart\\|proxy\\|wmgr\\|domain\\|combat\\|instance\\|login\\|objmgr\\|mob\\|arena\\|builder\\|auction\\|weather\\|faction\\|chat\\|quest\\|prefab\\|all_in_one\"\\s*$";

    String contentRegex = "all_in_one";
    String newContent = (newCommands.length() > 0 ? newCommands + "|" : "") + "all_in_one";

    try {
      CodeInjector.modifyCodeBlock(filePath, blockName, codeBlockRegex, contentRegex, newContent, "##");
    } catch (RuntimeException e) { // Assuming RuntimeException is thrown when block is not found
      throw new CodeBlockNotFound(filePath, blockName, codeBlockRegex);
    }
  }

  public static void injectBeforeAllIneOneStartCommandParam(String filePath, String blockName,
      String newContent) throws IOException, AllInOneCommandCaseNotFound {

    String regex = "^\\s*all_in_one\\)\\s*\\n*\\s*start_all_in_one\\s*\\n*\\s*;;\\s*$";

    try {
      CodeInjector.injectCodeBlockBeforeRegex(filePath, blockName, newContent, "##", regex);
    } catch (RegexNotFound e) {
      throw new AllInOneCommandCaseNotFound(filePath);
    }
  }

  public static void injectBeforeVariableDefinition(String filePath, String variableName, String blockName,
      String newContent) throws IOException, VariableDefinitionNotFound {
    // Regex to match the variable definition. It handles optional 'export',
    // arbitrary whitespaces, and any value inside quotes.
    String regex = "^\\s*(export\\s+)?" + Pattern.quote(variableName) + "\\s*=\\s*\".*\"";

    // Use the injectCodeBlockBeforeRegex from the CodeInjector class to inject the
    // new content before the variable definition
    try {
      CodeInjector.injectCodeBlockBeforeRegex(filePath, blockName, newContent, "##", regex);
    } catch (RegexNotFound e) {
      throw new VariableDefinitionNotFound(variableName, filePath);
    }
  }

  public static void injectBeforeFunction(String filePath, String functionName, String blockName, String newContent)
      throws IOException, FunctionNotFound {
    // Construct a regular expression to match the function definition in a shell
    // script
    String regex = "^\\s*function\\s+" + functionName + "\\s*\\(\\s*\\)\\s*\\{";

    // Use the CodeInjector to inject the code before the matched line
    try {
      CodeInjector.injectCodeBlockBeforeRegex(filePath, blockName, newContent, "##", regex);
    } catch (RegexNotFound e) {
      throw new FunctionNotFound(functionName, filePath);
    }
  }
}
