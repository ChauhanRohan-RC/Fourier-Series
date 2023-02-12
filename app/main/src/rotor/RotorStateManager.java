package rotor;

import animation.animator.AbstractAnimator;
import function.definition.ColorHandler;
import function.definition.ColorProviderI;
import function.definition.ComplexDomainFunctionI;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.FunctionMeta;
import provider.FunctionType;
import rotor.frequency.RotorFrequencyProviderI;
import async.CancellationProvider;
import async.Consumer;

import java.util.*;

public interface RotorStateManager extends RotorFrequencyProviderI, RotorStateProvider, ColorHandler {

    String TAG = "RotorStateManager";
//    String ROTOR_STATE_SAVE_FREQ_TO_COEFF_DELIMITER = ":";   // frequency to coefficient delimiter
//    String ROTOR_STATE_SAVE_COEFF_DELIMITER = ",";           // coefficient real and imaginary part delimiter

    enum LoadCallResult {

        /**
         * Load Queued
         * */
        QUEUED,

        /**
         * Load intercepted by any one of the registered interceptors
         * */
        INTERCEPTED,

        /**
         * Load not required, due to Rotor States already loaded, or a bigger load is ongoing
         * */
        REDUNDANT

    }

    interface Listener {

        /**
         * Called before loading rotor states.
         * Can be called on any thread
         *
         * @return true to cancel load, false to continue with this load
         * */
        default boolean onInterceptRotorsLoad(@NotNull RotorStateManager manager, int loadCount) {
            return false;
        }

        void onRotorsLoadIntercepted(@NotNull RotorStateManager manager, int loadCount);

        void onRotorsLoadingChanged(@NotNull RotorStateManager manager, boolean isLoading);

        void onRotorsLoadFinished(@NotNull RotorStateManager manager, int count, boolean cancelled);

        void onRotorsCountChanged(@NotNull RotorStateManager manager, int prevCount, int newCount);


        default boolean onInterceptRotorFrequencyProvider(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new) {
            return false;
        }

        void onRotorsFrequencyProviderIntercepted(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI rotorFrequencyProvider);

        /**
         * Called when rotor frequency provider is changed.
         * if The old provider is null, it means that the manager was previously using its default provider.
         *
         * @param manager the states manager
         * @param old old rotor frequency provider
         * @param _new new rotor frequency provider
         * */
        void onRotorsFrequencyProviderChanged(@NotNull RotorStateManager manager, @Nullable RotorFrequencyProviderI old, @Nullable RotorFrequencyProviderI _new);
    }


    int getId();

    @NotNull
    FunctionMeta getFunctionMeta();

    @NotNull
    ComplexDomainFunctionI getFunction();

    default boolean isNoOp() {
        return getFunctionMeta().functionType() == FunctionType.NO_OP || getFunction().isNoop();
    }

    int getDefaultInitialRotorCount();

    /**
     * @return current rotor count
     * */
    int getRotorCount();

    /**
     * @return pending rotor count if loading, or -1 if not loading
     * */
    int getPendingRotorCount();

    void considerInitialize();

    /**
     * @return default frequency provider for this manager
     * */
    @NotNull
    RotorFrequencyProviderI getManagerDefaultRotorFrequencyProvider();

    /**
     * @return frequency provider set by {@link #setRotorFrequencyProvider(RotorFrequencyProviderI)}
     * */
    @Nullable
    RotorFrequencyProviderI getManagerRotorFrequencyProvider();

    @NotNull
    default RotorFrequencyProviderI getManagerRotorFrequencyProviderOrDefault() {
        RotorFrequencyProviderI fp = getManagerRotorFrequencyProvider();
        if (fp == null) {
            fp = getManagerDefaultRotorFrequencyProvider();
        }

        return fp;
    }

    @Override
    default double getRotorFrequency(int index, int count) {
        return getManagerRotorFrequencyProviderOrDefault().getRotorFrequency(index, count);
    }

    /**
     * sets the rotor Frequency Provider for this manager. Passing {@code null} will cause the manager
     * to use its default frequency provider given by {@link #getManagerDefaultRotorFrequencyProvider()}
     *
     * @param rotorFrequencyProvider new frequency provider, or {@code null} to use deafult provider
     *
     * @see #getManagerDefaultRotorFrequencyProvider()
     * */
    void setRotorFrequencyProvider(@Nullable RotorFrequencyProviderI rotorFrequencyProvider);


