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
import atavism.buildSrc.*;
import atavism.buildSrc.generators.*;
import atavism.buildSrc.injectors.*;
import atavism.server.engine.EnginePlugin;
import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.io.*;

public abstract class GenerateWorldShTask extends DefaultTask {

        public static final String worldShPath = "atavism_server/bin/world.sh";

        private String projectDir;

        @Input
        public String getProjectDir() {
                return projectDir;
        }

        public void setProjectDir(String projectDir) {
                this.projectDir = projectDir;
        }

        @TaskAction
        void genertateWorldSh() throws Exception {
                Set<Class<? extends EnginePlugin>> pluginClasses = new PluginDiscoveryService(projectDir)
                                .pluginClasses();

                assembleWorldSh(pluginClasses);

        }

        private void assembleWorldSh(Set<Class<? extends EnginePlugin>> pluginClasses)
                        throws Exception, FunctionNotFound, VariableDefinitionNotFound, AllInOneCommandCaseNotFound {
                String allStartFuncs = ShellGenerator.generateStartFuncs(pluginClasses, projectDir);

                ShellInjector.injectBeforeFunction(
                                projectDir + "/" + worldShPath,
                                "start_domain",
                                "CustomPluginsStartFunctions",
                                allStartFuncs.toString());

                String allJavaFlags = ShellGenerator.generateJavaFlags(pluginClasses);

                ShellInjector.injectBeforeVariableDefinition(
                                projectDir + "/" + worldShPath,
                                "JAVA_DOMAIN",
                                "CustomPluginsJavaFlagsFunctions",
                                allJavaFlags.toString());

                String allCommandCases = ShellGenerator.generateStartCommandCases(pluginClasses);

                ShellInjector.injectBeforeAllIneOneStartCommandParam(
                                projectDir + "/" + worldShPath,
                                "CustomPluginsStartCommands",
                                allCommandCases.toString());

                ShellInjector.injectCommandUsage(
                                projectDir + "/" + worldShPath,
                                "CustomPluginsStartCommandUsage",
                                ShellGenerator.generateStartCommandUsage(pluginClasses));

                ShellInjector.injectServerStatusHooks(
                                projectDir + "/" + worldShPath,
                                "CustomPluginsStatusServerHooks",
                                ShellGenerator.generateStatusServerHooks(pluginClasses));

                ShellInjector.injectStopServerHooks(
                                projectDir + "/" + worldShPath,
                                "CustomPluginsStopServerHooks",
                                ShellGenerator.generateStopServerHooks(pluginClasses));

                ShellInjector.injectServerStartHooks(
                                projectDir + "/" + worldShPath,
                                "CustomPluginsStartServerHooks",
                                ShellGenerator.generateStartServerHooks(pluginClasses));

                ShellInjector.injectAgentNamesForStartDomain(
                                projectDir + "/" + worldShPath,
                                "CustomPluginsStartDomainAgentNames",
                                ShellGenerator.generateAgentNamesForStartDomain(pluginClasses));

                ShellInjector.injectPluginTypesForStartDomain(
                                projectDir + "/" + worldShPath,
                                "CustomPluginsStartDomainPluginTypes",
                                ShellGenerator.generatePluginTypesForStartDomain(pluginClasses));

                System.out.println("Generated world.sh in " + worldShPath);
        }

}
