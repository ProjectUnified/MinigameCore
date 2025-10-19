package io.github.projectunified.minigamecore.base;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The arena. The unit that handles the game
 */
public class Arena extends FeatureUnit implements Runnable {
    private final AtomicReference<Class<? extends GameState>> currentState = new AtomicReference<>();
    private final AtomicReference<Class<? extends GameState>> nextState = new AtomicReference<>();

    /**
     * Create a new arena
     *
     * @param parentList the parent {@link FeatureUnit} list
     */
    public Arena(List<FeatureUnit> parentList) {
        super(parentList);
    }

    /**
     * Create a new arena
     *
     * @param parent the parent {@link FeatureUnit}
     */
    public Arena(FeatureUnit... parent) {
        super(parent);
    }

    /**
     * Initialize the arena
     *
     * @see Initializer#init()
     */
    protected void initArena() {
        // Override this method to do something
    }

    /**
     * Post-initialize the arena
     *
     * @see Initializer#postInit()
     */
    protected void postInitArena() {
        // Override this method to do something
    }

    /**
     * Clear the arena
     *
     * @see Initializer#clear()
     */
    protected void clearArena() {
        // Override this method to do something
    }

    /**
     * Called when the arena's state is about to change.
     * This is usually used to perform actions or validations on state transitions.
     * Return false to cancel the state change (e.g., if you manually set a new state via {@link #setNextState(Class)}).
     *
     * @param oldStage the old state (may be null)
     * @param newStage the new state (may be null)
     * @return true if the change should proceed, false to cancel it
     */
    protected boolean callStateChanged(GameState oldStage, GameState newStage) {
        return true;
    }

    /**
     * Check if the arena is valid.
     * Mainly called when the arena is being registered to the arena manager.
     *
     * @return true if the arena is valid, otherwise false
     */
    public boolean isValid() {
        return true;
    }

    @Override
    protected List<GameState> loadGameStates() {
        return Collections.emptyList();
    }

    @Override
    protected List<Feature> loadFeatures() {
        return Collections.emptyList();
    }

    @Override
    public final void init() {
        super.init();
        initArena();
    }

    @Override
    public final void postInit() {
        super.postInit();
        postInitArena();
    }

    @Override
    public final void clear() {
        clearArena();
        super.clear();
    }

    @Override
    public final void run() {
        Optional<GameState> currentStateOptional = getCurrentStateInstance();
        Optional<GameState> nextStateOptional = getNextStateInstance();
        if (nextStateOptional.isPresent()) {
            GameState nextStateInstance = nextStateOptional.get();
            if (callStateChanged(currentStateOptional.orElse(null), nextStateInstance)) {
                currentState.set(nextStateInstance.getClass());
                nextState.set(null);
                currentStateOptional.ifPresent(gameState -> gameState.end(this));
                nextStateInstance.start(this);
                return;
            }
        }
        currentStateOptional.ifPresent(gameState -> gameState.update(this));
    }

    /**
     * Get the game state of the arena
     *
     * @return the class of the game state
     */
    public Class<? extends GameState> getCurrentState() {
        return this.currentState.get();
    }

    /**
     * Get the instance of the current game state of the arena
     *
     * @return the instance of the game state
     */
    public Optional<GameState> getCurrentStateInstance() {
        return Optional.ofNullable(getCurrentState()).map(this::getGameState);
    }

    /**
     * Get the next game state of the arena
     *
     * @return the class of the game state
     */
    public Class<? extends GameState> getNextState() {
        return this.nextState.get();
    }

    /**
     * Set the next game state of the arena.
     *
     * @param stateClass the class of the game state
     */
    public void setNextState(Class<? extends GameState> stateClass) {
        this.nextState.lazySet(stateClass);
    }

    /**
     * Get the instance of the next game state of the arena
     *
     * @return the instance of the game state
     */
    public Optional<GameState> getNextStateInstance() {
        return Optional.ofNullable(getNextState()).map(this::getGameState);
    }
}