    @Nullable
    default AbstractAnimator.RepeatMode getDefaultRepeatMode() {
        final FunctionType ft = getFunctionMeta().functionType();

        if (ft == FunctionType.INTERNAL_PATH || ft == FunctionType.EXTERNAL_PATH) {
            return AbstractAnimator.RepeatMode.END;
        }

        return null;
    }

    void forEachRotorState(@NotNull Consumer<RotorState> consumer);

    default void copyAllRotorStates(@NotNull Collection<RotorState> dest) {
        forEachRotorState(dest::add);
    }

    default void copyAllRotorStates(@NotNull Map<? super Double, ? super RotorState> dest) {
        forEachRotorState(state -> dest.put(state.getFrequency(), state));
    }

    @NotNull
    default Map<Double, RotorState> getRotorStatesMapCopy() {
        final Map<Double, RotorState> map = new HashMap<>();
        copyAllRotorStates(map);
        return map;
    }

    @Nullable
    default List<RotorState> getRotorStatesListCopy() {
        final ArrayList<RotorState> states = new ArrayList<>();
        copyAllRotorStates(states);
        return states;
    }

    @Nullable
    default List<RotorState> getSortedRotorStates(@Nullable Comparator<? super RotorState> comparator) {
        final ArrayList<RotorState> states = new ArrayList<>();
        copyAllRotorStates(states);
        states.sort(comparator);
        return states;
    }

    void addListener(@NotNull Listener l);

    boolean removeListener(@NotNull Listener l);

    boolean containsListener(@NotNull Listener l);

    default void ensureListener(@NotNull Listener l) {
        if (!containsListener(l)) {
            addListener(l);
        }
    }

    boolean isLoading();

    void cancelLoad(boolean interrupt);

    void loadSync(int count, @Nullable CancellationProvider c, @Nullable Consumer<LoadCallResult> callResultCallback);

    @NotNull
    LoadCallResult loadAsync(int count);

    void setRotorCountSync(int count, @Nullable CancellationProvider c, @Nullable Consumer<LoadCallResult> callResultCallback);

    @NotNull
    LoadCallResult setRotorCountAsync(int count);

    void reloadAsync();

    double getAllRotorsMagnitudeScaleSum();

    int getAllLoadedRotorStatesCount();

    /***
     * Clears all loaded rotor states and reset to uninitialised state
     * */
    void clearAndResetSync();

    default void clearAndReloadAsync() {
        final int rotorCount = getRotorCount();
        clearAndResetSync();
        setRotorCountAsync(rotorCount);
    }


    /**
     * @return modCount
     * */
    int addRotorStates(Collection<RotorState> states);


    /* Function State */

    @NotNull
    default FunctionState createFunctionState(@Nullable String funcName) {
//        ComplexDomainFunctionI function = null;
//        if (this instanceof ComplexDomainFunctionI func) {
//            function = ComplexUtil.getBaseFunction(func);
//        }

        return FunctionState.from(this, funcName);
    }

