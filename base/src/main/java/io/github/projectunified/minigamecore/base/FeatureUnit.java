package io.github.projectunified.minigamecore.base;

import java.util.*;

/**
 * A unit to handle the arena {@link Feature} and {@link GameState}
 */
public abstract class FeatureUnit implements Initializer {
    private final Map<Class<? extends GameState>, GameState> gameStateMap = new IdentityHashMap<>();
    private final Map<Class<? extends Feature>, Feature> featureMap = new IdentityHashMap<>();
    private final List<Feature> features = new ArrayList<>();
    private final List<GameState> gameStates = new ArrayList<>();
    private final List<FeatureUnit> parentList;

    /**
     * Create a new {@link FeatureUnit}
     *
     * @param parentList the parent {@link FeatureUnit} list
     */
    public FeatureUnit(List<FeatureUnit> parentList) {
        this.parentList = Collections.unmodifiableList(new ArrayList<>(parentList));
    }

    /**
     * Create a new {@link FeatureUnit}
     *
     * @param parent the parent {@link FeatureUnit}
     */
    public FeatureUnit(FeatureUnit... parent) {
        this.parentList = parent.length == 0 ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(Arrays.asList(parent)));
    }

    private static <T> Set<Class<? extends T>> getSuperClasses(Class<T> baseClass, Class<? extends T> childClass) {
        Set<Class<? extends T>> classSet = new HashSet<>();
        classSet.add(childClass);

        Queue<Class<? extends T>> classQueue = new LinkedList<>();
        classQueue.add(childClass);
        while (true) {
            Class<? extends T> currentClass = classQueue.poll();
            if (currentClass == null) {
                break;
            }

            List<Class<?>> superClasses = new ArrayList<>(Arrays.asList(currentClass.getInterfaces()));
            Optional.ofNullable(currentClass.getSuperclass()).ifPresent(superClasses::add);

            for (Class<?> superClassOrInterface : superClasses) {
                if (baseClass.isAssignableFrom(superClassOrInterface)) {
                    Class<? extends T> superClassOrInterfaceSubClass = superClassOrInterface.asSubclass(baseClass);
                    if (classSet.add(superClassOrInterfaceSubClass)) {
                        classQueue.add(superClassOrInterfaceSubClass);
                    }
                }
            }
        }
        return classSet;
    }

    /**
     * Load the game states
     *
     * @return the game states
     */
    protected abstract List<GameState> loadGameStates();

    /**
     * Load the features
     *
     * @return the features
     */
    protected abstract List<Feature> loadFeatures();

    /**
     * Get the parent {@link FeatureUnit}.
     * It takes the first element from the result of {@link #getParentList()}.
     * If the result is empty, it will return null
     *
     * @return the parent {@link FeatureUnit} or null if not present
     */
    public FeatureUnit getParent() {
        return parentList.isEmpty() ? null : parentList.get(0);
    }

    /**
     * Get the parent {@link FeatureUnit} list
     *
     * @return the parent {@link FeatureUnit} list
     */
    @SuppressWarnings("unused")
    public List<FeatureUnit> getParentList() {
        return parentList;
    }

    @Override
    public void init() {
        this.gameStates.addAll(loadGameStates());
        gameStates.forEach(gameState -> getSuperClasses(GameState.class, gameState.getClass()).forEach(clazz -> gameStateMap.put(clazz, gameState)));

        this.features.addAll(loadFeatures());
        features.forEach(feature -> getSuperClasses(Feature.class, feature.getClass()).forEach(clazz -> featureMap.put(clazz, feature)));

        features.forEach(Initializer::init);
        gameStates.forEach(Initializer::init);
    }

    @Override
    public void postInit() {
        features.forEach(Initializer::postInit);
        gameStates.forEach(Initializer::postInit);
    }

    @Override
    public void clear() {
        for (int i = gameStates.size() - 1; i >= 0; i--) {
            gameStates.get(i).clear();
        }
        for (int i = features.size() - 1; i >= 0; i--) {
            features.get(i).clear();
        }
        featureMap.clear();
        gameStateMap.clear();
        features.clear();
        gameStates.clear();
    }

    /**
     * Get the instance of the game state
     *
     * @param gameStateClass the class of the game state
     * @param <T>            the type of the game state
     * @return the instance of the game state
     */
    public <T extends GameState> T getGameState(Class<T> gameStateClass) {
        GameState gameState = gameStateMap.get(gameStateClass);
        T checkedGameState = gameStateClass.isInstance(gameState) ? gameStateClass.cast(gameState) : null;
        if (checkedGameState != null) {
            return checkedGameState;
        }

        for (FeatureUnit parent : parentList) {
            checkedGameState = parent.getGameState(gameStateClass);
            if (checkedGameState != null) {
                return checkedGameState;
            }
        }

        return null;
    }

    /**
     * Get the instance of the feature
     *
     * @param featureClass the class of the feature
     * @param <T>          the type of the feature
     * @return the instance of the feature
     */
    @SuppressWarnings("unused")
    public <T extends Feature> T getFeature(Class<T> featureClass) {
        Feature feature = featureMap.get(featureClass);
        T checkedFeature = featureClass.isInstance(feature) ? featureClass.cast(feature) : null;
        if (checkedFeature != null) {
            return checkedFeature;
        }

        for (FeatureUnit parent : parentList) {
            checkedFeature = parent.getFeature(featureClass);
            if (checkedFeature != null) {
                return checkedFeature;
            }
        }

        return null;
    }

    /**
     * Get all classes of the game states
     *
     * @param deep if true, it will get all classes of the game states from the parent
     * @return the classes
     */
    @SuppressWarnings("unused")
    public Set<Class<? extends GameState>> getGameStates(boolean deep) {
        Set<Class<? extends GameState>> classes = new HashSet<>(gameStateMap.keySet());
        if (deep) {
            for (FeatureUnit parent : parentList) {
                classes.addAll(parent.getGameStates(true));
            }
        }
        return Collections.unmodifiableSet(classes);
    }

    /**
     * Get all classes of the features
     *
     * @param deep if true, it will get all classes of the features from the parent
     * @return the classes
     */
    @SuppressWarnings("unused")
    public Set<Class<? extends Feature>> getFeatures(boolean deep) {
        Set<Class<? extends Feature>> classes = new HashSet<>(featureMap.keySet());
        if (deep) {
            for (FeatureUnit parent : parentList) {
                classes.addAll(parent.getFeatures(true));
            }
        }
        return Collections.unmodifiableSet(classes);
    }
}
