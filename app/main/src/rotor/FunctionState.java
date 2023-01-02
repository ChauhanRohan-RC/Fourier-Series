package rotor;

import app.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import function.RotorStatesFunction;
import function.definition.ComplexDomainFunctionI;
import function.definition.DomainProviderI;
import json.Json;
import misc.CollectionUtil;
import misc.FileUtil;
import misc.Format;
import misc.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import provider.*;
import rotor.frequency.RotorFrequencyProviderI;
import async.Async;
import async.Canceller;
import async.TaskConsumer;
import util.main.ComplexUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FunctionState {

    public static final String TAG = "FunctionState";

    public static final boolean FUNCTION_SERIALIZATION_ENABLED = true;
    public static final boolean DEFAULT_SERIALIZE_FUNCTION = true;

    public static final boolean ROTOR_STATES_SERIALIZATION_ENABLED = true;
    public static final boolean DEFAULT_SERIALIZE_ROTOR_STATES = true;

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
            return ComplexUtil.polarExact(magnitude, phase);
        }
    }

    @NotNull
    public static FunctionState from(@NotNull RotorStateManager manager, @Nullable String funcName) {
        if (Format.isEmpty(funcName)) {
            funcName = manager.getFunctionMeta().displayName();
        }

        final FunctionType functionType = manager.getFunctionMeta().functionType();
        final ComplexDomainFunctionI function = manager.getFunction();

        return new FunctionState(
                System.currentTimeMillis(),
                funcName,
                functionType,
                function,
                function,
                manager.getManagerDefaultRotorFrequencyProvider(),          // should use function default
                manager.getManagerRotorFrequencyProvider(),
                manager.getRotorCount(),
                manager.getRotorStatesListCopy()
        );
    }

    @NotNull
    public static FunctionState from(@NotNull FunctionMeta meta, @NotNull ComplexDomainFunctionI function) {
        return new FunctionState(
                System.currentTimeMillis(),
                meta.displayName(),
                meta.functionType(),
                function,
                function,
                function.getFunctionDefaultFrequencyProvider(),          // should use function default
                null,
                0,
                null
        );
    }


    private static final Type TYPE_ROTOR_STATES_MAP = new TypeToken<Map<Double, RotorCoefficient>>(){ }.getType();

    private static final String KEY_SERIALIZED_FUNCTION = "_function";
    private static final String KEY_SERIALIZED_ROTOR_STATES = "rotor_states";