    @NotNull
    default FunctionState createFunctionState() {
        return createFunctionState(null);
    }


//    default void dumpFunctionStateFileAsync(@NotNull Path file, boolean serializeFunction, @Nullable TaskConsumer<Path> callback) {
//        dumpFunctionStateFileAsync(file, null, serializeFunction, callback);
//    }
//
//    default void dumpFunctionStateFileAsync(@NotNull Path file, @Nullable String funcName, boolean serializeFunction, @Nullable TaskConsumer<Path> callback) {
//        Async.execute(() -> dumpFunctionStateToFile(file, funcName, serializeFunction), callback);
//    }
//
//    default Path dumpFunctionStateToFile(@NotNull Path file, boolean serializeFunction) throws IOException, JsonParseException {
//        return dumpFunctionStateToFile(file, null, serializeFunction);
//    }

//    default FunctionState dumpFunctionStateToFile(@NotNull Path file, @Nullable String funcName, boolean serializeFunction) throws IOException, JsonParseException {
//        try {
//
//        } catch (Throwable t) {
//            Log.e(TAG, "Failed to dump function state of FUNCTION <" + (Format.isEmpty(funcName)? R.DISPLAY_NAME_FUNCTION_UNKNOWN: funcName) + "> to FILE <" + file + ">", t);
//            return null;
//        }
//
//        return file;
//    }

//    default FunctionState dumpFunctionState(@NotNull Appendable writer, @Nullable String funcName, boolean serializeFunction) throws JsonParseException {
//        final FunctionState state = createFunctionState(funcName, serializeFunction);
//        state.writeJson(writer);
//        return state;
//    }


//    @NotNull
//    default CharSequence dumpFunctionState(boolean serializeFunction) throws JsonParseException {
//        return dumpFunctionState(null, serializeFunction);
//    }
//

//    @NotNull
//    default FunctionStateDump dumpFunctionState(@Nullable String funcName, boolean serializeFunction) throws JsonParseException {
//        final FunctionState functionState = createFunctionState(funcName, serializeFunction);
//        return
//        Json.get().gson.toJson(functionState, FunctionState.class, );

//        final List<RotorState> states = getSortedRotorStates(RotorState.COMPARATOR_FREQ_ASC);
//        final int count = CollectionUtil.size(states);
//
////        final int count = getRotorCount();
//
//        final StringBuilder sb = new StringBuilder();
//        sb.append(R.COMMENT_TOKEN)
//                .append(" .......................  Rotor States  .......................\n\n")
//                .append(R.COMMENT_TOKEN)
//                .append(" Save Time: ")
//                .append(new Date())
//                .append("\n")
//                .append(R.COMMENT_TOKEN)
//                .append(" Function: ")
//                .append(funcName)
//                .append("\n")
//                .append(R.COMMENT_TOKEN)
//                .append(" Domain Start: ")
//                .append(getDomainStart())
//                .append("\n")
//                .append(R.COMMENT_TOKEN)
//                .append(" Domain End: ")
//                .append(getDomainEnd())
//                .append("\n")
//                .append(R.COMMENT_TOKEN)
//                .append(" Domain Travel Time (ms-min): ")
//                .append(getDomainAnimationDurationMsMin())
//                .append("\n")
//                .append(R.COMMENT_TOKEN)
//                .append(" Domain Travel Time (ms-max): ")
//                .append(getDomainAnimationDurationMsMax())
//                .append("\n")
//                .append(R.COMMENT_TOKEN)
//                .append(" Domain Travel Time (ms-default): ")
//                .append(getDomainAnimationDurationMsDefault())
//                .append("\n")
//                .append(R.COMMENT_TOKEN)
//                .append(" Rotors Count: ")
//                .append(count)
//                .append("\n\n")
//                .append(R.COMMENT_TOKEN)
//                .append(" Rotor States (Frequency ")
//                .append(ROTOR_STATE_SAVE_FREQ_TO_COEFF_DELIMITER)
//                .append(" magnitude")
//                .append(ROTOR_STATE_SAVE_COEFF_DELIMITER)
//                .append(" phase)")
//                .append("\n");
//
//        if (CollectionUtil.notEmpty(states)) {
//            for (RotorState state: states) {
//                sb.append("\n")
//                        .append(state.getFrequency())
//                        .append("   ")
//                        .append(ROTOR_STATE_SAVE_FREQ_TO_COEFF_DELIMITER)            // Delimiter 1
//                        .append("   ")
//                        .append(state.getMagnitudeScale())
//                        .append(ROTOR_STATE_SAVE_COEFF_DELIMITER)                    // Delimiter 2
//                        .append("  ")
//                        .append(state.getCoefficientArgument());
//            }
//        }
//
//        return sb;
//    }


