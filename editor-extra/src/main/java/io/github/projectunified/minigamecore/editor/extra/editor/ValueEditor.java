package io.github.projectunified.minigamecore.editor.extra.editor;

import io.github.projectunified.minigamecore.editor.Editor;
import io.github.projectunified.minigamecore.editor.EditorAction;
import io.github.projectunified.minigamecore.editor.EditorActor;

import java.util.*;

/**
 * The editor that handles a value
 *
 * @param <T> the type of the value
 */
public abstract class ValueEditor<T> implements Editor<T> {
    private final Map<String, EditorAction> actionMap;
    private T value;

    /**
     * Create a new value editor
     */
    protected ValueEditor() {
        this.actionMap = new HashMap<>();
        this.actionMap.put("set", new EditorAction() {
            @Override
            public void execute(EditorActor actor, String[] args) {
                T value = create(actor, args);
                if (value == null) {
                    actor.sendMessage("Cannot create (" + Arrays.toString(args) + ")", false);
                    return;
                }
                ValueEditor.this.value = value;
                actor.sendMessage("Set (" + Arrays.toString(args) + ")", true);
            }

            @Override
            public String description() {
                return "Set the value";
            }

            @Override
            public String usage() {
                return createUsage();
            }

            @Override
            public Collection<String> complete(EditorActor actor, String[] args) {
                return createComplete(actor, args);
            }
        });
        this.actionMap.put("edit", new EditorAction() {
            @Override
            public void execute(EditorActor actor, String[] args) {
                if (value == null) {
                    return;
                }
                T edited = edit(value, actor, args);
                if (edited == null) {
                    actor.sendMessage("Cannot edit (" + Arrays.toString(args) + ")", false);
                    return;
                }
                if (edited != value) {
                    value = edited;
                }
                actor.sendMessage("Edited (" + Arrays.toString(args) + ")", true);
            }

            @Override
            public String description() {
                return "Edit the value";
            }

            @Override
            public String usage() {
                return editUsage();
            }

            @Override
            public Collection<String> complete(EditorActor actor, String[] args) {
                if (value == null) return Collections.emptyList();
                return editComplete(value, actor, args);
            }
        });
    }

    /**
     * Create a new value
     *
     * @param actor the actor
     * @param args  the arguments
     * @return the value or null if it cannot be created
     */
    protected abstract T create(EditorActor actor, String[] args);

    /**
     * Get the completion suggestions for actions that create a new value, given the arguments
     *
     * @param actor the action
     * @param args  the argument
     * @return the suggestions
     */
    protected abstract Collection<String> createComplete(EditorActor actor, String[] args);

    /**
     * Get the usage of the "create" action
     *
     * @return the usage
     */
    protected String createUsage() {
        return "[args...]";
    }

    /**
     * Edit the current value
     *
     * @param data  the value
     * @param actor the actor
     * @param args  the arguments
     * @return the value after editing
     */
    protected abstract T edit(T data, EditorActor actor, String[] args);

    /**
     * Get the completion suggestions for actions that edit the current value, given the arguments
     *
     * @param actor the action
     * @param args  the argument
     * @return the suggestions
     */
    protected abstract Collection<String> editComplete(T data, EditorActor actor, String[] args);

    /**
     * Get the usage of the "edit" action
     *
     * @return the usage
     */
    protected String editUsage() {
        return "[args...]";
    }

    @Override
    public Map<String, EditorAction> actions() {
        return actionMap;
    }

    @Override
    public void reset() {
        value = null;
    }

    @Override
    public Object status() {
        return value == null ? "null" : value;
    }

    @Override
    public Optional<T> export(EditorActor actor) {
        return Optional.ofNullable(value);
    }

    @Override
    public void migrate(T data) {
        this.value = data;
    }
}
