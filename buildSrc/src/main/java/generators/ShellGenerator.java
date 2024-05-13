package atavism.buildSrc.generators;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ClasspathHelper;
import java.lang.reflect.*;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.*;
import java.io.IOException;
import java.io.File;
import java.util.regex.*;
import atavism.buildSrc.*;
import atavism.buildSrc.generators.*;
import atavism.buildSrc.injectors.*;
import atavism.server.engine.EnginePlugin;
import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.io.*;

public class ShellGenerator {
  public static final String templatesDir = "buildSrc/templates/sh";

  public static String generatePluginTypesForStartDomain(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
    // Start functions
    List<String> pluginTypesStrings = new ArrayList<>();
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      String pluginTypesString = "-p " + clazz.getSimpleName().replace("Plugin", "");
      pluginTypesString += ",1";

      pluginTypesStrings.add(pluginTypesString);
    }

    return String.join(" ", pluginTypesStrings);
  }

  public static String generateAgentNamesForStartDomain(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
    // Start functions
    List<String> agentNamesStrings = new ArrayList<>();
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      String snakeCasePluginName = convertToSnakeCase(clazz.getSimpleName().replace("Plugin", ""));

      String agentNamesString = "-a " + snakeCasePluginName;

      agentNamesStrings.add(agentNamesString);

    }

    return String.join(" ", agentNamesStrings);
  }

  public static String generateStartServerHooks(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
    // Start functions
    StringBuilder allStartLines = new StringBuilder();
    allStartLines.append("\tif [ X$RUN_ALL_IN_ONE = X\"false\" ]; then\n");
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      String snakeCasePluginName = convertToSnakeCase(clazz.getSimpleName().replace("Plugin", ""));

      allStartLines.append("\t\tstart_" + snakeCasePluginName + "\n");

    }
    allStartLines.append("\tfi");

    return allStartLines.toString();
  }

  public static String generateStopServerHooks(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
    // Start functions
    StringBuilder allKillLines = new StringBuilder();
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      String snakeCasePluginName = convertToSnakeCase(clazz.getSimpleName().replace("Plugin", ""));

      allKillLines.append("\tif [ -e \"${AO_RUN}\"/" + snakeCasePluginName + ".pid ]; then\n");
      allKillLines.append(
          "\t\tkill_process \"" + snakeCasePluginName + " server  \" $(cat \"${AO_RUN}\"/" + snakeCasePluginName
              + ".pid)\n");
      allKillLines.append("\tfi\n");

    }

    return allKillLines.toString();
  }

  public static String generateStatusServerHooks(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
    // Start functions
    StringBuilder allStatusLines = new StringBuilder();
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      String snakeCasePluginName = convertToSnakeCase(clazz.getSimpleName().replace("Plugin", ""));

      allStatusLines.append("if [ $? -ne 0 ]; then down=1 ; fi\n");
      allStatusLines
          .append(
              "status_process \"" + snakeCasePluginName + " \" $(cat \"${AO_RUN}\"/" + snakeCasePluginName + ".pid)\n");
    }

    return allStatusLines.toString();
  }

  public static String generateStartCommandUsage(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
    // Start functions
    List<String> allCommands = new ArrayList();
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      String snakeCasePluginName = convertToSnakeCase(clazz.getSimpleName().replace("Plugin", ""));
      allCommands.add(snakeCasePluginName);
    }

    return String.join("|", allCommands);
  }

  public static String generateStartCommandCases(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
    // Start functions
    HashMap<Class<? extends EnginePlugin>, String> commandCases = generateStartCommandCasesByPlugin(pluginClasses);

    StringBuilder allCases = new StringBuilder();
    for (Class<? extends EnginePlugin> clazz : commandCases.keySet()) {
      allCases.append(commandCases.get(clazz) + "\n");
    }

    return allCases.toString();
  }

  public static HashMap<Class<? extends EnginePlugin>, String> generateStartCommandCasesByPlugin(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {

    HashMap<Class<? extends EnginePlugin>, String> commands = new HashMap<>();
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      String snakeCasePluginName = convertToSnakeCase(clazz.getSimpleName().replace("Plugin", ""));
      String command = snakeCasePluginName;
      String startFuncName = "start_" + snakeCasePluginName;

      String commandCase = "    " + command + ")\n        " + startFuncName + "\n        ;;";

      commands.put(clazz, commandCase);
    }

    return commands;
  }

  public static String generateJavaFlags(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
    // Start functions
    HashMap<Class<? extends EnginePlugin>, String> flags = generateJavaFlagsByPlugin(pluginClasses);

    StringBuilder allFlags = new StringBuilder();
    for (Class<? extends EnginePlugin> clazz : flags.keySet()) {
      // Append the class name as a comment and its start function code to the
      // StringBuilder
      allFlags.append(flags.get(clazz) + "\n");
    }

    return allFlags.toString();
  }

  public static HashMap<Class<? extends EnginePlugin>, String> generateJavaFlagsByPlugin(
      Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {

    HashMap<Class<? extends EnginePlugin>, String> flags = new HashMap<>();
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      String varName = "JAVA_" + convertToSnakeCase(clazz.getSimpleName().replace("Plugin", "")).toUpperCase();

      String varValue = "\"${JVM_FLAG}   -Xms$atavism_domain_Xms -Xmx$atavism_domain_Xmx ${JAVA_FLAGS} \"";

      String flag = varName + "=" + varValue;

      flags.put(clazz, flag);
    }

    return flags;
  }

  public static String generateStartFuncs(
      Set<Class<? extends EnginePlugin>> pluginClasses, String projectDir) throws Exception {
    // Start functions
    HashMap<Class<? extends EnginePlugin>, String> startFuncs = generateStartFuncsByPlugin(pluginClasses, projectDir);

    StringBuilder allStartFuncs = new StringBuilder();
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      // Append the class name as a comment and its start function code to the
      // StringBuilder
      allStartFuncs.append("# ")
          .append(clazz.getSimpleName())
          .append(" start function generated by AtavismServerDocker\n")
          .append(startFuncs.get(clazz))
          .append("\n\n"); // Ensure there is a blank line between functions
    }

    return allStartFuncs.toString();
  }

  public static HashMap<Class<? extends EnginePlugin>, String> generateStartFuncsByPlugin(
      Set<Class<? extends EnginePlugin>> pluginClasses, String projectDir) throws Exception {
    TemplateLoader loader = new FileTemplateLoader(projectDir + "/" + templatesDir, ".sh");
    Handlebars handlebars = new Handlebars().with(loader);
    Template template = handlebars.compile("start_plugin");

    HashMap<Class<? extends EnginePlugin>, String> startsFuns = new HashMap<>();
    for (Class<? extends EnginePlugin> clazz : pluginClasses) {
      HashMap<String, String> params = new HashMap<>();
      params.put("agentType", convertToSnakeCase(clazz.getSimpleName().replace("Plugin", "")));
      params.put("agentName", convertToSnakeCase(clazz.getSimpleName().replace("Plugin", "")));
      params.put("javaPluginFlagsVar",
          "${JAVA_" + convertToSnakeCase(clazz.getSimpleName().replace("Plugin", "")).toUpperCase() + "}");

      String generated = template.apply(params);

      startsFuns.put(clazz, generated);
    }

    return startsFuns;
  }

  private static String convertToSnakeCase(String camelCase) {
    // Convert from CamelCase to snake_case
    String regex = "(?<=\\p{Ll})(?=\\p{Lu})";
    return camelCase.replaceAll(regex, "_").toLowerCase();
  }
}
