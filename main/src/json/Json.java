package json;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import rotor.FunctionState;
import rotor.frequency.RotorFrequencyProviderI;
import util.json.GsonTypeAdapter;
import util.json.JsonParsable;

import java.io.Serializable;
import java.lang.reflect.Type;


/** Json Parser that can Parse Serializable objects
 *
 * @see Serializable
 * @see JsonParsable
 * @see GsonTypeAdapter
 * */
public class Json implements JsonSerializationContext, JsonDeserializationContext {

    private static final Json sInstance = new Json();

    @NotNull
    public static Json get() {
        return sInstance;
    }



    @NotNull
    public final Gson gson;

    private Json() {
        final GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .setLenient()
                .registerTypeAdapter(Serializable.class, new GsonTypeAdapter<>())
                .registerTypeAdapter(JsonParsable.class, new GsonTypeAdapter<>())
                .registerTypeAdapter(RotorFrequencyProviderI.class, new GsonTypeAdapter<>());

        gson = gsonBuilder.create();
    }


    @Override
    public JsonElement serialize(Object src) {
        return gson.toJsonTree(src);
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc) {
        return gson.toJsonTree(src, typeOfSrc);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
        return gson.fromJson(json, typeOfT);
    }
}