//    public static final String KEY_SAVE_TIMESTAMP = "save_timestamp";
//    public static final String KEY_FUNCTION_NAME = "function_name";
//    public static final String KEY_DOMAIN_START = "domain_start";
//    public static final String KEY_DOMAIN_END = "domain_end";
//    public static final String KEY_NUMERICAL_INTEGRATION_INTERVAL_COUNT = "numerical_integration_interval_count";
//    public static final String KEY_ROTOR_FREQUENCY_PROVIDER = "rotor_frequency_provider";
//    public static final String KEY_DOMAIN_ANIMATION_MILLS_MIN = "domain_animation_mills_min";
//    public static final String KEY_DOMAIN_ANIMATION_MILLS_MAX = "domain_animation_mills_max";
//    public static final String KEY_DOMAIN_ANIMATION_MILLS_DEFAULT = "domain_animation_mills_default";
//    public static final String KEY_ROTOrS_COUNT = "rotors_count";
//    public static final String KEY_ROTOR_STATES= "rotors";


    public final long saveTimestamp;
    public final String functionName;
    @Nullable
    public final FunctionType functionType;

    @Expose(serialize = false, deserialize = false)
    @Nullable
    private transient final ComplexDomainFunctionI function;

    @SerializedName(KEY_SERIALIZED_FUNCTION)
    @Nullable
    private ComplexDomainFunctionI serializedFunction;

    public final double domainStart;
    public final double domainEnd;
    public final int numericalIntegrationIntervalCount;

    @Nullable
    public final RotorFrequencyProviderI defaultFrequencyProvider;
    @Nullable
    public final RotorFrequencyProviderI frequencyProvider;

    public final long domainAnimationMillsMin;
    public final long domainAnimationMillsMax;
    public final long domainAnimationMillsDefault;

    public final int rotorCount;            // current rotor count, may be less than size of all loaded rotor states

    @Expose(serialize = false, deserialize = false)
    @Nullable
    private transient Map<Double, RotorCoefficient> allRotorStates;       // can be null if not serialized

    @SerializedName(KEY_SERIALIZED_ROTOR_STATES)
    @Nullable
    private Map<Double, RotorCoefficient> allSerializedRotorStates;

    public FunctionState(long saveTimestamp,
                         String functionName,
                         @Nullable FunctionType functionType,
                         @Nullable ComplexDomainFunctionI function,
                         double domainStart,
                         double domainEnd,
                         int numericalIntegrationIntervalCount,
                         @Nullable RotorFrequencyProviderI defaultFrequencyProvider,
                         @Nullable RotorFrequencyProviderI frequencyProvider,
                         long domainAnimationMillsMin,
                         long domainAnimationMillsMax,
                         long domainAnimationMillsDefault,
                         int rotorCount,
                         @Nullable Collection<RotorState> allRotorStates) {

        this.saveTimestamp = saveTimestamp;
        this.functionName = functionName;
        this.functionType = functionType;
        this.function = function;
        this.domainStart = domainStart;
        this.domainEnd = domainEnd;
        this.numericalIntegrationIntervalCount = numericalIntegrationIntervalCount;
        this.defaultFrequencyProvider = defaultFrequencyProvider;
        this.frequencyProvider = frequencyProvider;
        this.domainAnimationMillsMin = domainAnimationMillsMin;
        this.domainAnimationMillsMax = domainAnimationMillsMax;
        this.domainAnimationMillsDefault = domainAnimationMillsDefault;
        this.rotorCount = rotorCount;
        this.allRotorStates = createOrAddSorted(allRotorStates, null);

        // defaults
        setSerializeFunction(DEFAULT_SERIALIZE_FUNCTION);
        setSerializeRotorStates(DEFAULT_SERIALIZE_ROTOR_STATES);
    }

    public FunctionState(long saveTimestamp,
                         String functionName,
                         @Nullable FunctionType functionType,
                         @Nullable ComplexDomainFunctionI function,
                         @NotNull DomainProviderI domainProvider,
                         @Nullable RotorFrequencyProviderI defaultFrequencyProvider,
                         @Nullable RotorFrequencyProviderI frequencyProvider,
                         int rotorCount,
                         @Nullable Collection<RotorState> allRotorStates) {

        this(saveTimestamp,
                functionName,
                functionType,
                function,
                domainProvider.getDomainStart(),
                domainProvider.getDomainEnd(),
                domainProvider.getNumericalIntegrationIntervalCount(),
                defaultFrequencyProvider,
                frequencyProvider,
                domainProvider.getDomainAnimationDurationMsMin(),
                domainProvider.getDomainAnimationDurationMsMax(),
                domainProvider.getDomainAnimationDurationMsDefault(),
                rotorCount,
                allRotorStates);
    }

    @Nullable
    public ComplexDomainFunctionI getSerializedFunction() {
        return serializedFunction;
    }

    public boolean hasSerialisedFunction() {
        return serializedFunction != null;
    }

    public void setSerializeFunction(boolean serializeFunction) {
        if (FUNCTION_SERIALIZATION_ENABLED && serializeFunction && functionType != null && functionType.serializable) {
            this.serializedFunction = function;
        } else {
            this.serializedFunction = null; // cannot serialize
        }
    }

    @Nullable
    public ComplexDomainFunctionI getFunction() {
        if (function == null) {
            return serializedFunction;
        }

        return function;
    }


    public boolean hasSerializedRotorStates() {
        return CollectionUtil.notEmpty(allSerializedRotorStates);
    }

    public void setSerializeRotorStates(boolean serializeRotorStates) {
        if (ROTOR_STATES_SERIALIZATION_ENABLED && serializeRotorStates) {
            this.allSerializedRotorStates = allRotorStates;
        } else {
            this.allSerializedRotorStates = null;              // do not serialize
        }
    }

    @Nullable
    public Map<Double, RotorCoefficient> getAllRotorStates() {
        if (CollectionUtil.isEmpty(allRotorStates)) {
            return allSerializedRotorStates;
        }

        return allRotorStates;
    }


    public void addRotorStates(Collection<RotorState> rotorStates) {
        if (CollectionUtil.isEmpty(rotorStates))
            return;

        allRotorStates = createOrAddSorted(rotorStates, allRotorStates);
    }


    @NotNull
    public String getTypedFunctionDisplayName() {
        final String name = functionName != null? functionName: R.DISPLAY_NAME_FUNCTION_UNKNOWN;
        final String type = functionType != null? functionType.displayName: "Unknown Type";
        return name + " (" + type + ")";
    }

    @NotNull
    public SimpleFunctionProvider toProvider(@NotNull String defaultName, boolean externallyLoaded) {
        String name = this.functionName;
        if (Format.isEmpty(name))
            name = defaultName;

        if (externallyLoaded) {
            name = R.createExternallyLoadedFunctionDisplayName(name);
        }

        final ComplexDomainFunctionI func = getFunction();
        final Map<Double, RotorCoefficient> allStates = getAllRotorStates();
        final List<RotorState> _states = toList(allStates);
        final int _rotorCount = func != null? rotorCount: Math.min(rotorCount, CollectionUtil.size(_states));

        if (functionType != null && func != null) {
            final FunctionMeta meta = new FunctionMeta(
                    functionType,
                    name,
                    frequencyProvider,
                    _rotorCount,
                    true,
                    _states
            );

            return new SimpleFunctionProvider(meta, func);
        }

        final FunctionMeta meta = new FunctionMeta(
                FunctionType.EXTERNAL_ROTOR_STATE,
                name,
                frequencyProvider,
                _rotorCount,
                func != null,
                _states
        );

        return new SimpleFunctionProvider(meta, new RotorStatesFunction(
                func,
                _states,
                domainStart,
                domainEnd,
                numericalIntegrationIntervalCount,
                domainAnimationMillsDefault,
                domainAnimationMillsMin,
                domainAnimationMillsMax,
                defaultFrequencyProvider)
        );
    }


    /* .......................... CSV ..................................*/

    @NotNull
    public static Map<Double, RotorCoefficient> createOrAddSorted(@Nullable Collection<RotorState> states, @Nullable Map<Double, RotorCoefficient> dest) {
        if (dest == null) {
            dest = new TreeMap<>(FREQUENCY_COMPARATOR);
        }

        final Map<Double, RotorCoefficient> result = dest;
        if (CollectionUtil.notEmpty(states)) {
            states.forEach(s -> {
                if (s != null) {
                    result.put(s.getFrequency(), new RotorCoefficient(s));
                }
            });
        }

        return result;
    }

    @NotNull
    public static List<RotorState> toList(@Nullable Map<Double, RotorCoefficient> states) {
        return CollectionUtil.notEmpty(states)? states.entrySet()
                .stream()
                .map(e -> new RotorState(e.getKey(), e.getValue().toComplex()))
                .toList(): Collections.emptyList();
    }


    private static final String ROTOR_STATE_CSV_HEADER_FREQUENCY = "FREQUENCY (Hz)";
    private static final String ROTOR_STATE_CSV_HEADER_MAGNITUDE = "MAGNITUDE";
    private static final String ROTOR_STATE_CSV_HEADER_PHASE = "PHASE (rad)";


    private static final CSVFormat ROTOR_STATES_CSV_FORMAT = CSVFormat.EXCEL.builder()
            .setCommentMarker('#')
            .setHeader(ROTOR_STATE_CSV_HEADER_FREQUENCY, ROTOR_STATE_CSV_HEADER_MAGNITUDE, ROTOR_STATE_CSV_HEADER_PHASE)
            .setIgnoreHeaderCase(true)
            .setAllowDuplicateHeaderNames(false)
            .setIgnoreEmptyLines(true)
            .setIgnoreSurroundingSpaces(true)
            .setAutoFlush(true)
            .build();

    @NotNull
    public static CSVFormat createCSVFormat(boolean reader, Object... headerComments) {
        return ROTOR_STATES_CSV_FORMAT.builder()
//                .setSkipHeaderRecord(reader)                 // write headers
                .setSkipHeaderRecord(false)                 // write and read headers (will ignore error while reading)
                .setHeaderComments(headerComments)          // header comments
                .build();
    }



    /* write */

    public static void writeRotorStatesASCSV(@NotNull Appendable writer, @Nullable Map<Double, RotorCoefficient> states, Object... headerComments) throws IOException {
        try (final CSVPrinter printer = new CSVPrinter(writer, createCSVFormat(false, headerComments))) {
            if (CollectionUtil.isEmpty(states))
                return;

            for (Map.Entry<Double, RotorCoefficient> entry: states.entrySet()) {
                final RotorCoefficient c = entry.getValue();
                printer.printRecord(entry.getKey(), c.magnitude, c.phase);
            }
        }
    }

    public static void writeRotorStatesASCSV(@NotNull Appendable writer, @Nullable Collection<RotorState> states, Object... headerComments) throws IOException {
        try (final CSVPrinter printer = new CSVPrinter(writer, createCSVFormat(false, headerComments))) {
            if (CollectionUtil.isEmpty(states))
                return;

            for (RotorState state: states) {
                printer.printRecord(state.getFrequency(), state.getMagnitudeScale(), state.getCoefficientArgument());
            }
        }
    }

    @Nullable
    public Object[] getRotorStatesCSVHeaderComment() {
        return new String[] {
                "%%______Function State (Meta)______%%",
                "save_timestamp: " + saveTimestamp,
                "function: " + (Format.notEmpty(functionName)? functionName: R.DISPLAY_NAME_FUNCTION_UNKNOWN),
                "function_type: " + (functionType != null? functionType.displayName: "Unknown Type"),
                "domain_start: " + domainStart,
                "domain_end: " + domainEnd,
                "numerical_integration_intervals: " + numericalIntegrationIntervalCount,
                "rotor_states_count: " + CollectionUtil.size(allRotorStates),
                "",
                "%%_______Rotor States_______%%"
        };
    }

    public void writeRotorStatesASCSV(@NotNull Path file, @NotNull Charset encoding) throws JsonParseException, IOException {
        try (final Writer writer = Files.newBufferedWriter(file, encoding)) {
            writeRotorStatesASCSV(writer, allRotorStates, getRotorStatesCSVHeaderComment());
        }
    }

    public void writeRotorStatesASCSV(@NotNull Path file) throws JsonParseException, IOException {
        writeRotorStatesASCSV(file, R.ENCODING);
    }

    @NotNull
    public Canceller writeRotorStatesASCSVAsync(@NotNull Path file, @NotNull Charset encoding, @Nullable TaskConsumer<Void> consumer) {
        return Async.execute(() -> {
            writeRotorStatesASCSV(file, encoding);
            return null;
        }, consumer);
    }

    @NotNull
    public Canceller writeRotorStatesASCSVAsync(@NotNull Path file, @Nullable TaskConsumer<Void> consumer) {
        return writeRotorStatesASCSVAsync(file, R.ENCODING, consumer);
    }


    /* Read */

    @NotNull
    public static List<RotorState> readRotorStatesFromCSV(@NotNull Reader csv) throws IOException, NumberFormatException, NullPointerException {
        final List<RotorState> states = new ArrayList<>();
        try (final CSVParser parser = CSVParser.parse(csv, createCSVFormat(true))) {
            final Iterator<CSVRecord> itr = parser.iterator();

            while (itr.hasNext()) {
                try {
                    final CSVRecord record = itr.next();

                    states.add(new RotorState(
                            Double.parseDouble(record.get(ROTOR_STATE_CSV_HEADER_FREQUENCY)),
                            ComplexUtil.polarExact(Double.parseDouble(record.get(ROTOR_STATE_CSV_HEADER_MAGNITUDE)), Double.parseDouble(record.get(ROTOR_STATE_CSV_HEADER_PHASE)))
                    ));
                } catch (NumberFormatException num) {
                    Log.w(TAG, String.format("Exception while loading Rotor State from CSV at line %d. ignoring anyway....", parser.getCurrentLineNumber()), num);
                }
            }
        }

        return states;
    }

    @NotNull
    public static List<RotorState> readRotorStatesFromCSV(@NotNull Path file, @NotNull Charset encoding) throws IOException, JsonParseException {
        try (final Reader reader = Files.newBufferedReader(file, encoding)) {
            return readRotorStatesFromCSV(reader);
        }
    }

    @NotNull
    public static List<RotorState> readRotorStatesFromCSV(@NotNull Path file) throws IOException, JsonParseException {
        return readRotorStatesFromCSV(file, R.ENCODING);
    }

    @NotNull
    public static Canceller readRotorStatesFromCSVAsync(@NotNull Path file, @NotNull Charset encoding, @NotNull TaskConsumer<List<RotorState>> consumer) {
        return Async.execute(() -> readRotorStatesFromCSV(file, encoding), consumer);
    }

    @NotNull
    public static Canceller readRotorStatesFromCSVAsync(@NotNull Path file, @NotNull TaskConsumer<List<RotorState>>  consumer) {
        return readRotorStatesFromCSVAsync(file, R.ENCODING, consumer);
    }


    /*...................................  JSON  ...........................................*/

    /* Save */

    @NotNull
    public String toJsonString() throws JsonParseException {
        return Json.get().gson.toJson(this, getClass());
    }

    public void writeJson(@NotNull Appendable writer) throws JsonParseException {
        Json.get().gson.toJson(this, getClass(), writer);
    }

    public void writeJson(@NotNull Path file, @NotNull Charset encoding) throws JsonParseException, IOException {
        FileUtil.ensureFileParentDir(file);

        try (final Writer writer = Files.newBufferedWriter(file, encoding)) {
            writeJson(writer);
        }
    }

    public void writeJson(@NotNull Path file) throws JsonParseException, IOException {
        writeJson(file, R.ENCODING);
    }

    @NotNull
    public Canceller writeJsonAsync(@NotNull Path file, @NotNull Charset encoding, @Nullable TaskConsumer<Void> consumer) {
        return Async.execute(() -> {
            writeJson(file, encoding);
            return null;
        }, consumer);
    }

    @NotNull
    public Canceller writeJsonAsync(@NotNull Path file, @Nullable TaskConsumer<Void> consumer) {
        return writeJsonAsync(file, R.ENCODING, consumer);
    }

    /* Load */

    @NotNull
    public static FunctionState loadFromJson(@NotNull Reader json, boolean withFunctionDefinition) throws JsonParseException {
        if (!withFunctionDefinition) {
            final JsonObject o = JsonParser.parseReader(json).getAsJsonObject();
            o.remove(KEY_SERIALIZED_FUNCTION);
            return Json.get().gson.fromJson(o, FunctionState.class);
        }

        return Json.get().gson.fromJson(json, FunctionState.class);
    }

    @NotNull
    public static FunctionState loadFromJson(@NotNull Path file, @NotNull Charset encoding, boolean withFunctionDefinition) throws IOException, JsonParseException {
        try (final Reader reader = Files.newBufferedReader(file, encoding)) {
            return loadFromJson(reader, withFunctionDefinition);
        }
    }

    @NotNull
    public static FunctionState loadFromJson(@NotNull Path file, boolean withFunctionDefinition) throws IOException, JsonParseException {
        return loadFromJson(file, R.ENCODING, withFunctionDefinition);
    }

    @NotNull
    public static Canceller loadFromJsonAsync(@NotNull Path file, @NotNull Charset encoding, boolean withFunctionDefinition, @NotNull TaskConsumer<FunctionState> consumer) {
        return Async.execute(() -> loadFromJson(file, encoding, withFunctionDefinition), consumer);
    }

    @NotNull
    public static Canceller loadFromJsonAsync(@NotNull Path file, boolean withFunctionDefinition, @NotNull TaskConsumer<FunctionState> consumer) {
        return loadFromJsonAsync(file, R.ENCODING, withFunctionDefinition, consumer);
    }




