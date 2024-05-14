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
        // Convert pluginName and messageName to various formats
        NamingContext namingContext = new NamingContext(pluginName, messageName);

        // Prepare Handlebars
        Handlebars handlebars = new Handlebars();

        // Check if the plugin exists
        Path targetDir = Paths.get(customPluginsPath, namingContext.getPluginCamelCase());
        if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
            throw new IOException("The plugin with the name " + pluginName + " does not exist in " + customPluginsPath);
        }

        // Walk through the template directory
        Path templateDir = Paths.get(pluginTemplatePath);
        Files.walk(templateDir).forEach(source -> {
            try {
                if (Files.isDirectory(source))
                    return;

                // Process only .hbs files
                if (!source.toString().endsWith(".hbs"))
                    return;

                TemplateProcessor templateProcessor = new TemplateProcessor(source, templateDir, namingContext,
                        handlebars);
                templateProcessor.process(targetDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static class NamingContext {
        private final String pluginCamelCase;
        private final String pluginSnakeCase;
        private final String pluginCapitalizedSnakeCase;
        private final String messageCamelCase;
        private final String messageSnakeCase;
        private final String messageCapitalizedSnakeCase;

        public NamingContext(String pluginName, String messageName) {
            this.pluginCamelCase = pluginName;
            this.pluginSnakeCase = pluginName.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
            this.pluginCapitalizedSnakeCase = pluginSnakeCase.toUpperCase();
            this.messageCamelCase = messageName;
            this.messageSnakeCase = messageName.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
            this.messageCapitalizedSnakeCase = messageSnakeCase.toUpperCase();
        }

        public String getPluginCamelCase() {
            return pluginCamelCase;
        }

        public String getPluginSnakeCase() {
            return pluginSnakeCase;
        }

        public String getPluginCapitalizedSnakeCase() {
            return pluginCapitalizedSnakeCase;
        }

        public String getMessageCamelCase() {
            return messageCamelCase;
        }

        public String getMessageSnakeCase() {
            return messageSnakeCase;
        }

        public String getMessageCapitalizedSnakeCase() {
            return messageCapitalizedSnakeCase;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> context = new HashMap<>();
            context.put("pluginCamelCase", pluginCamelCase);
            context.put("pluginSnakeCase", pluginSnakeCase);
            context.put("pluginCapitalizedSnakeCase", pluginCapitalizedSnakeCase);
            context.put("messageCamelCase", messageCamelCase);
            context.put("messageSnakeCase", messageSnakeCase);
            context.put("messageCapitalizedSnakeCase", messageCapitalizedSnakeCase);
            return context;
        }
    }

    private static class TemplateProcessor {
        private final Path source;
        private final Path templateDir;
        private final NamingContext namingContext;
        private final Handlebars handlebars;

        public TemplateProcessor(Path source, Path templateDir, NamingContext namingContext, Handlebars handlebars) {
            this.source = source;
            this.templateDir = templateDir;
            this.namingContext = namingContext;
            this.handlebars = handlebars;
        }

        public void process(Path targetDir) throws IOException {
            // Read the template content as plain text
            String templateContent = new String(Files.readAllBytes(source));

            // Determine the relative path of the template file
            Path relativeSource = templateDir.relativize(source);

            // Apply the Handlebars context to the relative path
            Template pathTemplate = handlebars.compileInline(relativeSource.toString());
            String processedRelativePath = pathTemplate.apply(namingContext.toMap());

            // Remove .hbs suffix from the processed file name
            if (processedRelativePath.endsWith(".hbs")) {
                processedRelativePath = processedRelativePath.substring(0, processedRelativePath.length() - 4);
            }

            // Resolve the destination path with the processed relative path
            Path destination = targetDir.resolve(processedRelativePath);

            // Process each injection point in the template file
            InjectionProcessor injectionProcessor = new InjectionProcessor(templateContent, destination, namingContext,
                    handlebars);
            injectionProcessor.process();
        }
    }

    private static class InjectionProcessor {
        private final String content;
        private final Path destination;
        private final NamingContext namingContext;
        private final Handlebars handlebars;

        public InjectionProcessor(String content, Path destination, NamingContext namingContext,
                Handlebars handlebars) {
            this.content = content;
            this.destination = destination;
            this.namingContext = namingContext;
            this.handlebars = handlebars;
        }

        public void process() throws IOException {
            Pattern injectPattern = Pattern.compile(
                    "\\{\\{!--\\s*@Inject\\(\"([^\"]+)\",\\s*\"([^\"]+)\",?\\s*\"?([^\"]*)\"?,?\\s*\"?([^\"]*)\"?\\)\\s*--\\}\\}");
            Matcher injectMatcher = injectPattern.matcher(content);

            while (injectMatcher.find()) {
                String type = injectMatcher.group(1);
                String target = injectMatcher.group(2);
                String method = injectMatcher.group(3);
                String position = injectMatcher.group(4);

                // Extract the code block associated with this injection point
                int start = injectMatcher.end();
                int end = content.indexOf("{{!--", start);
                if (end == -1) {
                    end = content.length();
                }
                String codeBlock = content.substring(start, end);

                // Process the target to handle handlebars
                Template targetTemplate = handlebars.compileInline(target);
                String processedTarget = targetTemplate.apply(namingContext.toMap());

                // Process the method to handle handlebars
                Template methodTemplate = handlebars.compileInline(method);
                String processedMethod = methodTemplate.apply(namingContext.toMap());

                // Process the code block to handle handlebars
                Template codeBlockTemplate = handlebars.compileInline(codeBlock);
                String processedCodeBlock = codeBlockTemplate.apply(namingContext.toMap());

                // Inject the code block into the destination file
                if (Files.exists(destination)) {
                    List<String> existingLines = Files.readAllLines(destination);
                    int injectionLineNumber = findInjectionLine(existingLines,
                            new InjectionInfo(type, processedTarget, processedMethod, position));

                    if (injectionLineNumber >= 0) {
                        List<String> modifiedLines = new ArrayList<>(existingLines);
                        modifiedLines.add(injectionLineNumber, processedCodeBlock);
                        Files.write(destination, modifiedLines);
                    }
                }
            }
        }

        private int findInjectionLine(List<String> lines, InjectionInfo injectionInfo) {
            String type = injectionInfo.getType();
            String target = injectionInfo.getTarget();
            String method = injectionInfo.getMethod();
            String position = injectionInfo.getPosition();

            boolean insideTarget = false;
            boolean insideMethod = false;
            int braceCount = 0;
            int methodBraceCount = 0;

            // Pattern to match method definitions
            Pattern methodPattern = Pattern.compile(
                    "\\b(public|protected|private|static|final|native|synchronized|abstract|transient|volatile)\\s+" +
                            "(\\<[^>]+\\>\\s+)?(\\w+\\s+)+" +
                            "\\b" + method + "\\b" + "\\s*\\([^\\)]*\\)\\s*\\{");

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();

                if ((type.equals("class") || type.equals("method")) && line.contains("class " + target)) {
                    insideTarget = true;
                }

                if (insideTarget && type.equals("method") && methodPattern.matcher(line).find()) {
                    insideMethod = true;
                }

                if (insideTarget) {
                    braceCount += countOccurrences(line, '{');
                    braceCount -= countOccurrences(line, '}');

                    if (insideMethod) {
                        methodBraceCount += countOccurrences(line, '{');
                        methodBraceCount -= countOccurrences(line, '}');

                        if (methodBraceCount == 0 && line.contains("}")) {
                            if (position.equals("end")) {
                                return i; // Insert before the closing brace of the method
                            }
                            insideMethod = false; // Reset for potential nested methods
                        }
                    }

                    if (type.equals("class") && braceCount == 0) {
                        if (position.equals("end")) {
                            return i; // Insert before the closing brace of the class
                        }
                        insideTarget = false; // Reset for potential nested classes
                    }
                }
            }
            return -1; // Not found
        }

        private int countOccurrences(String line, char ch) {
            int count = 0;
            boolean inString = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '"')
                    inString = !inString; // toggle in-string status
                if (!inString && c == ch)
                    count++;
            }
            return count;
        }

    }

    private static class InjectionInfo {
        private String type = null;
        private String target = null;
        private String method = null;
        private String position = null;

        public InjectionInfo(String param1, String param2, String param3, String param4) {
            if (param1.equals("method")) {
                this.type = param1;
                this.target = param2;
                this.method = param3;
                this.position = param4;
            } else if (param1.equals("class")) {
                this.type = param1;
                this.target = param2;
                this.position = param3;
            }
        }

        public String getType() {
            return type;
        }

        public String getTarget() {
            return target;
        }

        public String getMethod() {
            return method;
        }

        public String getPosition() {
            return position;
        }
    }
}
