package util;

import com.google.gson.*;
import function.ComplexDomainFunctionWrapper;
import function.definition.ComplexDomainFunctionI;
import misc.ExternalJava;
import org.jetbrains.annotations.NotNull;
import util.json.GsonTypeAdapter;

import javax.tools.JavaFileObject;
import java.lang.reflect.Type;

public class ExternalProgramFunction extends ComplexDomainFunctionWrapper {

    private static final String KEY_LOCATION = "location";
    private static final String KEY_BASE_FUNCTION = "base_function";

    @NotNull
    public final ExternalJava.Location location;

    public ExternalProgramFunction(@NotNull ComplexDomainFunctionI base, @NotNull ExternalJava.Location location) {
        super(base);
        this.location = location;
    }

    public static class GsonAdapter implements JsonSerializer<ExternalProgramFunction>, JsonDeserializer<ExternalProgramFunction> {

        @Override
        public JsonElement serialize(ExternalProgramFunction src, Type typeOfSrc, JsonSerializationContext context) {
            // Serialize everything, just as GsonTypeAdapter

            final JsonObject o = new JsonObject();
            o.addProperty(GsonTypeAdapter.CLASS_NAME_KEY, src.getClass().getName());

            final JsonObject data = new JsonObject();
            data.add(KEY_LOCATION, context.serialize(src.location));
            data.add(KEY_BASE_FUNCTION, context.serialize(src.getBaseFunction(), src.getBaseFunction().getClass()));
            o.add(GsonTypeAdapter.DATA_KEY, data);
            return o;
        }

        private void throwNoLocation() {
            throw new JsonParseException("No external program location found");
        }

        @Override
        public ExternalProgramFunction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            // Deserialize only data, delegated by GsonTypeAdapter

            final JsonObject o = json.getAsJsonObject();
            final JsonElement locElement = o.get(KEY_LOCATION);
            if (locElement == null) {
                throwNoLocation();
            }

            final ExternalJava.Location location = context.deserialize(locElement, ExternalJava.Location.class);
            if (location == null) {
                throwNoLocation();
            }

            try {
                final Class<?> clazz = ExternalJava.compileAndLoadClass(new ExternalJava.JavaObject(location, JavaFileObject.Kind.SOURCE), true);
                // todo: use this class for deserializing
                return new ExternalProgramFunction(context.deserialize(o.get(KEY_BASE_FUNCTION), clazz), location);
            } catch (Throwable e) {
                throw new JsonParseException("Failed to load class at location " + location.getSourcePath(), e);
            }
        }
    }
}