//    @NotNull
//    public JsonElement toJson(@NotNull JsonSerializationContext context) {
//        JsonObject o = new JsonObject();
//        o.addProperty(KEY_SAVE_TIMESTAMP, saveTimestamp);
//        o.addProperty(KEY_FUNCTION_NAME, Format.toString(functionName));
//        o.addProperty(KEY_DOMAIN_START, domainStart);
//        o.addProperty(KEY_DOMAIN_END, domainEnd);
//        o.addProperty(KEY_NUMERICAL_INTEGRATION_INTERVAL_COUNT, numericalIntegrationIntervalCount);
//        o.addProperty(KEY_DOMAIN_ANIMATION_MILLS_MIN, domainAnimationMillsMin);
//        o.addProperty(KEY_DOMAIN_ANIMATION_MILLS_MAX, domainAnimationMillsMax);
//        o.addProperty(KEY_DOMAIN_ANIMATION_MILLS_DEFAULT, domainAnimationMillsDefault);
//        o.addProperty(KEY_ROTOrS_COUNT, rotorStates.size());
//        o.add(KEY_ROTOR_FREQUENCY_PROVIDER, context.serialize(frequencyProvider, RotorFrequencyProviderI.class));
//        o.add(KEY_ROTOR_STATES, context.serialize(rotorStates, TYPE_ROTOR_STATES_MAP));
//        return o;
//    }

