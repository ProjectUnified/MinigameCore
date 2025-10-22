package io.github.projectunified.minigamecore.editor.extra.editor;

import io.github.projectunified.minigamecore.editor.Editor;
import io.github.projectunified.minigamecore.editor.EditorAction;
import io.github.projectunified.minigamecore.editor.EditorActor;
import io.github.projectunified.minigamecore.editor.EditorString;

import java.util.*;

/**
 * The editor that handles a value
 *
 * @param <T> the type of the value
 */
public abstract class ValueEditor<T> implements Editor<T> {
    /**
     * The error message when cannot create a value.
     */
    public static final EditorString SET_CANNOT_CREATE = EditorString.of("editor.value.set.cannot_create", "Cannot create (%s)");
    /**
     * The success message for setting a value.
     */
    public static final EditorString SET_SUCCESS = EditorString.of("editor.value.set.success", "Set (%s)");
    /**
     * The description message for the set action.
     */
    public static final EditorString SET_DESCRIPTION = EditorString.of("editor.value.set.description", "Set the value");
    /**
     * The usage message for the set action.
     */
    public static final EditorString SET_USAGE = EditorString.of("editor.value.set.usage", "[args...]");

    /**
     * The error message when cannot edit a value.
     */
    public static final EditorString EDIT_CANNOT_EDIT = EditorString.of("editor.value.edit.cannot_edit", "Cannot edit (%s)");
    /**
     * The success message for editing a value.
     */
    public static final EditorString EDIT_SUCCESS = EditorString.of("editor.value.edit.success", "Edited (%s)");
    /**
     * The description message for the edit action.
     */
    public static final EditorString EDIT_DESCRIPTION = EditorString.of("editor.value.edit.description", "Edit the value");
    /**
     * The usage message for the edit action.
     */
    public static final EditorString EDIT_USAGE = EditorString.of("editor.value.edit.usage", "[args...]");

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
                T value = set(actor, args);
                if (value == null) {
                    actor.sendMessage(SET_CANNOT_CREATE, Arrays.toString(args));
                    return;
                }
                ValueEditor.this.value = value;
                actor.sendMessage(SET_SUCCESS, Arrays.toString(args));
            }

            @Override
            public EditorString description() {
                return SET_DESCRIPTION;
            }

            @Override
            public EditorString usage() {
                return setUsage();
            }

            @Override
            public Collection<String> complete(EditorActor actor, String[] args) {
                return setComplete(actor, args);
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
                    actor.sendMessage(EDIT_CANNOT_EDIT, Arrays.toString(args));
                    return;
                }
                if (edited != value) {
                    value = edited;
                }
                actor.sendMessage(EDIT_SUCCESS, Arrays.toString(args));
            }

            @Override
            public EditorString description() {
                return EDIT_DESCRIPTION;
            }

            @Override
            public EditorString usage() {
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
     * Set a new value
     *
     * @param actor the actor
     * @param args  the arguments
     * @return the value or null if it cannot be set
     */
    protected abstract T set(EditorActor actor, String[] args);

    /**
     * Get the completion suggestions for actions that set a new value, given the arguments
     *
     * @param actor the action
     * @param args  the argument
     * @return the suggestions
     */
    protected abstract Collection<String> setComplete(EditorActor actor, String[] args);

    /**
     * Get the usage of the set action
     *
     * @return the usage
     */
    protected EditorString setUsage() {
        return SET_USAGE;
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
    protected EditorString editUsage() {
        return EDIT_USAGE;
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
