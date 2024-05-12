package atavism.buildSrc.generators;

import java.util.Set;

import atavism.msgsys.MessageType;
import atavism.server.messages.PropertyMessage;

import java.util.HashMap;
import java.lang.reflect.Field;
import atavism.msgsys.Message;

public class PythonGenerator {
  public static String generateAtavismWorldMarshallers(
      HashMap<Class<?>, Set<Class<? extends Message>>> clientMessagesClasses) {

    StringBuilder pyCode = new StringBuilder();
    for (Class<?> clientClass : clientMessagesClasses.keySet()) {

      pyCode.append("# Client messages classes for " + clientClass.getSimpleName() + "\n");

      // Generate list of message classes
      for (Class<? extends Message> clientMessageClass : clientMessagesClasses.get(clientClass)) {
        pyCode.append(clientMessageClass.getName());
      }

      // Add a new line after each class's registrations for better readability
      pyCode.append("\n");
    }
    return pyCode.toString();
  }

  public static String generateAtavismPluginClientsImports(Set<Class<?>> customPluginsClients) {
    StringBuilder imports = new StringBuilder();
    for (Class<?> clientClass : customPluginsClients) {
      // Extract package name and the simple class name
      String fullClassName = clientClass.getName();
      int lastDotIndex = fullClassName.lastIndexOf('.');
      String packageName = fullClassName.substring(0, lastDotIndex);
      String className = fullClassName.substring(lastDotIndex + 1);

      // Create the Python import statement
      imports.append("from ").append(packageName).append(" import ").append(className).append("\n");
    }
    return imports.toString();
  }

  public static String generateAtavsimWorldMessagesPyMsgTranslations(HashMap<Class<?>, Set<Field>> clientClasses) {
    StringBuilder pyCode = new StringBuilder();
    for (Class<?> cls : clientClasses.keySet()) {
      // Append a Python comment with the name of the class
      pyCode.append("# ").append(cls.getSimpleName()).append("\n");

      Set<Field> fields = clientClasses.get(cls);
      for (Field field : fields) {
        // Check if the field is a message type field; assuming it has "MSG_TYPE" in its
        // name
        if (field.getName().contains("MSG_TYPE")) {
          // Generate the Python code for adding message type translations
          pyCode.append("MessageCatalog.addMsgTypeTranslation(aoMessageCatalog, ");
          pyCode.append(cls.getSimpleName()).append(".").append(field.getName()).append(")\n");
        }
      }
      // Add a new line after each class's messages for better readability
      pyCode.append("\n");
    }
    return pyCode.toString();
  }

  public static String generateAtavismExtensionsProxyPySubtypesRegistrations(
      HashMap<Class<?>, Set<Field>> clientClasses) {
    StringBuilder pyCode = new StringBuilder();
    for (Class<?> cls : clientClasses.keySet()) {
      // Python comment indicating the start of a new class's registration
      pyCode.append("# Registrations for ").append(cls.getSimpleName()).append("\n");

      Set<Field> fields = clientClasses.get(cls);
      for (Field field : fields) {
        try {
          // Ensure the field is accessible
          field.setAccessible(true);
          // Get the value of the static field from the class
          MessageType fieldValue = (MessageType) field.get(null);

          // Generate Python registration code
          pyCode.append("proxyPlugin.registerExtensionSubtype(\"");
          pyCode.append(fieldValue.getMsgTypeString()).append("\", ");
          pyCode.append(cls.getSimpleName()).append(".").append(field.getName()).append(")\n");
        } catch (IllegalAccessException e) {
          System.err.println("Failed to access " + field.getName() + " in " + cls.getName());
        }
      }
      // Add a new line after each class's registrations for better readability
      pyCode.append("\n");
    }
    return pyCode.toString();
  }

  public static String generateAtavismAllInOneAdsMessages(
      HashMap<Class<?>, Set<Field>> clientClasses) {
    StringBuilder pyCode = new StringBuilder();
    for (Class<?> cls : clientClasses.keySet()) {
      // Python comment indicating the start of a new class's registration
      pyCode.append("# Registrations for ").append(cls.getSimpleName()).append("\n");

      Set<Field> fields = clientClasses.get(cls);
      for (Field field : fields) {
        try {
          // Ensure the field is accessible
          field.setAccessible(true);
          // Get the value of the static field from the class
          MessageType fieldValue = (MessageType) field.get(null);

          // Generate Python registration code
          pyCode.append(fieldValue.getMsgTypeString()).append("\n");
        } catch (IllegalAccessException e) {
          System.err.println("Failed to access " + field.getName() + " in " + cls.getName());
        }
      }
      // Add a new line after each class's registrations for better readability
      pyCode.append("\n");
    }
    return pyCode.toString();
  }
}
