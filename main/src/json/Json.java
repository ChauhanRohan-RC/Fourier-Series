package json;

import com.google.gson.*;
import function.definition.ColorProviderI;
import function.definition.ComplexDomainFunctionI;
import function.graphic.CharFunction;
import function.graphic.GraphicFunction;
import function.path.PathFunction;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import rotor.frequency.RotorFrequencyProviderI;
import util.json.ColorGsonAdapter;
import util.json.GsonTypeAdapter;
import util.json.JsonParsable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.lang.reflect.Type;


/** Json Parser that can Parse Serializable objects
 *
 * @see Serializable
 * @see JsonParsable
 * @see GsonTypeAdapter
 * */
public class Json implements JsonSerializationContext, JsonDeserializationContext {

    @NotNull
    private static final Type[] SERIALIZED_TYPES = {
            Serializable.class,
            JsonParsable.class,
            Point2D.class,
            Rectangle2D.class,
            ComplexDomainFunctionI.class,
            GraphicFunction.class,
            CharFunction.class,
            PathFunction.class,
            RotorFrequencyProviderI.class,
            ColorProviderI.class
    };


    @Nullable
    private static volatile Json sInstance;

    @NotNull
    public static Json get() {
        Json ins = sInstance;
        if (ins == null) {
            synchronized (Json.class) {
                ins = sInstance;
                if (ins == null) {
                    ins = new Json();
                    sInstance = ins;
                }
            }
        }

        return ins;
    }




    @NotNull
    public final Gson gson;

    private Json() {
        final GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .setLenient()
                .registerTypeAdapter(Color.class, new ColorGsonAdapter());

        for (Type type: SERIALIZED_TYPES) {
            gsonBuilder.registerTypeAdapter(type, new GsonTypeAdapter<>());
        }

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
