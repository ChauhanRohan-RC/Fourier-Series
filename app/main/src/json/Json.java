package json;

import app.Settings;
import com.google.gson.*;
import function.definition.ColorProviderI;
import function.definition.ComplexDomainFunctionI;
import function.graphic.CharFunction;
import function.graphic.GraphicFunction;
import function.path.PathFunctionI;
import misc.ExternalJava;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;
import rotor.frequency.RotorFrequencyProviderI;
import util.ExternalJavaLocationGsonAdapter;
import util.ExternalProgramFunction;
import util.json.ColorGsonAdapter;
import util.json.GsonTypeAdapter;
import util.json.JsonParsable;
import util.json.PathGsonAdapter;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Path;


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
            Line2D.class,
            Rectangle2D.class,
            ComplexDomainFunctionI.class,
            GraphicFunction.class,
            CharFunction.class,
            PathFunctionI.class,
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
                .setLenient();

        for (Type type: SERIALIZED_TYPES) {
            gsonBuilder.registerTypeAdapter(type, new GsonTypeAdapter<>());
        }

        gsonBuilder.registerTypeAdapter(Path.class, new PathGsonAdapter())
                .registerTypeAdapter(Color.class, new ColorGsonAdapter())
                .registerTypeAdapter(ExternalJava.Location.class, new ExternalJavaLocationGsonAdapter())
                .registerTypeAdapter(ExternalProgramFunction.class, new ExternalProgramFunction.GsonAdapter())
                .registerTypeAdapter(Settings.class, new Settings.GsonAdapter());

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

    @Override
    public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
        return gson.fromJson(json, typeOfT);
    }
}
