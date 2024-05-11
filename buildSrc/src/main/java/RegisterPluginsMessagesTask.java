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
import java.util.HashSet;
import java.nio.file.*;
import java.io.IOException;
import java.io.File;
import java.util.regex.*;
import atavism.buildSrc.PluginDiscoveryService;
import atavism.buildSrc.generators.PythonGenerator;
import atavism.buildSrc.injectors.CodeInjector;
import atavism.server.engine.EnginePlugin;

public abstract class RegisterPluginsMessagesTask extends DefaultTask {

    public static final String worldmessagesPyPath = "atavism_server/config/world/worldmessages.py";
    public static final String extensionsProxyPyPath = "atavism_server/config/world/extensions_proxy.py";
    public static final String allInOneAdsPath = "atavism_server/config/world/all_in_one-ads.txt";
    public static final String proxyAdsPath = "atavism_server/config/world/proxy-ads.txt";
    public static final String worldDir = "atavism_server/config/world";

    @TaskAction
    void registerPluginsMessages() throws Exception {
        HashMap<Class<?>, Set<Field>> clientMessages = PluginDiscoveryService.clientClasses();
        Set<Class<? extends EnginePlugin>> pluginClasses = PluginDiscoveryService.pluginClasses();

        assembleWorldMessagesPy(clientMessages);
        assembleExtensionsProxyPy(clientMessages);
        assembleAllInOneAds(clientMessages);
        assembleProxyAds(clientMessages);
        assemblePluginsAds(pluginClasses, clientMessages);
    }

    private void assemblePluginsAds(Set<Class<? extends EnginePlugin>> pluginClasses,
            HashMap<Class<?>, Set<Field>> clientMessages) throws Exception {
        // Ensure the directory path exists
        Path directory = Paths.get(worldDir);
        try {
            if (!Files.exists(directory)) {
                throw new Exception("Can't find world directory: " + worldDir);
            }

            // Process each class in the map
            for (Class<? extends EnginePlugin> pluginClass : pluginClasses) {

                String pluginSimpleName = pluginClass.getSimpleName().replace("Plugin", "");

                // Generate file name from class name
                String fileName = convertToSnakeCase(pluginClass.getSimpleName().replace("Plugin", ""));
                fileName += "-ads.txt";

                // Create the file path
                Path filePath = directory.resolve(fileName);

                // Filter clientMessages to find matching client classes for this plugin
                HashMap<Class<?>, Set<Field>> filteredClasses = new HashMap<>();
                clientMessages.forEach((clientClass, fields) -> {
                    if (clientClass.getSimpleName().replace("Client", "").equals(pluginSimpleName)) {
                        filteredClasses.put(clientClass, fields);
                    }
                });

                // Write the ads
                injectAds(filteredClasses, filePath.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void assembleProxyAds(HashMap<Class<?>, Set<Field>> clientMessages) {
        injectAds(clientMessages, proxyAdsPath);
    }

    private void assembleAllInOneAds(HashMap<Class<?>, Set<Field>> clientMessages) {
        injectAds(clientMessages, allInOneAdsPath);
    }

    private void injectAds(HashMap<Class<?>, Set<Field>> classes, String filePath) {
        String adsString = PythonGenerator
                .generateAtavismAllInOneAdsMessages(classes);
        CodeInjector.injectCodeBlockAtTheEnd(
                System.getProperty("user.dir") + "/" + filePath,
                "CustomPluginsClientMessages",
                adsString, "##");
    }

    private void assembleExtensionsProxyPy(HashMap<Class<?>, Set<Field>> clientMessages) {
        injectExtensionSubtypesRegistrations(clientMessages);
    }

    private void injectExtensionSubtypesRegistrations(HashMap<Class<?>, Set<Field>> classes) {
        String subtypesRegistrationsString = PythonGenerator
                .generateAtavismExtensionsProxyPySubtypesRegistrations(classes);
        CodeInjector.injectCodeBlockAtTheEnd(
                System.getProperty("user.dir") + "/" + extensionsProxyPyPath,
                "CustomPluginsClientMessagesSubtypesRegistrations",
                subtypesRegistrationsString, "##");
    }

    private void assembleWorldMessagesPy(HashMap<Class<?>, Set<Field>> clientMessages) {
        injectWordMessagesPyImports(clientMessages.keySet());
        injectWorldMessaggesPyMsgTranslations(clientMessages);
    }

    private void injectWorldMessaggesPyMsgTranslations(HashMap<Class<?>, Set<Field>> classes) {
        String msgTranslationsString = PythonGenerator.generateAtavsimWorldMessagesPyMsgTranslations(classes);
        CodeInjector.injectCodeBlockAtTheEnd(
                System.getProperty("user.dir") + "/" + worldmessagesPyPath,
                "CustomPluginsClientMessagesTranslations",
                msgTranslationsString, "##");
    }

    private void injectWordMessagesPyImports(Set<Class<?>> classes) {
        String importsString = PythonGenerator.generateAtavismPluginClientsImports(classes);
        CodeInjector.injectCodeBlockAtTheTop(
                System.getProperty("user.dir") + "/" + worldmessagesPyPath,
                "CustomPluginsImports",
                importsString, "##");
    }

    private String convertToSnakeCase(String camelCase) {
        // Convert from CamelCase to snake_case
        String regex = "(?<=\\p{Ll})(?=\\p{Lu})";
        return camelCase.replaceAll(regex, "_").toLowerCase();
    }
}
