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
import java.util.HashMap;
import java.util.HashSet;
import java.nio.file.*;
import java.io.IOException;
import java.io.File;
import java.util.regex.*;
import atavism.buildSrc.PluginDiscoveryService;
import atavism.buildSrc.generators.*;
import atavism.buildSrc.injectors.*;
import atavism.server.engine.EnginePlugin;
import atavism.msgsys.Message;

public abstract class RegisterPluginsMessagesTask extends DefaultTask {

    public static final String worldmessagesPyPath = "atavism_server/config/world/worldmessages.py";
    public static final String extensionsProxyPyPath = "atavism_server/config/world/extensions_proxy.py";
    public static final String allInOneAdsPath = "atavism_server/config/world/all_in_one-ads.txt";
    public static final String proxyAdsPath = "atavism_server/config/world/proxy-ads.txt";
    public static final String worldMarshallersPath = "atavism_server/config/world/worldmarshallers.txt";
    public static final String messageInitializerPath = "src/lib/atavism/agis/server/messages/MessageInitializer.java";
    public static final String worldDir = "atavism_server/config/world";

    private String projectDir;

    @Input
    public String getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(String projectDir) {
        this.projectDir = projectDir;
    }

    @TaskAction
    void registerPluginsMessages() throws Exception {
        PluginDiscoveryService pluginDiscovery = new PluginDiscoveryService(projectDir);
        HashMap<Class<?>, Set<Field>> clientMessages = pluginDiscovery.clientClasses();
        HashMap<Class<?>, Set<Class<? extends Message>>> clientMessagesClasses = pluginDiscovery
                .clientMessagesClasses(clientMessages.keySet());
        Set<Class<? extends EnginePlugin>> pluginClasses = pluginDiscovery.pluginClasses();

        assembleWorldMessagesPy(clientMessages);
        assembleExtensionsProxyPy(clientMessages);
        assembleAllInOneAds(clientMessages);
        assembleProxyAds(clientMessages);
        assemblePluginsAds(pluginClasses, clientMessages);
        assembleWorldMarshallers(clientMessagesClasses);
        assembleMessageInitializer(clientMessages);
    }

    private void assembleMessageInitializer(
            HashMap<Class<?>, Set<Field>> clientMessages) throws IOException, FunctionNotFound {
        if (clientMessages.size() == 0) {
            return;
        }
        injectMessageInitialzers(clientMessages, projectDir + "/" + messageInitializerPath);
        System.out.println("Generated client messages initializer in " + messageInitializerPath);
    }

    private void assembleWorldMarshallers(HashMap<Class<?>, Set<Class<? extends Message>>> clientMessagesClasses) {
        if (clientMessagesClasses.size() == 0) {
            return;
        }
        injectWorldMarshallers(clientMessagesClasses, projectDir + "/" + worldMarshallersPath);
        System.out.println("Generated client messages world marshallers in " + worldMarshallersPath);
    }

    private void assemblePluginsAds(Set<Class<? extends EnginePlugin>> pluginClasses,
            HashMap<Class<?>, Set<Field>> clientMessages) throws Exception {
        if (pluginClasses.size() == 0) {
            return;
        }

        // Ensure the directory path exists
        Path directory = Paths.get(projectDir + "/" + worldDir);
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
                System.out.println(pluginSimpleName + ": generated the ads file in " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void assembleProxyAds(HashMap<Class<?>, Set<Field>> clientMessages) {
        if (clientMessages.size() == 0) {
            return;
        }

        injectAds(clientMessages, projectDir + "/" + proxyAdsPath);
        System.out.println("Generated client messages ads in " + proxyAdsPath);
    }

    private void assembleAllInOneAds(HashMap<Class<?>, Set<Field>> clientMessages) {
        if (clientMessages.size() == 0) {
            return;
        }

        injectAds(clientMessages, projectDir + "/" + allInOneAdsPath);
        System.out.println("Generated client messages ads in " + allInOneAdsPath);
    }

    private void injectMessageInitialzers(
            HashMap<Class<?>, Set<Field>> clientMessages,
            String filePath) throws IOException, FunctionNotFound {
        String messageInitializersString = JavaGenerator
                .generateAtavismMessageInitializers(clientMessages);

        JavaInjector.injectMessageInitialzers(
                filePath,
                "CustomPluginsClientMessagesInitializers",
                messageInitializersString);
    }

    private void injectWorldMarshallers(HashMap<Class<?>, Set<Class<? extends Message>>> clientMessagesClasses,
            String filePath) {
        String marshallersString = PythonGenerator
                .generateAtavismWorldMarshallers(clientMessagesClasses);

        CodeInjector.injectCodeBlockAtTheEnd(
                filePath,
                "CustomPluginsClientMessages",
                marshallersString, "##");
    }

    private void injectAds(HashMap<Class<?>, Set<Field>> classes, String filePath) {
        String adsString = PythonGenerator
                .generateAtavismAllInOneAdsMessages(classes);
        CodeInjector.injectCodeBlockAtTheEnd(
                filePath,
                "CustomPluginsClientMessages",
                adsString, "##");
    }

    private void assembleExtensionsProxyPy(HashMap<Class<?>, Set<Field>> clientMessages) {
        if (clientMessages.size() == 0) {
            return;
        }

        injectExtensionSubtypesRegistrations(clientMessages);
    }

    private void injectExtensionSubtypesRegistrations(HashMap<Class<?>, Set<Field>> classes) {
        String subtypesRegistrationsString = PythonGenerator
                .generateAtavismExtensionsProxyPySubtypesRegistrations(classes);
        CodeInjector.injectCodeBlockAtTheEnd(
                projectDir + "/" + extensionsProxyPyPath,
                "CustomPluginsClientMessagesSubtypesRegistrations",
                subtypesRegistrationsString, "##");

        System.out.println("Generated client messages extensions subtypes in " + extensionsProxyPyPath);
    }

    private void assembleWorldMessagesPy(HashMap<Class<?>, Set<Field>> clientMessages) {
        if (clientMessages.size() == 0) {
            return;
        }

        injectWorldMessagesPyImports(clientMessages.keySet());
        injectWorldMessaggesPyMsgTranslations(clientMessages);
    }

    private void injectWorldMessaggesPyMsgTranslations(HashMap<Class<?>, Set<Field>> classes) {
        String msgTranslationsString = PythonGenerator.generateAtavsimWorldMessagesPyMsgTranslations(classes);
        CodeInjector.injectCodeBlockAtTheEnd(
                projectDir + "/" + worldmessagesPyPath,
                "CustomPluginsClientMessagesTranslations",
                msgTranslationsString, "##");
        System.out.println("Generated client messages translations in " + worldmessagesPyPath);
    }

    private void injectWorldMessagesPyImports(Set<Class<?>> classes) {
        String importsString = PythonGenerator.generateAtavismPluginClientsImports(classes);
        CodeInjector.injectCodeBlockAtTheTop(
                projectDir + "/" + worldmessagesPyPath,
                "CustomPluginsImports",
                importsString, "##");
        System.out.println("Generated client classes imports in " + worldmessagesPyPath);
    }

    private String convertToSnakeCase(String camelCase) {
        // Convert from CamelCase to snake_case
        String regex = "(?<=\\p{Ll})(?=\\p{Lu})";
        return camelCase.replaceAll(regex, "_").toLowerCase();
    }
}
