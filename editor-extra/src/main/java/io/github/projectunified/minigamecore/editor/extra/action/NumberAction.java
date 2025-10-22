package io.github.projectunified.minigamecore.editor.extra.action;

import io.github.projectunified.minigamecore.editor.EditorAction;
import io.github.projectunified.minigamecore.editor.EditorActor;
import io.github.projectunified.minigamecore.editor.EditorString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The {@link EditorAction} for number value
 */
public abstract class NumberAction implements EditorAction {
    /**
     * The string of the usage of the number action
     */
    public static final EditorString USAGE = EditorString.of("action.number.usage", "<value>");
    /**
     * The string of the message when the actor uses an invalid value
     */
    public static final EditorString INVALID_VALUE = EditorString.of("action.enum.invalid_value", "Invalid value: %s");

    @Override
    public EditorString usage() {
        return USAGE;
    }

    @Override
    public List<String> complete(EditorActor actor, String[] args) {
        if (args.length == 1) {
            return valueComplete(actor).map(Objects::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Execute the action
     *
     * @param actor the actor
     * @param value the value
     * @param args  the arguments
     */
    public abstract void execute(EditorActor actor, Number value, String[] args);

    /**
     * Get number suggestions
     *
     * @param actor the actor
     * @return the suggestions
     */
    public Stream<? extends Number> valueComplete(EditorActor actor) {
        return IntStream.range(0, 10).boxed();
    }

    @Override
    public void execute(EditorActor actor, String[] args) {
        if (args.length < 1) {
            actor.sendUsage(this);
            return;
        }
        String value = args[0];
        try {
            execute(actor, Double.parseDouble(value), Arrays.copyOfRange(args, 1, args.length));
        } catch (NumberFormatException e) {
            actor.sendMessage(INVALID_VALUE, value);
        }
    }
}
