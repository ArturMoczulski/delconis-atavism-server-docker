import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.ClasspathHelper
import java.util.stream.Collectors
import java.util.Set

public abstract class RegisterPluginsMessagesTask extends DefaultTask {
  
    @TaskAction
    void registerPluginsMessages() {
        // println "RegisterPluginsMessagesTask called"
        // Set<String> allMsgTypes = findClientClasses()
        // println "All MSG_TYPE properties: $allMsgTypes"
    }

    // private Set<String> findClientClasses() {
    //     String packageName = 'atavism.agis.plugins'
    //     Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        
    //     // Find all classes within the package that end with 'Client'
    //     Set<Class<?>> clientClasses = reflections.getSubTypesOf(Object.class)
    //         .stream()
    //         .filter(clazz -> clazz.getName().endsWith("Client"))
    //         .collect(Collectors.toSet());

    //     Set<String> msgTypes = new HashSet<>();
    //     clientClasses.forEach(clazz -> msgTypes.addAll(printMessageTypes(clazz)));
    //     return msgTypes;
    // }

    // private Set<String> printMessageTypes(Class<?> clazz) {
    //     Set<String> msgTypeFields = new HashSet<>();

    //     // Get all fields of the class
    //     Field[] fields = clazz.getDeclaredFields();

    //     // Filter static fields starting with "MSG_TYPE_"
    //     for (Field field : fields) {
    //         int modifiers = field.getModifiers();
    //         if (Modifier.isStatic(modifiers) && field.getName().startsWith("MSG_TYPE_")) {
    //             try {
    //                 // Make the field accessible if it is private
    //                 field.setAccessible(true);
    //                 // Add the field name to the set
    //                 msgTypeFields.add(field.getName());
    //             } catch (IllegalAccessException e) {
    //                 println "Unable to access " + field.getName() + " in " + clazz.getName()
    //             }
    //         }
    //     }
    //     return msgTypeFields;
    // }
}
