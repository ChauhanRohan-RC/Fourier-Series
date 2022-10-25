package json;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import rotor.FunctionState;

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
                .registerTypeAdapter(Serializable.class, new GsonTypeAdapter<>())
                .registerTypeAdapter(JsonParsable.class, new GsonTypeAdapter<>())
                .registerTypeAdapter(FunctionState.class, new FunctionState.GsonAdapter());

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
        return (R) gson.fromJson(json, typeOfT);
    }
}