    /* Load Function State */

//    @Nullable
//    static FunctionProviderI loadFunctionState(@NotNull Reader json, @NotNull String defaultName) {
//        try {
//            return Json.get().gson.fromJson(json, FunctionState.class).toProvider(defaultName);
//        } catch (Throwable t) {
//            Log.e(TAG, "Failed to load Function STate from json", t);
//        }
//
//        return null;


//        final Wrapper<String> name = new Wrapper<>(null);
//        final Wrapper.Doub
//                dStart = new Wrapper.Doub(0),
//                dEnd = new Wrapper.Doub(1);
//
//        final Wrapper.Long msDef = new Wrapper.Long(-1),
//                msMin = new Wrapper.Long(-1),
//                msMax = new Wrapper.Long(-1);
//
//        final Map<Double, RotorState> states = new HashMap<>();
//
//        json.lines().forEachOrdered(line -> {
//            line = Format.removeAllWhiteSpaces(line);
//
//            int comment_token_i = line.indexOf(R.COMMENT_TOKEN);
//            if (comment_token_i != -1) {
//                final String commented = line.substring(comment_token_i + 1);
//                if (Format.notEmpty(commented)) {
//                    final int idx = commented.indexOf(':');
//                    if (idx != -1) {
//                        final String[] meta = commented.split(":");
//                        if (meta != null && meta.length == 2 && Format.notEmpty(meta[0])) {
//                            switch (meta[0].toLowerCase()) {
//                                case "function" -> name.set(meta[1]);
//                                case "domainstart" -> {
//                                    try {
//                                        dStart.set(Double.parseDouble(meta[1]));
//                                    } catch (Throwable ignored) {
//                                    }
//                                } case "domainend" -> {
//                                    try {
//                                        dEnd.set(Double.parseDouble(meta[1]));
//                                    } catch (Throwable ignored) {
//                                    }
//                                } case "domaintraveltime(ms-default)" -> {
//                                    try {
//                                        msDef.set(Long.parseLong(meta[1]));
//                                    } catch (Throwable ignored) {
//                                    }
//                                } case "domaintraveltime(ms-min)" -> {
//                                    try {
//                                        msMin.set(Long.parseLong(meta[1]));
//                                    } catch (Throwable ignored) {
//                                    }
//                                } case "domaintraveltime(ms-max)" -> {
//                                    try {
//                                        msMax.set(Long.parseLong(meta[1]));
//                                    } catch (Throwable ignored) {
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                line = line.substring(0, comment_token_i);
//            }
//
////            line = line.replaceAll("\n", "");
//
//            if (!line.isEmpty()) {
//                final String[] dat = line.split(ROTOR_STATE_SAVE_FREQ_TO_COEFF_DELIMITER);
//                String[] coeff;
//
//                if (dat != null && dat.length == 2 && Format.notEmpty(dat[0]) && Format.notEmpty(dat[1]) && (coeff = dat[1].split(ROTOR_STATE_SAVE_COEFF_DELIMITER)) != null && coeff.length == 2) {
//                    try {
//                        final double freq = Double.parseDouble(dat[0]);
//                        final Complex fsCoeff = ComplexUtil.polar(Double.parseDouble(coeff[0]), Double.parseDouble(coeff[1]));
//
//                        states.put(freq, new RotorState(freq, fsCoeff));
//                    } catch (Throwable ignored) {
//                        Log.e("Failed to parse rotor state: " + line);
//                    }
//                } else {
//                    Log.e("Failed to parse rotor state: " + line);
//                }
//            }
//        });
//
//        if (states.isEmpty())
//            return null;
//
//        if (Format.isEmpty(name.get())) {
//            name.set(defaultName);
//        }
//
//        Log.d(TAG, String.format("Loaded Rotor States Function -> Name: %s, Domain Start: %f, Domain End: %f", name.get(), dStart.get(), dEnd.get()));
//
//        final FunctionMeta meta = new FunctionMeta(
//                FunctionType.EXTERNAL_ROTOR_STATE,
//                R.createExternalRotorStatesFunctionDisplayTitle(name.get()),
//                states.size(),
//                Collections.unmodifiableMap(states)
//        );
//
//        return new BaseFunctionProvider(meta, () -> new RotorStatesFunction(states.values(), dStart.get(), dEnd.get(), msDef.get(), msMin.get(), msMax.get()));
//    }


//    @Nullable
//    static FunctionProviderI loadFunctionStateFile(@NotNull Path file) {
//        if (Files.isRegularFile(file)) {
//
//            try (Reader reader = Files.newBufferedReader(file, R.ENCODING)) {
//                return loadFunctionState(reader, file.getFileName().toString());
//            } catch (Throwable t) {
//                Log.e(TAG, "Failed to load Function State from file <" + file + ">", t);
//            }
//        }
//
//        return null;
//    }
//
//    static void loadFunctionStateFileAsync(@NotNull Path file, @NotNull Consumer<FunctionProviderI> callback) {
//        Async.execute(RotorStateManager::loadFunctionStateFile, callback, file);
//    }





