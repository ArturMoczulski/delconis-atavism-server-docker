import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Input;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public abstract class NewGenericMessageTask extends DefaultTask {

    public static final String pluginTemplatePath = "buildSrc/templates/GenericMessage";
    public static final String customPluginsPath = "src/plugins";

    private String projectDir;
    private String pluginName;
    private String messageName;

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

    @Input
    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    @TaskAction
    public void newGenericMessage() throws Exception {
        // Convert pluginName to camelCase and snake_case formats
        String pluginCamelCase = pluginName;
        String pluginSnakeCase = pluginName.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
        String pluginCapitalizedSnakeCase = pluginSnakeCase.toUpperCase();

        // Convert messageName to camelCase and snake_case formats
        String messageCamelCase = messageName;
        String messageSnakeCase = messageName.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
        String messageCapitalizedSnakeCase = messageSnakeCase.toUpperCase();

        // Prepare Handlebars
        Handlebars handlebars = new Handlebars();

        // Check if the plugin exists
        Path targetDir = Paths.get(customPluginsPath, pluginCamelCase);
        if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
            throw new IOException("The plugin with the name " + pluginName + " does not exist in " + customPluginsPath);
        }

        // Walk through the template directory
        Path templateDir = Paths.get(pluginTemplatePath);
        Files.walk(templateDir).forEach(source -> {
            try {
                if (Files.isDirectory(source)) {
                    return;
                }

                // Relativize the source path to get the template path
                Path relativeSource = templateDir.relativize(source);

                // Process only .hbs files
                if (!relativeSource.toString().endsWith(".hbs")) {
                    return;
                }

                Template fileNameTemplate = handlebars.compileInline(relativeSource.toString());

                // Prepare context for the file name
                Map<String, Object> fileNameContext = new HashMap<>();
                fileNameContext.put("pluginCamelCase", pluginCamelCase);
                String processedFileName = fileNameTemplate.apply(fileNameContext);

                // Remove .hbs suffix from the processed file name
                if (processedFileName.endsWith(".hbs")) {
                    processedFileName = processedFileName.substring(0, processedFileName.length() - 4);
                }

                // Resolve the destination path with the processed file name
                Path destination = targetDir.resolve(processedFileName);

                // Read template file
                List<String> templateLines = Files.readAllLines(source);
                String content = String.join("\n", templateLines);
                Template contentTemplate = handlebars.compileInline(content);

                // Prepare context for the file content
                Map<String, Object> contentContext = new HashMap<>();
                contentContext.put("pluginCamelCase", pluginCamelCase);
                contentContext.put("pluginSnakeCase", pluginSnakeCase);
                contentContext.put("pluginCapitalizedSnakeCase", pluginCapitalizedSnakeCase);
                contentContext.put("messageCamelCase", messageCamelCase);
                contentContext.put("messageCapitalizedSnakeCase", messageCapitalizedSnakeCase);

                // Parse and get injection info
                String injectLine = templateLines.get(0);
                injectLine = injectLine.substring(4, injectLine.length() - 3).trim(); // Remove the {{!-- and --}}
                                                                                      // markers
                InjectionInfo injectionInfo = parseInjectionInfo(injectLine, contentContext);

                // Process template content
                String output = contentTemplate.apply(contentContext);

                // Modify the existing file by injecting the content
                if (Files.exists(destination)) {
                    List<String> existingLines = Files.readAllLines(destination);
                    int injectionLineNumber = findInjectionLine(existingLines, injectionInfo);
                    if (injectionLineNumber >= 0) {
                        List<String> modifiedLines = new ArrayList<>(existingLines);
                        modifiedLines.add(injectionLineNumber, output);
                        Files.write(destination, modifiedLines);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private InjectionInfo parseInjectionInfo(String injectLine, Map<String, Object> context) throws IOException {
        Pattern pattern = Pattern.compile("@Inject\\(\"(.*?)\",\\s*\"(.*?)\",\\s*\"(.*?)\"\\)");
        Matcher matcher = pattern.matcher(injectLine);
        if (matcher.find()) {
            String type = matcher.group(1);
            String target = matcher.group(2);
            String position = matcher.group(3);

            // Process the target to handle handlebars
            Handlebars handlebars = new Handlebars();
            Template targetTemplate = handlebars.compileInline(target);
            String processedTarget = targetTemplate.apply(context);

            return new InjectionInfo(type, processedTarget, position);
        } else {
            throw new IOException("Invalid inject line: " + injectLine);
        }
    }

    private int findInjectionLine(List<String> lines, InjectionInfo injectionInfo) {
        String type = injectionInfo.getType();
        String target = injectionInfo.getTarget();
        String position = injectionInfo.getPosition();

        int braceCount = 0;
        boolean insideTarget = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (type.equals("class") && line.contains("class " + target)) {
                insideTarget = true;
            }

            if (insideTarget) {
                braceCount += countOccurrences(line, '{');
                braceCount -= countOccurrences(line, '}');

                if (braceCount == 0 && line.endsWith("}")) {
                    if (position.equals("end")) {
                        return i; // Insert before the closing brace
                    }
                }
            }
        }
        return -1; // Not found
    }

    private int countOccurrences(String line, char ch) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ch) {
                count++;
            }
        }
        return count;
    }

    private static class InjectionInfo {
        private final String type;
        private final String target;
        private final String position;

        public InjectionInfo(String type, String target, String position) {
            this.type = type;
            this.target = target;
            this.position = position;
        }

        public String getType() {
            return type;
        }

        public String getTarget() {
            return target;
        }

        public String getPosition() {
            return position;
        }
    }
}
