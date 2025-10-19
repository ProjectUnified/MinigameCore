package io.github.projectunified.minigamecore.editor.extra.action;

import io.github.projectunified.minigamecore.editor.EditorAction;
import io.github.projectunified.minigamecore.editor.EditorActor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The {@link EditorAction} for boolean value
 */
public abstract class BooleanAction implements EditorAction {
    @Override
    public String usage() {
        return "<true|false>";
    }

    @Override
    public List<String> complete(EditorActor actor, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("true", "false");
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
    public abstract void execute(EditorActor actor, boolean value, String[] args);

    @Override
    public void execute(EditorActor actor, String[] args) {
        if (args.length < 1) {
            actor.sendUsage(this);
            return;
        }
        execute(actor, Boolean.parseBoolean(args[0]), Arrays.copyOfRange(args, 1, args.length));
    }
}