    /* No-op Manager */

    class NoOp implements RotorStateManager {

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public @NotNull FunctionMeta getFunctionMeta() {
            return FunctionMeta.NOOP;
        }

        @Override
        @NotNull
        public ComplexDomainFunctionI getFunction() {
            return ComplexDomainFunctionI.NOOP;
        }

        @Override
        public boolean isNoOp() {
            return true;
        }

        @Override
        public @NotNull RotorFrequencyProviderI getManagerDefaultRotorFrequencyProvider() {
            return ComplexDomainFunctionI.getDefaultFrequencyProvider(1);
        }

        @Override
        public @Nullable RotorFrequencyProviderI getManagerRotorFrequencyProvider() {
            return null;
        }

        @Override
        public void setRotorFrequencyProvider(@Nullable RotorFrequencyProviderI rotorFrequencyProvider) {

        }

        @Override
        @NotNull
        public RotorState getRotorState(int index) {
            return RotorState.ZERO;
        }

        @Override
        public void forEachRotorState(@NotNull Consumer<RotorState> consumer) {
            /* no-op */
        }

        @Override
        public int getDefaultInitialRotorCount() {
            return 0;
        }

        @Override
        public int getRotorCount() {
            return 0;
        }

        @Override
        public int getPendingRotorCount() {
            return -1;
        }

        @Override
        public double getAllRotorsMagnitudeScaleSum() {
            return 0;
        }

        @Override
        public void considerInitialize() {

        }

        @Override
        public void reloadAsync() {
        }

//        @Override
//        public double getDomainStart() {
//            return 0;
//        }
//
//        @Override
//        public double getDomainEnd() {
//            return 0;
//        }
//
//        @Override
//        public long getDomainAnimationDurationMsDefault() {
//            return 0;
//        }
//
//        @Override
//        public long getDomainAnimationDurationMsMin() {
//            return 0;
//        }
//
//        @Override
//        public long getDomainAnimationDurationMsMax() {
//            return 0;
//        }

        @Override
        public void addListener(@Nullable RotorStateManager.Listener l) { }

        @Override
        public boolean removeListener(@NotNull RotorStateManager.Listener l) {
            return false;
        }

        @Override
        public boolean containsListener(@NotNull RotorStateManager.Listener l) {
            return false;
        }


        @Override
        public boolean isLoading() {
            return false;
        }

        @Override
        public void cancelLoad(boolean interrupt) { }

        @Override
        public void loadSync(int count, @Nullable CancellationProvider c, @Nullable Consumer<LoadCallResult> callResultCallback) {
            if (callResultCallback != null) {
                callResultCallback.consume(LoadCallResult.REDUNDANT);
            }
        }

        @Override
        public @NotNull LoadCallResult loadAsync(int count) {
            return LoadCallResult.REDUNDANT;
        }

        @Override
        public void setRotorCountSync(int count, @Nullable CancellationProvider c, @Nullable Consumer<LoadCallResult> callResultCallback) {
            if (callResultCallback != null) {
                callResultCallback.consume(LoadCallResult.REDUNDANT);
            }
        }

        @Override
        public @NotNull LoadCallResult setRotorCountAsync(int count) {
            return LoadCallResult.REDUNDANT;
        }

        @Override
        public int getAllLoadedRotorStatesCount() {
            return 0;
        }

        @Override
        public void clearAndResetSync() {

        }

        @Override
        public int addRotorStates(Collection<RotorState> states) {
            return 0;       // noop
        }

        @Override
        public @Nullable ColorProviderI getColorProvider() {
            return null;
        }

        @Override
        public ColorHandler setColorProvider(@Nullable ColorProviderI colorProvider) {
            return this;
        }

        @Override
        public ColorHandler hueCycle(float hueStart, float hueEnd) {
            return this;
        }
    }
}