//    @NotNull
//    public JsonElement toJson() {
//        return toJson(Json.get());
//    }


//    @NotNull
//    public static FunctionState fromJson(@NotNull JsonElement json, @NotNull JsonDeserializationContext context) throws JsonParseException {
//        try {
//            final JsonObject o = json.getAsJsonObject();
//
//            return new FunctionState(
//                    o.getAsJsonPrimitive(KEY_SAVE_TIMESTAMP).getAsLong(),
//                    o.getAsJsonPrimitive(KEY_FUNCTION_NAME).getAsString(),
//                    o.getAsJsonPrimitive(KEY_DOMAIN_START).getAsDouble(),
//                    o.getAsJsonPrimitive(KEY_DOMAIN_END).getAsDouble(),
//                    context.deserialize(o.get(KEY_ROTOR_FREQUENCY_PROVIDER), RotorFrequencyProviderI.class),
//                    o.getAsJsonPrimitive(KEY_NUMERICAL_INTEGRATION_INTERVAL_COUNT).getAsInt(),
//                    o.getAsJsonPrimitive(KEY_DOMAIN_ANIMATION_MILLS_MIN).getAsLong(),
//                    o.getAsJsonPrimitive(KEY_DOMAIN_ANIMATION_MILLS_MAX).getAsLong(),
//                    o.getAsJsonPrimitive(KEY_DOMAIN_ANIMATION_MILLS_DEFAULT).getAsLong(),
//                    context.deserialize(o.get(KEY_ROTOR_STATES), TYPE_ROTOR_STATES_MAP)
//            );
//        } catch (Throwable t) {
//            throw new JsonParseException(t);
//        }
//    }


//    @NotNull
//    public static FunctionState fromJson(@NotNull JsonElement json) {
//        return fromJson(json, Json.get());
//    }
//
//    public static class GsonAdapter implements JsonSerializer<FunctionState>, JsonDeserializer<FunctionState> {
//
//        @Override
//        public FunctionState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//            return fromJson(json, context);
//        }
//
//        @Override
//        public JsonElement serialize(FunctionState src, Type typeOfSrc, JsonSerializationContext context) {
//            return src.toJson(context);
//        }
//    }
}
