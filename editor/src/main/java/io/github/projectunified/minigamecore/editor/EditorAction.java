package io.github.projectunified.minigamecore.editor;

import java.util.Collection;
import java.util.Collections;

/**
 * An action of the editor
 */
public interface EditorAction {
    /**
     * Execute the action
     *
     * @param actor the actor
     * @param args  the arguments
     */
    void execute(EditorActor actor, String[] args);

    /**
     * Get the description of the action
     *
     * @return the description
     */
    String description();

    /**
     * Get the completion suggestions, given the arguments
     *
     * @param actor the action
     * @param args  the argument
     * @return the suggestions
     */
    default Collection<String> complete(EditorActor actor, String[] args) {
        return Collections.emptyList();
    }

    /**
     * Get the usage of the action
     *
     * @return the usage
     */
    default String usage() {
        return "";
    }
}
