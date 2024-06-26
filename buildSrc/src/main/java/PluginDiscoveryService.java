package atavism.buildSrc;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import atavism.server.engine.EnginePlugin;

import org.reflections.util.ClasspathHelper;
import java.lang.reflect.*;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.nio.file.*;
import java.io.IOException;
import java.io.File;
import java.util.regex.*;
import atavism.msgsys.Message;

public class PluginDiscoveryService {

  private String projectDir;

  public static final String pluginsSrcDir = "src/plugins";
  public static final String pluginsJavaPackage = "atavism.agis.plugins";

  public PluginDiscoveryService(String projectDir) {
    this.projectDir = projectDir;
  }

  public Set<Class<? extends EnginePlugin>> pluginClasses() {
    Reflections reflections = new Reflections(pluginsJavaPackage, new SubTypesScanner(false));

    // Find all classes within the package that end with 'Plugin'
    Set<Class<? extends EnginePlugin>> allClasses = reflections.getSubTypesOf(EnginePlugin.class);
    Set<Class<? extends EnginePlugin>> pluginClasses = allClasses.stream()
        .filter(clazz -> clazz.getName().endsWith("Plugin"))
        .collect(Collectors.toSet());

    // Set to store the class names that match the file condition
    Set<String> filteredClassNames = new HashSet<>();

    // Traverse all directories starting from baseDir to find matching files
    try {
      Files.walk(Paths.get(
          projectDir + "/" + pluginsSrcDir))
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith("Plugin.java"))
          .forEach(path -> {
            String relativePath = extractClassPathFromPluginSourcePath(path.toString());

            // Convert file path back to a fully qualified class name
            String relativeClassPath = relativePath.toString().replace(File.separatorChar, '.').replace(
                ".java",
                "");

            String fullClassName = relativeClassPath;
            // Check if this class name is in our set of client classes

            pluginClasses.stream()
                .filter(clazz -> clazz.getName().equals(fullClassName))
                .findFirst()
                .ifPresent(clazz -> filteredClassNames.add(clazz.getName()));
          });
    } catch (IOException e) {
      e.printStackTrace();
    }

    Set<Class<? extends EnginePlugin>> customPluginClasses = new HashSet<Class<? extends EnginePlugin>>();
    filteredClassNames.forEach(className -> {
      try {
        Class<? extends EnginePlugin> clazz = (Class<? extends EnginePlugin>) Class.forName(className);

        customPluginClasses.add(clazz);

      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    });

    return customPluginClasses;
  }

  public HashMap<Class<?>, Set<Field>> clientClasses() {
    Reflections reflections = new Reflections(pluginsJavaPackage, new SubTypesScanner(false));

    // Find all classes within the package that end with 'Client'
    Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);
    Set<Class<?>> clientClasses = allClasses.stream()
        .filter(clazz -> clazz.getName().endsWith("Client"))
        .collect(Collectors.toSet());

    // Set to store the class names that match the file condition
    Set<String> filteredClassNames = new HashSet<>();

    // Traverse all directories starting from baseDir to find matching files
    try {
      Files.walk(Paths.get(
          projectDir + "/" + pluginsSrcDir))
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith("Client.java"))
          .forEach(path -> {
            String relativePath = extractClassPathFromPluginSourcePath(path.toString());

            // Convert file path back to a fully qualified class name
            String relativeClassPath = relativePath.toString().replace(File.separatorChar, '.').replace(
                ".java",
                "");

            String fullClassName = relativeClassPath;
            // Check if this class name is in our set of client classes

            clientClasses.stream()
                .filter(clazz -> clazz.getName().equals(fullClassName))
                .findFirst()
                .ifPresent(clazz -> filteredClassNames.add(clazz.getName()));
          });
    } catch (IOException e) {
      e.printStackTrace();
    }

    HashMap<Class<?>, Set<Field>> pluginsClients = new HashMap<Class<?>, Set<Field>>();
    filteredClassNames.forEach(className -> {
      try {
        Class<?> clazz = Class.forName(className);

        if (!pluginsClients.containsKey(clazz)) {
          pluginsClients.put(clazz, new HashSet<Field>());
        }

        pluginsClients.put(
            clazz,
            getMessageTypesFromClientClass(clazz));

      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    });
    return pluginsClients;
  }

  public HashMap<Class<?>, Set<Class<? extends Message>>> clientMessagesClasses(Set<Class<?>> clientClasses) {
    HashMap<Class<?>, Set<Class<? extends Message>>> clientMessagesClasses = new HashMap();

    for (Class<?> clientClass : clientClasses) {
      clientMessagesClasses.put(clientClass, clientMessagesClasses(clientClass));
    }

    return clientMessagesClasses;
  }

  public Set<Class<? extends Message>> clientMessagesClasses(Class<?> clientClass) {
    Set<Class<? extends Message>> messagesClasses = new HashSet<>();

    // Get all declared classes within the outer class
    Class<?>[] declaredClasses = clientClass.getDeclaredClasses();

    for (Class<?> declaredClass : declaredClasses) {
      // Check if the declared class is a subclass of Hook
      if (Message.class.isAssignableFrom(declaredClass) && !Modifier.isAbstract(declaredClass.getModifiers())) {
        // Add the class to the set with a cast to Class<? extends Hook>
        messagesClasses.add(declaredClass.asSubclass(Message.class));
      }
    }

    return messagesClasses;
  }

  private Set<Field> getMessageTypesFromClientClass(Class<?> clazz) {
    HashSet<Field> msgTypes = new HashSet<Field>();

    // Get all fields of the class
    Field[] fields = clazz.getDeclaredFields();

    // Filter static fields starting with "MSG_TYPE_"
    for (Field field : fields) {
      int modifiers = field.getModifiers();
      if (Modifier.isStatic(modifiers) && field.getName().startsWith("MSG_TYPE_")) {
        // Make the field accessible if it is private
        field.setAccessible(true);
        // Add the field name to the set
        msgTypes.add(field);
      }
    }
    return msgTypes;
  }

  public String extractClassPathFromPluginSourcePath(String fullPath) {
    // Regular expression to find '/src/' and capture everything after it until
    // '.java'
    String regex = pluginsSrcDir + "/.*/src/(.*\\.java)";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(fullPath);

    if (matcher.find()) {
      // Return the matched group which contains the path after '/src/'
      return matcher.group(1);
    }
    return null; // or throw an exception if preferred
  }
}
