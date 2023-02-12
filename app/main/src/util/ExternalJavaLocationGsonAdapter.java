package util;

import com.google.gson.*;
import misc.ExternalJava;

import java.lang.reflect.Type;
import java.nio.file.Path;

public class ExternalJavaLocationGsonAdapter implements JsonSerializer<ExternalJava.Location>, JsonDeserializer<ExternalJava.Location> {

    private static final String KEY_CLASSPATH = "classpath";
    private static final String KEY_REL_SRC_PATH = "relativeSourcePath";

    @Override
    public JsonElement serialize(ExternalJava.Location src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject o = new JsonObject();
        o.addProperty(KEY_CLASSPATH, src.classpath.toString());
        o.addProperty(KEY_REL_SRC_PATH, src.relativeSourcePath);
        return o;
    }

    @Override
    public ExternalJava.Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject o = json.getAsJsonObject();
        return new ExternalJava.Location(Path.of(o.getAsJsonPrimitive(KEY_CLASSPATH).getAsString()), o.getAsJsonPrimitive(KEY_REL_SRC_PATH).getAsString());
    }
}