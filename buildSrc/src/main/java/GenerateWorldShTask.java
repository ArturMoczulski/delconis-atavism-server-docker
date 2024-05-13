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
import atavism.buildSrc.*;
import atavism.buildSrc.generators.*;
import atavism.buildSrc.injectors.*;
import atavism.server.engine.EnginePlugin;
import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.io.*;

public abstract class GenerateWorldShTask extends DefaultTask {

        public static final String worldShPath = "atavism_server/bin/world.sh";

        @TaskAction
        void genertateWorldSh() throws Exception {
                Set<Class<? extends EnginePlugin>> pluginClasses = PluginDiscoveryService.pluginClasses();

                assembleWorldSh(pluginClasses);

        }

        private static void assembleWorldSh(Set<Class<? extends EnginePlugin>> pluginClasses)
                        throws Exception, FunctionNotFound, VariableDefinitionNotFound, AllInOneCommandCaseNotFound {
                String allStartFuncs = ShellGenerator.generateStartFuncs(pluginClasses);

                ShellInjector.injectBeforeFunction(
                                System.getProperty("user.dir") + "/" + worldShPath,
                                "start_domain",
                                "CustomPluginsStartFunctions",
                                allStartFuncs.toString());

                String allJavaFlags = ShellGenerator.generateJavaFlags(pluginClasses);

                ShellInjector.injectBeforeVariableDefinition(
                                System.getProperty("user.dir") + "/" + worldShPath,
                                "JAVA_DOMAIN",
                                "CustomPluginsJavaFlagsFunctions",
                                allJavaFlags.toString());

                String allCommandCases = ShellGenerator.generateStartCommandCases(pluginClasses);

                ShellInjector.injectBeforeAllIneOneStartCommandParam(
                                System.getProperty("user.dir") + "/" + worldShPath,
                                "CustomPluginsStartCommands",
                                allCommandCases.toString());

                ShellInjector.injectCommandUsage(
                                System.getProperty("user.dir") + "/" + worldShPath,
                                "CustomPluginsStartCommandUsage",
                                ShellGenerator.generateStartCommandUsage(pluginClasses));

                ShellInjector.injectServerStatusHooks(
                                System.getProperty("user.dir") + "/" + worldShPath,
                                "CustomPluginsStatusServerHooks",
                                ShellGenerator.generateStatusServerHooks(pluginClasses));

                ShellInjector.injectStopServerHooks(
                                System.getProperty("user.dir") + "/" + worldShPath,
                                "CustomPluginsStopServerHooks",
                                ShellGenerator.generateStopServerHooks(pluginClasses));

                ShellInjector.injectServerStartHooks(
                                System.getProperty("user.dir") + "/" + worldShPath,
                                "CustomPluginsStartServerHooks",
                                ShellGenerator.generateStartServerHooks(pluginClasses));

                ShellInjector.injectAgentNamesForStartDomain(
                                System.getProperty("user.dir") + "/" + worldShPath,
                                "CustomPluginsStartDomainAgentNames",
                                ShellGenerator.generateAgentNamesForStartDomain(pluginClasses));

                ShellInjector.injectPluginTypesForStartDomain(
                                System.getProperty("user.dir") + "/" + worldShPath,
                                "CustomPluginsStartDomainPluginTypes",
                                ShellGenerator.generatePluginTypesForStartDomain(pluginClasses));

                System.out.println("Generated world.sh in " + worldShPath);
        }

}
