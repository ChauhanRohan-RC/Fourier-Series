package main.models.rotor;

import main.R;
import main.models.ColorHandler;
import main.models.function.RotorStatesFunction;
import main.models.function.provider.BaseFunctionProvider;
import main.models.function.provider.ColorProviderI;
import main.models.function.provider.DomainProviderI;
import main.models.function.provider.FunctionProviderI;
import main.util.Log;
import main.util.Wrapper;
import main.util.async.Async;
import main.util.async.CancellationProvider;
import main.util.async.Canceller;
import main.util.async.Consumer;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public interface RotorStateManager extends RotorFrequencyProvider, RotorStateProvider, DomainProviderI, ColorHandler {

    String TAG = "RotorStateManager";
    int ROTOR_STATES_SAVE_PRECISION = 3;
    String ROTOR_STATE_SAVE_STRING_FORMAT = "%." + ROTOR_STATES_SAVE_PRECISION + "f";

    String ROTOR_STATE_SAVE_FREQ_TO_COEFF_DELIMITER = ":";   // frequency to coefficient delimiter
    String ROTOR_STATE_SAVE_COEFF_DELIMITER = ",";           // coefficient real and imaginary part delimiter
    Charset ROTOR_STATES_SAVE_ENC = StandardCharsets.UTF_8;

    interface Listener {

        void onRotorsLoadingChanged(boolean isLoading);

        void onRotorsLoadFinished(@NotNull RotorStateProvider manager, int count, boolean cancelled);

        void onRotorCountChanged(@NotNull RotorStateProvider manager, int prevCount, int newCount);
    }


    boolean isNoOp();

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

    void addListener(@NotNull Listener l);

    boolean removeListener(@NotNull Listener l);

    boolean hasListener(@NotNull Listener l);


    boolean isLoading();

    void cancelLoad(boolean interrupt);

    void loadSync(int count, @Nullable CancellationProvider c);

    @Nullable
    Canceller loadAsync(int count);

    void setRotorCountSync(int count, @Nullable CancellationProvider c);

    @Nullable
    Canceller setRotorCountAsync(int count);

    double getAllRotorsMagnitudeScaleSum();



    /* Dump Rotor States */


    @NotNull
    default CharSequence dumpRotorStates(@Nullable String funcName) {
        if (R.isEmpty(funcName)) {
            funcName = R.DISPLAY_NAME_FUNCTION_UNKNOWN;
        }

        final int count = getRotorCount();

        final StringBuilder sb = new StringBuilder();
        sb.append("# .......................  Rotor States  .......................\n\n")
                .append("# Save Time: ")
                .append(new Date().toString())
                .append("\n")
                .append("# Function: ")
                .append(funcName)
                .append("\n")
                .append("# Domain Start: ")
                .append(getDomainStart())
                .append("\n")
                .append("# Domain End: ")
                .append(getDomainEnd())
                .append("\n")
                .append("# Rotors Count: ")
                .append(count)
                .append("\n\n")
                .append("# Rotor States (Frequency ")
                .append(ROTOR_STATE_SAVE_FREQ_TO_COEFF_DELIMITER)
                .append(" magnitude")
                .append(ROTOR_STATE_SAVE_COEFF_DELIMITER)
                .append(" phase)")
                .append("\n");

        for (int i=0; i < count; i++) {
            RotorState state = getRotorState(i);

            sb.append("\n")
                    .append(String.format(ROTOR_STATE_SAVE_STRING_FORMAT, state.getFrequency()))
                    .append(' ').append(ROTOR_STATE_SAVE_FREQ_TO_COEFF_DELIMITER).append(' ')          // Delimiter 1
                    .append(String.format(ROTOR_STATE_SAVE_STRING_FORMAT, state.getMagnitudeScale()))
                    .append(ROTOR_STATE_SAVE_COEFF_DELIMITER).append(' ')                               // Delimiter 2
                    .append(String.format(ROTOR_STATE_SAVE_STRING_FORMAT, state.getCoefficientArgument()));
        }

        return sb;
    }


    @Nullable
    static FunctionProviderI loadRotorStatesFunction(@NotNull String s, @NotNull String defaultName) {
        if (R.isEmpty(s))
            return null;

        final Wrapper<String> name = new Wrapper<>(null);
        final Wrapper.Doub dStart = new Wrapper.Doub(0), dEnd = new Wrapper.Doub(1);
        final List<RotorState> states = new LinkedList<>();

        s.lines().forEachOrdered(line -> {
            line = R.removeAllWhiteSpaces(line);

            int comment_token_i = line.indexOf('#');
            if (comment_token_i != -1) {
                final String commented = line.substring(comment_token_i + 1);
                if (R.notEmpty(commented)) {
                    final int idx = commented.indexOf(':');
                    if (idx != -1) {
                        final String[] meta = commented.split(":");
                        if (meta != null && meta.length == 2 && R.notEmpty(meta[0])) {
                            switch (meta[0].toLowerCase()) {
                                case "function" -> name.set(meta[1]);
                                case "domainstart" -> {
                                    try {
                                        dStart.set(Double.parseDouble(meta[1]));
                                    } catch (Throwable ignored) {
                                    }
                                } case "domainend" -> {
                                    try {
                                        dEnd.set(Double.parseDouble(meta[1]));
                                    } catch (Throwable ignored) {
                                    }
                                }
                            }
                        }
                    }
                }

                line = line.substring(0, comment_token_i);
            }

//            line = line.replaceAll("\n", "");

            if (!line.isEmpty()) {
                final String[] dat = line.split(ROTOR_STATE_SAVE_FREQ_TO_COEFF_DELIMITER);
                String[] coeff;

                if (dat != null && dat.length == 2 && R.notEmpty(dat[0]) && R.notEmpty(dat[1]) && (coeff = dat[1].split(ROTOR_STATE_SAVE_COEFF_DELIMITER)) != null && coeff.length == 2) {
                    try {

                        states.add(new RotorState(Double.parseDouble(dat[0]), Complex.polar(Double.parseDouble(coeff[0]), Double.parseDouble(coeff[1])), 1));
                    } catch (Throwable ignored) {
                        Log.e("Failed to parse rotor state: " + line);
                    }
                } else {
                    Log.e("Failed to parse rotor state: " + line);
                }
            }
        });

        if (states.isEmpty())
            return null;

        if (R.isEmpty(name.get())) {
            name.set(defaultName);
        }

        Log.d(TAG, String.format("Loaded Rotor States Function -> Name: %s, Domain Start: %f, Domain End: %f", name.get(), dStart.get(), dEnd.get()));

        return new BaseFunctionProvider("Loaded (" + name.get() + ")", () -> new RotorStatesFunction(states, dStart.get(), dEnd.get()));
    }


    @Nullable
    default Path dumpRotorStatesToFile(@Nullable String funcName) {
        final Path file = R.createRotorStatesDumpFile(funcName);
        if (file == null)
            return null;

        try {
            Files.writeString(file, dumpRotorStates(funcName), ROTOR_STATES_SAVE_ENC);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to dump rotor states of FUNCTION <" + (R.isEmpty(funcName)? R.DISPLAY_NAME_FUNCTION_UNKNOWN: funcName) + "> to FILE <" + file.toString() + ">", t);
            return null;
        }

        return file;
    }

    default void dumpRotorStatesToFileAsync(@Nullable String funcName, @Nullable Consumer<Path> callback) {
        Async.execute(this::dumpRotorStatesToFile, callback, funcName);
    }

    @Nullable
    static FunctionProviderI loadFunctionFromRotorStatesFile(@NotNull Path file) {
        if (Files.isRegularFile(file)) {
            try {
                final String s = Files.readString(file, ROTOR_STATES_SAVE_ENC);
                final FunctionProviderI fp = loadRotorStatesFunction(s, R.getName(file.getFileName().toString()));
                if (fp == null) {
                    throw new Exception("Parse Error");
                }

                return fp;
            } catch (Throwable t) {
                Log.e(TAG, "Failed to load Rotor States from file <" + file.toString() + ">", t);
            }
        }

        return null;
    }

    static void loadFunctionFromRotorStatesFileAsync(@NotNull Path file, @NotNull Consumer<FunctionProviderI> callback) {
        Async.execute(RotorStateManager::loadFunctionFromRotorStatesFile, callback, file);
    }






    class NoOp implements RotorStateManager {

        @Override
        public boolean isNoOp() {
            return true;
        }

        @Override
        public double getRotorFrequency(int index, int count) {
            return 0;
        }

        @Override
        @NotNull
        public RotorState getRotorState(int index) {
            return RotorState.ZERO;
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
        public double getDomainStart() {
            return 0;
        }

        @Override
        public double getDomainEnd() {
            return 0;
        }

        @Override
        public double getDomainRangeTravelMsDefault() {
            return 0;
        }

        @Override
        public double getDomainRangeTravelMsMin() {
            return 0;
        }

        @Override
        public double getDomainRangeTravelMsMax() {
            return 0;
        }

        @Override
        public void addListener(@Nullable RotorStateManager.Listener l) { }

        @Override
        public boolean removeListener(@NotNull RotorStateManager.Listener l) {
            return false;
        }

        @Override
        public boolean hasListener(@NotNull RotorStateManager.Listener l) {
            return false;
        }


        @Override
        public @Nullable Canceller setRotorCountAsync(int count) {
            return null;
        }

        @Override
        public void setRotorCountSync(int count, @Nullable CancellationProvider c) {

        }

        @Override
        public boolean isLoading() {
            return false;
        }

        @Override
        public void cancelLoad(boolean interrupt) { }

        @Override
        public void loadSync(int count, @Nullable CancellationProvider c) {

        }

        @Override
        public @Nullable Canceller loadAsync(int count) {
            return null;
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
