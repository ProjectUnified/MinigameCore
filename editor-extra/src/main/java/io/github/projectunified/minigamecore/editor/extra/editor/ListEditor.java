package io.github.projectunified.minigamecore.editor.extra.editor;

import io.github.projectunified.minigamecore.editor.Editor;
import io.github.projectunified.minigamecore.editor.EditorAction;
import io.github.projectunified.minigamecore.editor.EditorActor;
import io.github.projectunified.minigamecore.editor.EditorString;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The editor that handles a list
 *
 * @param <T> the type of the element in the exported list
 */
public abstract class ListEditor<T> implements Editor<List<T>> {
    /**
     * The error message when cannot create an element.
     */
    public static final EditorString CREATE_CANNOT_CREATE = EditorString.of("editor.list.create.cannot_create", "Cannot create (%s)");
    /**
     * The success message for creating an element.
     */
    public static final EditorString CREATE_SUCCESS = EditorString.of("editor.list.create.success", "Added (%s) at index %d");
    /**
     * The description message for the "create" action.
     */
    public static final EditorString CREATE_DESCRIPTION = EditorString.of("editor.list.create.description", "Add element to list");
    /**
     * The usage message for the "create" action.
     */
    public static final EditorString CREATE_USAGE = EditorString.of("editor.list.create.usage", "[args...]");

    /**
     * The error message for invalid index in edit action.
     */
    public static final EditorString EDIT_INVALID_INDEX = EditorString.of("editor.list.edit.invalid_index", "Invalid index: %s");
    /**
     * The error message for index out of bounds in edit action.
     */
    public static final EditorString EDIT_OUT_OF_BOUNDS = EditorString.of("editor.list.edit.out_of_bounds", "Index out of bounds: %d");
    /**
     * The error message when cannot edit an element.
     */
    public static final EditorString EDIT_CANNOT_EDIT = EditorString.of("editor.list.edit.cannot_edit", "Cannot edit (%s)");
    /**
     * The success message for editing an element.
     */
    public static final EditorString EDIT_SUCCESS = EditorString.of("editor.list.edit.success", "Edited (%s)");
    /**
     * The description message for the edit action.
     */
    public static final EditorString EDIT_DESCRIPTION = EditorString.of("editor.list.edit.description", "Edit element in the list");
    /**
     * The usage message for the edit action.
     */
    public static final EditorString EDIT_USAGE = EditorString.of("editor.list.edit.usage", "<index> [args...]");

    /**
     * The error message for invalid index in remove action.
     */
    public static final EditorString REMOVE_INVALID_INDEX = EditorString.of("editor.list.remove.invalid_index", "Invalid index: %s");
    /**
     * The error message for index out of bounds in remove action.
     */
    public static final EditorString REMOVE_OUT_OF_BOUNDS = EditorString.of("editor.list.remove.out_of_bounds", "Index out of bounds: %d");
    /**
     * The success message for removing an element.
     */
    public static final EditorString REMOVE_SUCCESS = EditorString.of("editor.list.remove.success", "Removed element at index %d");
    /**
     * The description message for the remove action.
     */
    public static final EditorString REMOVE_DESCRIPTION = EditorString.of("editor.list.remove.description", "Remove element at index");
    /**
     * The usage message for the remove action.
     */
    public static final EditorString REMOVE_USAGE = EditorString.of("editor.list.remove.usage", "<index>");

    /**
     * The error message for invalid index in move action.
     */
    public static final EditorString MOVE_INVALID_INDEX = EditorString.of("editor.list.move.invalid_index", "Invalid index: %s");
    /**
     * The error message for invalid new index in move action.
     */
    public static final EditorString MOVE_INVALID_NEW_INDEX = EditorString.of("editor.list.move.invalid_new_index", "Invalid new index: %s");
    /**
     * The error message for index out of bounds in move action.
     */
    public static final EditorString MOVE_OUT_OF_BOUNDS = EditorString.of("editor.list.move.out_of_bounds", "Index out of bounds: %d");
    /**
     * The error message for new index out of bounds in move action.
     */
    public static final EditorString MOVE_NEW_OUT_OF_BOUNDS = EditorString.of("editor.list.move.new_out_of_bounds", "New index out of bounds: %d");
    /**
     * The success message for moving an element.
     */
    public static final EditorString MOVE_SUCCESS = EditorString.of("editor.list.move.success", "Moved element at index %d to index %d");
    /**
     * The description message for the move action.
     */
    public static final EditorString MOVE_DESCRIPTION = EditorString.of("editor.list.move.description", "Move element to a new index");
    /**
     * The usage message for the move action.
     */
    public static final EditorString MOVE_USAGE = EditorString.of("editor.list.move.usage", "<index> <newIndex>");

    private final List<T> list = new ArrayList<>();
    private final Map<String, EditorAction> actionMap;

