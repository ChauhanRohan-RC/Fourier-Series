package util.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.nio.file.Path;

public class PathGsonAdapter implements JsonSerializer<Path>, JsonDeserializer<Path> {

    @Override
    public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Path.of(json.getAsJsonPrimitive().getAsString());
    }
}
