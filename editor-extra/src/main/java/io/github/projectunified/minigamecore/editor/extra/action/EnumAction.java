package io.github.projectunified.minigamecore.editor.extra.action;

import io.github.projectunified.minigamecore.editor.EditorAction;
import io.github.projectunified.minigamecore.editor.EditorActor;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link EditorAction} for enum value
 *
 * @param <T> the type of the enum
 */
public abstract class EnumAction<T extends Enum<T>> implements EditorAction {
    private final Class<T> enumClass;

    /**
     * Create a new action
     *
     * @param enumClass the enum class
     */
    public EnumAction(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    /**
     * Execute the action
     *
     * @param actor the actor
     * @param value the value
     * @param args  the arguments
     */
    public abstract void execute(EditorActor actor, T value, String[] args);

    @Override
    public String usage() {
        return "<value>";
    }

    @Override
    public List<String> complete(EditorActor actor, String[] args) {
        if (args.length == 1) {
            return Stream.of(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(EditorActor actor, String[] args) {
        if (args.length < 1) {
            actor.sendUsage(this);
            return;
        }
        try {
            T value = Enum.valueOf(enumClass, args[0].toUpperCase(Locale.ROOT));
            execute(actor, value, args);
        } catch (IllegalArgumentException e) {
            actor.sendMessage("Invalid value: " + args[0], false);
        }
    }
}
