package main.models.rotor;

import main.models.ColorHandler;
import main.models.function.provider.ColorProviderI;
import main.models.function.provider.DomainProviderI;
import main.util.async.CancellationProvider;
import main.util.async.Canceller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RotorStateManager extends RotorFrequencyProvider, RotorStateProvider, DomainProviderI, ColorHandler {

    interface Listener {

        void onRotorsLoadFinished(@NotNull RotorStateProvider manager, int count, boolean cancelled);

        void onRotorCountChanged(@NotNull RotorStateProvider manager, int prevCount, int newCount);
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



    class NoOp implements RotorStateManager {

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