    /**
     * Create a new editor
     */
    protected ListEditor() {
        this.actionMap = new HashMap<>();
        this.actionMap.put("add", new EditorAction() {
            @Override
            public void execute(EditorActor actor, String[] args) {
                T value = create(actor, args);
                if (value == null) {
                    actor.sendMessage(CREATE_CANNOT_CREATE, Arrays.toString(args));
                    return;
                }
                list.add(value);
                actor.sendMessage(CREATE_SUCCESS, Arrays.toString(args), list.size() - 1);
            }

            @Override
            public Collection<String> complete(EditorActor actor, String[] args) {
                return createComplete(actor, args);
            }

            @Override
            public EditorString description() {
                return CREATE_DESCRIPTION;
            }

            @Override
            public EditorString usage() {
                return createUsage();
            }
        });
        this.actionMap.put("edit", new EditorAction() {
            @Override
            public void execute(EditorActor actor, String[] args) {
                if (args.length < 1) {
                    return;
                }
                int index;
                try {
                    index = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    actor.sendMessage(EDIT_INVALID_INDEX, args[0]);
                    return;
                }
                if (index < 0 || index >= list.size()) {
                    actor.sendMessage(EDIT_OUT_OF_BOUNDS, index);
                    return;
                }
                T value = list.get(index);
                T edited = edit(value, actor, Arrays.copyOfRange(args, 1, args.length));
                if (edited == null) {
                    actor.sendMessage(EDIT_CANNOT_EDIT, Arrays.toString(args));
                    return;
                }
                if (edited != value) {
                    list.set(index, edited);
                }
                actor.sendMessage(EDIT_SUCCESS, Arrays.toString(args));
            }

            @Override
            public Collection<String> complete(EditorActor actor, String[] args) {
                if (args.length < 1) {
                    return Collections.emptyList();
                }
                if (args.length == 1) {
                    return IntStream.range(0, list.size())
                            .mapToObj(String::valueOf)
                            .collect(Collectors.toList());
                }
                int index;
                try {
                    index = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    return Collections.emptyList();
                }
                if (index < 0 || index >= list.size()) {
                    return Collections.emptyList();
                }
                T value = list.get(index);
                return editComplete(value, actor, Arrays.copyOfRange(args, 1, args.length));
            }

            @Override
            public EditorString description() {
                return EDIT_DESCRIPTION;
            }

            @Override
            public EditorString usage() {
                return editUsage();
            }
        });
        this.actionMap.put("remove", new EditorAction() {
            @Override
            public void execute(EditorActor actor, String[] args) {
                if (args.length < 1) {
                    return;
                }
                int index;
                try {
                    index = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    actor.sendMessage(REMOVE_INVALID_INDEX, args[0]);
                    return;
                }
                if (index < 0 || index >= list.size()) {
                    actor.sendMessage(REMOVE_OUT_OF_BOUNDS, index);
                    return;
                }
                list.remove(index);
                actor.sendMessage(REMOVE_SUCCESS, index);
            }

            @Override
            public Collection<String> complete(EditorActor actor, String[] args) {
                if (args.length == 1) {
                    return IntStream.range(0, list.size())
                            .mapToObj(String::valueOf)
                            .collect(Collectors.toList());
                }
                return Collections.emptyList();
            }

            @Override
            public EditorString description() {
                return REMOVE_DESCRIPTION;
            }

            @Override
            public EditorString usage() {
                return REMOVE_USAGE;
            }
        });
        this.actionMap.put("move", new EditorAction() {
            @Override
            public void execute(EditorActor actor, String[] args) {
                if (args.length < 2) {
                    return;
                }

                int index;
                try {
                    index = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    actor.sendMessage(MOVE_INVALID_INDEX, args[0]);
                    return;
                }

                int newIndex;
                try {
                    newIndex = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    actor.sendMessage(MOVE_INVALID_NEW_INDEX, args[1]);
                    return;
                }

                if (index < 0 || index >= list.size()) {
                    actor.sendMessage(MOVE_OUT_OF_BOUNDS, index);
                    return;
                }

                if (newIndex < 0 || newIndex >= list.size()) {
                    actor.sendMessage(MOVE_NEW_OUT_OF_BOUNDS, newIndex);
                    return;
                }

                T value = list.get(index);
                list.remove(index);
                list.add(newIndex, value);
                actor.sendMessage(MOVE_SUCCESS, index, newIndex);
            }

            @Override
            public Collection<String> complete(EditorActor actor, String[] args) {
                if (args.length == 1 || args.length == 2) {
                    return IntStream.range(0, list.size())
                            .mapToObj(String::valueOf)
                            .collect(Collectors.toList());
                }
                return Collections.emptyList();
            }

            @Override
            public EditorString description() {
                return MOVE_DESCRIPTION;
            }

            @Override
            public EditorString usage() {
                return MOVE_USAGE;
            }
        });
    }

    /**
     * Create a new element
     *
     * @param actor the actor
     * @param args  the arguments
     * @return the element or null if it cannot be created
     */
    protected abstract T create(EditorActor actor, String[] args);

    /**
     * Get the completion suggestions for actions that create a new element, given the arguments
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
    protected EditorString createUsage() {
        return CREATE_USAGE;
    }

    /**
     * Edit the current element
     *
     * @param data  the element
     * @param actor the actor
     * @param args  the arguments
     * @return the element after editing
     */
    protected abstract T edit(T data, EditorActor actor, String[] args);

    /**
     * Get the completion suggestions for actions that edit the current element, given the arguments
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
        this.list.clear();
    }

    @Override
    public Object status() {
        return list;
    }

    @Override
    public Optional<List<T>> export(EditorActor actor) {
        return Optional.of(this.list);
    }

    /**
     * Get the current list
     *
     * @return the list
     */
    public List<T> list() {
        return Collections.unmodifiableList(this.list);
    }

    @Override
    public void migrate(List<T> data) {
        this.list.addAll(data);
    }
}
