package io.github.projectunified.minigamecore.editor.extra.editor;

import io.github.projectunified.minigamecore.editor.Editor;
import io.github.projectunified.minigamecore.editor.EditorAction;
import io.github.projectunified.minigamecore.editor.EditorActor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The editor that handles a list
 *
 * @param <T> the type of the element in the exported list
 */
public abstract class ListEditor<T> implements Editor<List<T>> {
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
                    actor.sendMessage("Cannot create (" + Arrays.toString(args) + ")", false);
                    return;
                }
                list.add(value);
                actor.sendMessage("Added (" + Arrays.toString(args) + ") at index " + (list.size() - 1), true);
            }

            @Override
            public Collection<String> complete(EditorActor actor, String[] args) {
                return createComplete(actor, args);
            }

            @Override
            public String description() {
                return "Add element to list";
            }

            @Override
            public String usage() {
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
                    actor.sendMessage("Invalid index: " + args[0], false);
                    return;
                }
                if (index < 0 || index >= list.size()) {
                    actor.sendMessage("Index out of bounds: " + index, false);
                    return;
                }
                T value = list.get(index);
                T edited = edit(value, actor, Arrays.copyOfRange(args, 1, args.length));
                if (edited == null) {
                    actor.sendMessage("Cannot edit (" + Arrays.toString(args) + ")", false);
                    return;
                }
                if (edited != value) {
                    list.set(index, edited);
                }
                actor.sendMessage("Edited (" + Arrays.toString(args) + ")", true);
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
            public String description() {
                return "Edit element in the list";
            }

            @Override
            public String usage() {
                return "<index> " + editUsage();
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
                    actor.sendMessage("Invalid index: " + args[0], false);
                    return;
                }
                if (index < 0 || index >= list.size()) {
                    actor.sendMessage("Index out of bounds: " + index, false);
                    return;
                }
                list.remove(index);
                actor.sendMessage("Removed element at index " + index, true);
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
            public String description() {
                return "Remove element at index";
            }

            @Override
            public String usage() {
                return "<index>";
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
                    actor.sendMessage("Invalid index: " + args[0], false);
                    return;
                }

                int newIndex;
                try {
                    newIndex = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    actor.sendMessage("Invalid new index: " + args[1], false);
                    return;
                }

                if (index < 0 || index >= list.size()) {
                    actor.sendMessage("Index out of bounds: " + index, false);
                    return;
                }

                if (newIndex < 0 || newIndex >= list.size()) {
                    actor.sendMessage("New index out of bounds: " + newIndex, false);
                    return;
                }

                T value = list.get(index);
                list.remove(index);
                list.add(newIndex, value);
                actor.sendMessage("Moved element at index " + index + " to index " + newIndex, true);
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
            public String description() {
                return "Move element to a new index";
            }

            @Override
            public String usage() {
                return "<index> <newIndex>";
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
    protected String createUsage() {
        return "[args...]";
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
    protected String editUsage() {
        return "[args...]";
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
