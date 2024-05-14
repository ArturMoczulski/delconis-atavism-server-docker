import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Input;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.*;
import java.io.IOException;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public abstract class NewPluginTask extends DefaultTask {

    public static final String pluginTemplatePath = "buildSrc/templates/plugin";
    public static final String customPluginsPath = "src/plugins";

    private String projectDir;
    private String pluginName;

    @Input
    public String getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(String projectDir) {
        this.projectDir = projectDir;
    }

    @Input
    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    @TaskAction
    public void newPlugin() throws Exception {
        // Convert pluginName to snake_case and camelCase
        String pluginSnakeCase = pluginName.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
        String pluginCamelCase = pluginName;

        // Prepare Handlebars
        Handlebars handlebars = new Handlebars();

        // Create the target directory
        Path targetDir = Paths.get(customPluginsPath, pluginName);

        // Check if the plugin already exists
        if (Files.exists(targetDir)) {
            throw new IOException("A plugin with the name " + pluginName + " already exists in " + customPluginsPath);
        }

        Files.createDirectories(targetDir);

        // Walk through the template directory
        Path templateDir = Paths.get(pluginTemplatePath);
        Files.walk(templateDir).forEach(source -> {
            try {
                // Relativize the source path to get the template path
                Path relativeSource = templateDir.relativize(source);
                
                Template fileNameTemplate = handlebars.compileInline(relativeSource.toString());

                // Prepare context for the file name
                Map<String, Object> fileNameContext = new HashMap<>();
                fileNameContext.put("pluginSnakeCase", pluginSnakeCase);
                fileNameContext.put("pluginCamelCase", pluginCamelCase);
                String processedFileName = fileNameTemplate.apply(fileNameContext);

                // Remove .hbs suffix from the processed file name
                if (processedFileName.endsWith(".hbs")) {
                    processedFileName = processedFileName.substring(0, processedFileName.length() - 4);
                }
                
                // Resolve the destination path with the processed file name
                Path destination = targetDir.resolve(processedFileName);
                
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destination);
                } else {
                    // Read template file
                    String content = new String(Files.readAllBytes(source));
                    Template contentTemplate = handlebars.compileInline(content);

                    // Prepare context for the file content
                    Map<String, Object> contentContext = new HashMap<>();
                    contentContext.put("pluginSnakeCase", pluginSnakeCase);
                    contentContext.put("pluginCamelCase", pluginCamelCase);

                    // Write processed template to destination
                    String output = contentTemplate.apply(contentContext);
                    Files.write(destination, output.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
