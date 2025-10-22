package io.github.projectunified.minigamecore.editor;

/**
 * The actor that interacts the editor
 */
public interface EditorActor {
    /**
     * Send a message
     *
     * @param message the message
     * @param args    the arguments
     */
    void sendMessage(EditorString message, Object... args);

    /**
     * Send the usage of the action
     *
     * @param action the action
     */
    void sendUsage(EditorAction action);

    /**
     * Send the usage of the editor
     *
     * @param editor the editor
     */
    void sendUsage(Editor<?> editor);
}
