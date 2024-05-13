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
import com.mysql.cj.protocol.Message;

public class JavaGenerator {
  public static String generateServerStarterMethods(
      Set<Class<? extends EnginePlugin>> pluginClasses) {

    StringBuilder javaCode = new StringBuilder();
    for (Class<? extends EnginePlugin> pluginClass : pluginClasses) {
      String methodName = "start" + pluginClass.getSimpleName().replace("Plugin", "");

      String methodString = "";
      methodString += "\tpublic static void " + methodName + "() {\n";
      methodString += "\t\tEngine.registerPlugin(\"" + pluginClass.getName() + "\");\n";
      methodString += "\t}\n";

      javaCode.append(methodString);
    }

    return javaCode.toString();
  }

  public static String generateAtavismMessageInitializers(
      HashMap<Class<?>, Set<Field>> clientMessages) {

    StringBuilder javaCode = new StringBuilder();
    for (Class<?> clientClass : clientMessages.keySet()) {

      javaCode.append("// Client messages for " + clientClass.getSimpleName() + "\n");

      // Generate list of message classes
      for (Field field : clientMessages.get(clientClass)) {
        String initializerString = "";
        initializerString += "aoMessageCatalog.addMsgTypeTranslation(";
        initializerString += clientClass.getSimpleName() + "." + field.getName();
        initializerString += ");";
        initializerString += "\n";

        javaCode.append(initializerString);
      }
    }
    return javaCode.toString();
  }

  private static String convertToSnakeCase(String camelCase) {
    // Convert from CamelCase to snake_case
    String regex = "(?<=\\p{Ll})(?=\\p{Lu})";
    return camelCase.replaceAll(regex, "_").toLowerCase();
  }
}
