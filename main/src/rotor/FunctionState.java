package rotor;

import app.R;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import function.RotorStatesFunction;
import function.definition.DomainProviderI;
import json.Json;
import json.JsonParsable;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.*;
import util.Format;
import util.main.ComplexUtil;

import java.lang.reflect.Type;
import java.util.*;

public class FunctionState {

    public static final Comparator<Double> FREQUENCY_COMPARATOR = Double::compare;             // ascending frequencies

    public record RotorCoefficient(double magnitude, double phase) {

        public RotorCoefficient(@NotNull Complex complex) {
            this(complex.abs(), complex.getArgument());
        }

        public RotorCoefficient(@NotNull RotorState state) {
            this(state.getMagnitudeScale(), state.getCoefficientArgument());
        }

        @NotNull
        public Complex toComplex() {
            return ComplexUtil.polar(magnitude, phase);
        }
    }

    @NotNull
    public static FunctionState from(@NotNull RotorStateManager manager, @Nullable String funcName) {
        if (Format.isEmpty(funcName)) {
            funcName = manager.getFunctionMeta().displayName();
        }

        final SortedMap<Double, RotorCoefficient> states = new TreeMap<>(FREQUENCY_COMPARATOR);
        manager.forEachRotorState(state -> states.put(state.getFrequency(), new RotorCoefficient(state)));

        return new FunctionState(
                System.currentTimeMillis(),
                funcName,
                manager.getInternalRotorFrequencyProvider(),
                manager,
                states
        );
    }

    @NotNull
    public static FunctionState from(@NotNull RotorStateManager manager) {
        return from(manager, null);
    }

    private static final Type TYPE_ROTOR_STATES_MAP = new TypeToken<Map<Double, RotorCoefficient>>(){ }.getType();


    public static final String KEY_SAVE_TIMESTAMP = "save_timestamp";
    public static final String KEY_FUNCTION_NAME = "function_name";
    public static final String KEY_DOMAIN_START = "domain_start";
    public static final String KEY_DOMAIN_END = "domain_end";
    public static final String KEY_NUMERICAL_INTEGRATION_INTERVAL_COUNT = "numerical_integration_interval_count";
    public static final String KEY_ROTOR_FREQUENCY_PROVIDER = "rotor_frequency_provider";
    public static final String KEY_DOMAIN_ANIMATION_MILLS_MIN = "domain_animation_mills_min";
    public static final String KEY_DOMAIN_ANIMATION_MILLS_MAX = "domain_animation_mills_max";
    public static final String KEY_DOMAIN_ANIMATION_MILLS_DEFAULT = "domain_animation_mills_default";
    public static final String KEY_ROTOrS_COUNT = "rotors_count";
    public static final String KEY_ROTOR_STATES= "rotors";

    public final long saveTimestamp;
    public final String functionName;

    public final double
            domainStart,
            domainEnd;

    @Nullable
    public final RotorFrequencyProviderE frequencyProviderE;

    public final int numericalIntegrationIntervalCount;

    public final long
            domainAnimationMillsMin,
            domainAnimationMillsMax,
            domainAnimationMillsDefault;

    @NotNull
    public final Map<Double, RotorCoefficient> rotorStates;

    public FunctionState(long saveTimestamp,
                         String functionName, double domainStart,
                         double domainEnd,
                         @Nullable RotorFrequencyProviderE frequencyProviderE,
                         int numericalIntegrationIntervalCount,
                         long domainAnimationMillsMin,
                         long domainAnimationMillsMax,
                         long domainAnimationMillsDefault,
                         @NotNull Map<Double, RotorCoefficient> rotorStates) {

        this.saveTimestamp = saveTimestamp;
        this.functionName = functionName;
        this.domainStart = domainStart;
        this.domainEnd = domainEnd;
        this.frequencyProviderE = frequencyProviderE;
        this.numericalIntegrationIntervalCount = numericalIntegrationIntervalCount;
        this.domainAnimationMillsMin = domainAnimationMillsMin;
        this.domainAnimationMillsMax = domainAnimationMillsMax;
        this.domainAnimationMillsDefault = domainAnimationMillsDefault;
        this.rotorStates = rotorStates;
    }

    public FunctionState(long saveTimestamp,
                         String functionName,
                         @Nullable RotorFrequencyProviderE frequencyProviderE,
                         @NotNull DomainProviderI domainProvider,
                         @NotNull Map<Double, RotorCoefficient> rotorStates) {

        this(saveTimestamp,
                functionName,
                domainProvider.getDomainStart(),
                domainProvider.getDomainEnd(),
                frequencyProviderE,
                domainProvider.getNumericalIntegrationIntervalCount(),
                domainProvider.getDomainAnimationDurationMsMin(),
                domainProvider.getDomainAnimationDurationMsMax(),
                domainProvider.getDomainAnimationDurationMsDefault(),
                rotorStates);
    }

