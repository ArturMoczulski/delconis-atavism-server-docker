import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ClasspathHelper;
import java.lang.reflect.*;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.nio.file.*;
import java.io.IOException;
import java.io.File;
import java.util.regex.*;
import atavism.buildSrc.PluginDiscoveryService;
import atavism.buildSrc.generators.PythonGenerator;
import atavism.buildSrc.injectors.CodeInjector;
import atavism.server.engine.EnginePlugin;

public abstract class SetPluginsPropertiesTask extends DefaultTask {

    public static final String worldDir = "atavism_server/config/world";
    public static final String worldPropertiesPath = "atavism_server/bin/world.properties";
    public static final String customPluginsPath = "src/plugins";
    public static final String customPluginPropertiesFileName = "plugin.properties";

    // Define a static map to hold all properties
    // Not currently used, instead properties are just
    // read from a file
    public static final Map<String, String> defaultPluginProperties = new HashMap<>();

    static {
        defaultPluginProperties.put("MessageIOExecutorThreadPoolSize", "1");
        defaultPluginProperties.put("MessageIOOrderedExecutorThreadPoolSize", "1");
        defaultPluginProperties.put("ExecutorThreadPoolSize", "1");
        defaultPluginProperties.put("AgentDataThreadPoolSize", "16");
        defaultPluginProperties.put("SubscriptionManagerThreadPoolSize", "15");
        defaultPluginProperties.put("Xms", "1m");
        defaultPluginProperties.put("Xmx", "200m");
    }

    @TaskAction
    void setPluginsPropertiesTask() throws Exception {
        Set<Class<? extends EnginePlugin>> pluginClasses = PluginDiscoveryService.pluginClasses();

        assembleWorldProperties(pluginClasses);
    }

    public void assembleWorldProperties(Set<Class<? extends EnginePlugin>> pluginClasses) {
        injectWorldProperties(pluginClasses);
    }

    private void injectWorldProperties(Set<Class<? extends EnginePlugin>> pluginClasses) {
        Path baseDirectory = Paths.get(customPluginsPath);

        try {
            StringBuilder allProperties = new StringBuilder();

            for (Class<? extends EnginePlugin> pluginClass : pluginClasses) {
                String pluginSimpleName = pluginClass.getSimpleName();
                Path propFilePath = baseDirectory
                        .resolve(pluginSimpleName.replaceAll("Plugin", "") + "/" + customPluginPropertiesFileName);

                if (Files.exists(propFilePath)) {
                    String propertiesContent = new String(Files.readAllBytes(propFilePath));
                    allProperties.append("##\n## Settings for " + pluginClass.getSimpleName() + "\n##\n");
                    allProperties.append(propertiesContent).append("\n");
                } else {
                    System.err.println("Property file not found for " + pluginSimpleName + " at " + propFilePath);
                }
            }

            if (allProperties.length() > 0) {
                CodeInjector.injectCodeBlockAtTheEnd(
                        System.getProperty("user.dir") + "/" + worldPropertiesPath,
                        "CustomPluginsProperties",
                        allProperties.toString(), "##");

                System.out.println("Generated plugins properties in " + worldPropertiesPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<Class<? extends EnginePlugin>, Set<String>> generateWorldProperties(
            Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
        Map<Class<? extends EnginePlugin>, Set<String>> worldProperties = new HashMap<>();
        Path directory = Paths.get(worldDir);
        if (!Files.exists(directory)) {
            throw new Exception("Can't find world directory: " + worldDir);
        }

        for (Class<? extends EnginePlugin> pluginClass : pluginClasses) {
            Set<String> propertiesContent = generateProperties(pluginClass, defaultPluginProperties);
            worldProperties.put(pluginClass, propertiesContent);
        }
        return worldProperties;
    }

    private static Set<String> generateProperties(Class<? extends EnginePlugin> pluginClass,
            Map<String, String> properties) {
        Set<String> propertiesSet = new HashSet();
        String snakeCasePluginName = convertToSnakeCase(pluginClass.getSimpleName().replace("Plugin", ""));

        properties.forEach((key, value) -> {
            propertiesSet.add("atavism." + snakeCasePluginName + "." + key + "=" + value);
        });

        return propertiesSet;
    }

    private static String convertToSnakeCase(String camelCase) {
        // Convert from CamelCase to snake_case
        String regex = "(?<=\\p{Ll})(?=\\p{Lu})";
        return camelCase.replaceAll(regex, "_").toLowerCase();
    }

}
