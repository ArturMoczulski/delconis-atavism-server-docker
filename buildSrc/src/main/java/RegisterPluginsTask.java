import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Input;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.ClasspathHelper;
import java.lang.reflect.*;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.nio.file.*;
import java.io.IOException;
import java.io.File;
import java.util.regex.*;
import atavism.buildSrc.PluginDiscoveryService;
import atavism.buildSrc.generators.*;
import atavism.buildSrc.injectors.*;
import atavism.server.engine.EnginePlugin;

public abstract class RegisterPluginsTask extends DefaultTask {

    public static final String worldDir = "atavism_server/config/world";
    public static final String serverStarterPath = "src/lib/atavism/agis/server/ServerStarter.java";
    public static final String allInOnePath = "src/lib/atavism/agis/server/AllInOneServer.java";
    public static final String customPluginsDir = "src/plugins";

    private String projectDir;

    @Input
    public String getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(String projectDir) {
        this.projectDir = projectDir;
    }

    @TaskAction
    void registerPlugins() throws Exception {
        Set<Class<? extends EnginePlugin>> pluginClasses = new PluginDiscoveryService(projectDir).pluginClasses();

        if (pluginClasses.size() == 0) {
            return;
        }

        copyPluginsRegistrationFiles(pluginClasses);
        assembleServerStarter(pluginClasses);
        assembleAllInOne(pluginClasses);
    }

    private void assembleAllInOne(Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
        assembleAllInOneAdsMerges(pluginClasses);
        assembleAllInOnePostScript(pluginClasses);

        System.out.println("Generated AllInOneServer in " + allInOnePath);
    }

    private void assembleAllInOnePostScript(Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {

        if (pluginClasses.size() > 0)
        {
            JavaInjector.addToPostScript(
                    projectDir + "/" + allInOnePath,
                    "CustomPluginsAllInOnePostScript",
                    JavaGenerator.generatePostScript(pluginClasses));

            System.out.println("Generated postScript method in " + allInOnePath);
        }
    }

    private void assembleAllInOneAdsMerges(Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {

        List<String> pluginNames = new ArrayList();

        for (Class<? extends EnginePlugin> pluginClass : pluginClasses) {
            pluginNames.add("\"" + convertToSnakeCase(pluginClass.getSimpleName().replace("Plugin", "")) + "\"");
        }

        if (pluginNames.size() > 0)
        {
            JavaInjector.addToAdsMerger(
                    projectDir + "/" + allInOnePath,
                    "CustomPluginsAllInOneAdsMerger",
                    String.join(", ", pluginNames));
            System.out.println("Generated All In One Ads merger in " + allInOnePath);
        }

    }

    private void assembleServerStarter(Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
        String allStarters = JavaGenerator.generateServerStarterMethods(pluginClasses);

        if (pluginClasses.size() > 0)
        {
            JavaInjector.injectBeforeMethod(
                    projectDir + "/" + serverStarterPath,
                    "startDomain",
                    "CustomPluginsServerStarterMethods",
                    allStarters.toString());
            System.out.println("Generated ServerStarter methods in " + serverStarterPath);
        }

    }

    private void copyPluginsRegistrationFiles(Set<Class<? extends EnginePlugin>> pluginClasses) throws Exception {
        // Ensure the directory path exists
        Path sourceDir = Paths.get(projectDir + "/" + customPluginsDir);
        Path destinationDir = Paths.get(projectDir + "/" + worldDir);
        try {
            if (!Files.exists(destinationDir)) {
                throw new Exception("Can't find world directory: " + worldDir);
            }

            // Process each class in the map
            for (Class<? extends EnginePlugin> pluginClass : pluginClasses) {

                String pluginSimpleName = pluginClass.getSimpleName().replace("Plugin", "");

                // Generate file name from class name
                String fileName = convertToSnakeCase(pluginClass.getSimpleName().replace("Plugin", ""));
                fileName += ".py";

                // Create the file path
                Path sourceFilePath = sourceDir.resolve(pluginSimpleName + "/register.py");
                Path destinationPath = destinationDir.resolve(fileName);

                if (Files.exists(sourceFilePath)) {
                    String content = String.join("\n", Files.readAllLines(sourceFilePath));
                    // Copy the file from source to destination
                    CodeInjector.injectCodeBlockAtTheEnd(
                            destinationPath.toString(),
                            "CustomPluginRegistration",
                            content, "##");
                    System.out.println(
                            "Copied " + pluginSimpleName + " registration Python script to " + destinationPath);
                } else {
                    System.out.println("Plugin registration Python script does not exist for " + pluginSimpleName
                            + " at " + sourceFilePath);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertToSnakeCase(String camelCase) {
        // Convert from CamelCase to snake_case
        String regex = "(?<=\\p{Ll})(?=\\p{Lu})";
        return camelCase.replaceAll(regex, "_").toLowerCase();
    }

}