    @NotNull
    public FunctionProviderI toProvider(@NotNull String defaultName) {
        String name = this.functionName;
        if (Format.isEmpty(name)) {
            name = defaultName;
        }

        final List<RotorState> states = rotorStates.entrySet()
                .stream()
                .map(e -> new RotorState(e.getKey(), e.getValue().toComplex()))
                .toList();

        final FunctionMeta meta = new FunctionMeta(
                FunctionType.EXTERNAL_ROTOR_STATE,
                R.createExternalRotorStatesFunctionDisplayTitle(name),
                rotorStates.size(),
                frequencyProviderE,
                states
        );

        return new SimpleFunctionProvider(meta, new RotorStatesFunction(states, domainStart, domainEnd, domainAnimationMillsDefault, domainAnimationMillsMin, domainAnimationMillsMax));
    }


    @NotNull
    public JsonElement toJson(@NotNull JsonSerializationContext context) {
        JsonObject o = new JsonObject();
        o.addProperty(KEY_SAVE_TIMESTAMP, saveTimestamp);
        o.addProperty(KEY_FUNCTION_NAME, Format.toString(functionName));
        o.addProperty(KEY_DOMAIN_START, domainStart);
        o.addProperty(KEY_DOMAIN_END, domainEnd);
        o.addProperty(KEY_NUMERICAL_INTEGRATION_INTERVAL_COUNT, numericalIntegrationIntervalCount);
        o.addProperty(KEY_DOMAIN_ANIMATION_MILLS_MIN, domainAnimationMillsMin);
        o.addProperty(KEY_DOMAIN_ANIMATION_MILLS_MAX, domainAnimationMillsMax);
        o.addProperty(KEY_DOMAIN_ANIMATION_MILLS_DEFAULT, domainAnimationMillsDefault);
        o.addProperty(KEY_ROTOrS_COUNT, rotorStates.size());
        o.addProperty(KEY_ROTOR_FREQUENCY_PROVIDER, R.dumpEnum(frequencyProviderE));
        o.add(KEY_ROTOR_STATES, context.serialize(rotorStates, TYPE_ROTOR_STATES_MAP));

        return o;
    }

    @NotNull
    public JsonElement toJson() {
        return toJson(Json.get());
    }


    @NotNull
    public static FunctionState fromJson(@NotNull JsonElement json, @NotNull JsonDeserializationContext context) throws JsonParseException {
        try {
            final JsonObject o = json.getAsJsonObject();

            return new FunctionState(
                    o.getAsJsonPrimitive(KEY_SAVE_TIMESTAMP).getAsLong(),
                    o.getAsJsonPrimitive(KEY_FUNCTION_NAME).getAsString(),
                    o.getAsJsonPrimitive(KEY_DOMAIN_START).getAsDouble(),
                    o.getAsJsonPrimitive(KEY_DOMAIN_END).getAsDouble(),
                    R.loadEnum(RotorFrequencyProviderE.class, o.getAsJsonPrimitive(KEY_ROTOR_FREQUENCY_PROVIDER).getAsString()),
                    o.getAsJsonPrimitive(KEY_NUMERICAL_INTEGRATION_INTERVAL_COUNT).getAsInt(),
                    o.getAsJsonPrimitive(KEY_DOMAIN_ANIMATION_MILLS_MIN).getAsLong(),
                    o.getAsJsonPrimitive(KEY_DOMAIN_ANIMATION_MILLS_MAX).getAsLong(),
                    o.getAsJsonPrimitive(KEY_DOMAIN_ANIMATION_MILLS_DEFAULT).getAsLong(),
                    context.deserialize(o.get(KEY_ROTOR_STATES), TYPE_ROTOR_STATES_MAP)
            );
        } catch (Throwable t) {
            throw new JsonParseException(t);
        }
    }

    @NotNull
    public static FunctionState fromJson(@NotNull JsonElement json) {
        return fromJson(json, Json.get());
    }

    public static class GsonAdapter implements JsonSerializer<FunctionState>, JsonDeserializer<FunctionState> {

        @Override
        public FunctionState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return fromJson(json, context);
        }

        @Override
        public JsonElement serialize(FunctionState src, Type typeOfSrc, JsonSerializationContext context) {
            return src.toJson(context);
        }
    }
}
