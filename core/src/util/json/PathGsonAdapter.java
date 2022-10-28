package util.json;

import com.google.gson.*;
import util.Log;

import java.lang.reflect.Type;
import java.nio.file.Path;

public class PathGsonAdapter implements JsonSerializer<Path>, JsonDeserializer<Path> {

    @Override
    public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
        Log.d("path adapter: serializing " + src);
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Log.d("path adapter: deserialising " + json.getAsString());
        return Path.of(json.getAsJsonPrimitive().getAsString());
    }
}
