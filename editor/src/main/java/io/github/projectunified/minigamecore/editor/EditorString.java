package io.github.projectunified.minigamecore.editor;

/**
 * The string for the editor
 */
public interface EditorString {
    /**
     * An empty string
     */
    EditorString EMPTY = of("", "");

    /**
     * Create a {@link EditorString}
     *
     * @param key            the key
     * @param defaultMessage the default message
     * @return the {@link EditorString}
     */
    static EditorString of(String key, String defaultMessage) {
        return new EditorString() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public String defaultMessage() {
                return defaultMessage;
            }
        };
    }

    /**
     * Get the key of the string
     *
     * @return the key
     */
    String key();

    /**
     * Get the default message
     *
     * @return the default message
     */
    String defaultMessage();

    /**
     * Get the default message
     *
     * @param args the arguments
     * @return the default message
     */
    default String defaultMessage(Object... args) {
        return String.format(defaultMessage(), args);
    }
}
