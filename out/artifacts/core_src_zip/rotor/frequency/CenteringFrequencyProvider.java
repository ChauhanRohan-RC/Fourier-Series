package rotor.frequency;

public final class CenteringFrequencyProvider implements RotorFrequencyProviderI {

    public static final double DEFAULT_FREQUENCY_MULTIPLIER = 1;

//    private static final String KEY_FREQ_MULTIPLIER = "frequency_multiplier";

    private double frequencyMultiplier = DEFAULT_FREQUENCY_MULTIPLIER;

    public CenteringFrequencyProvider(double frequencyMultiplier) {
        this.frequencyMultiplier = frequencyMultiplier;
    }

    public CenteringFrequencyProvider() {
    }


    public double getFrequencyMultiplier() {
        return frequencyMultiplier;
    }

    public CenteringFrequencyProvider setFrequencyMultiplier(double frequencyMultiplier) {
        this.frequencyMultiplier = frequencyMultiplier;
        return this;
    }

    @Override
    public double getRotorFrequency(int index, int count) {
        if (count % 2 == 0) {
            count++;
        }

        final int f = (index + 1) - ((count + 1) / 2);          // int frequencies
        return f * frequencyMultiplier;                         // transforms
    }

    @Override
    public String toString() {
        return "CenteringFrequencyProvider{" +
                "frequencyMultiplier=" + frequencyMultiplier +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        CenteringFrequencyProvider that = (CenteringFrequencyProvider) o;
        return that.frequencyMultiplier == frequencyMultiplier;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(frequencyMultiplier);
    }

    //    @NotNull
//    public JsonElement toJson() {
//        final JsonObject o = new JsonObject();
//        o.addProperty(KEY_FREQ_MULTIPLIER, frequencyMultiplier);
//        return o;
//    }
//
//    public static CenteringFrequencyProvider fromJson(@NotNull JsonElement json) throws JsonParseException {
//        final JsonObject o = json.getAsJsonObject();
//
//        final double freqMult = o.has(KEY_FREQ_MULTIPLIER)? o.getAsJsonPrimitive(KEY_FREQ_MULTIPLIER).getAsDouble(): 1;
//
//        return new CenteringFrequencyProvider()
//    }


//    public static class GsonAdapter implements JsonSerializer<CenteringFrequencyProvider>, JsonDeserializer<CenteringFrequencyProvider> {
//
//
//        @Override
//        public CenteringFrequencyProvider deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//            return null;
//        }
//
//        @Override
//        public JsonElement serialize(CenteringFrequencyProvider src, Type typeOfSrc, JsonSerializationContext context) {
//            return null;
//        }
//    }
}
