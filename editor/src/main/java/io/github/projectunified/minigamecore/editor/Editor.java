package io.github.projectunified.minigamecore.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * The Editor
 *
 * @param <T> the type of the exported object
 */
public interface Editor<T> extends EditorAction {
    /**
     * The map of all available actions of the editor
     *
     * @return the action map
     */
    Map<String, EditorAction> actions();

    /**
     * Reset the editor
     */
    void reset();

    /**
     * Get an object representing the status of the editor
     *
     * @return the status
     */
    Object status();

    /**
     * Export the object from the editor
     *
     * @param actor the actor
     * @return the object if it's exported successfully, otherwise empty
     */
    Optional<T> export(EditorActor actor);

    /**
     * Migrate from the object
     *
     * @param data the object
     */
    void migrate(T data);

    @Override
    default String description() {
        return getClass().getSimpleName();
    }

    @Override
    default String usage() {
        return "<action> [args]";
    }

    @Override
    default Collection<String> complete(EditorActor actor, String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }
        Map<String, EditorAction> actions = actions();
        if (args.length == 1) {
            return actions.keySet();
        }
        String actionName = args[0];
        EditorAction action = actions.get(actionName);
        if (action == null) {
            return Collections.emptyList();
        }
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);
        return action.complete(actor, subArgs);
    }

    @Override
    default void execute(EditorActor actor, String[] args) {
        if (args.length == 0) {
            actor.sendUsage(this);
            return;
        }
        String command = args[0];
        EditorAction action = actions().get(command);
        if (action == null) {
            actor.sendMessage("Invalid action: " + command, false);
            return;
        }
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);
        action.execute(actor, subArgs);
    }
}
