package util.json;

import org.jetbrains.annotations.NotNull;

import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * Type Adapter for GSON that enables Interface references to deserialize
 * It serializes object by storing its Class name in {@link GsonTypeAdapter#CLASS_NAME_KEY} that is used to deserialize it properly
 *
 * @see JsonParsable
 * */
public class GsonTypeAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    public static String CLASS_NAME_KEY = "_class_name";
    public static String DATA_KEY = "_data";

    /**
     * Packs Class name and Data of supplied Object in JsonObject for future deserialization
     * */
    @Override
    public JsonElement serialize(@NotNull T o, Type type, @NotNull JsonSerializationContext jsonSerializationContext) {
        final Class<?> clazz = o.getClass();

        final JsonObject obj = new JsonObject();
        obj.addProperty(CLASS_NAME_KEY, clazz.getName());
        obj.add(DATA_KEY, jsonSerializationContext.serialize(o, clazz));
        return obj;
    }


    /**
     * Extracts Class<?> using class name stored in {@link GsonTypeAdapter#CLASS_NAME_KEY} and uses it to deserialize the JsonObject
     *
     * @throws JsonParseException in case of Parsing exception or Class could not be loaded by {@link GsonTypeAdapter#getClass(String)}
     *  */
    @Override
    public T deserialize(@NotNull JsonElement jsonElement, Type type, @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject obj = jsonElement.getAsJsonObject();

        final Class<?> clazz = getClass(obj.getAsJsonPrimitive(CLASS_NAME_KEY).getAsString());
        final JsonElement data = obj.get(DATA_KEY);

        return jsonDeserializationContext.deserialize(data, clazz);
    }

    /**
     * Utility method for fetching Class<?> object from class name
     * */
    @NotNull
    private static Class<?> getClass(@NotNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }
}
