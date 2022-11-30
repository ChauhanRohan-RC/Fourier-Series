package util.json;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Type;

public class ColorGsonAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {

    @Override
    public JsonElement serialize(@NotNull Color src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getRGB());
    }

    @Override
    public Color deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new Color(json.getAsJsonPrimitive().getAsInt(), true);
    }

}
