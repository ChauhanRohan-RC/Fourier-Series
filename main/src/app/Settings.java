package app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import json.Json;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Format;
import util.async.Async;
import util.async.Canceller;
import util.async.TaskConsumer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Settings {

    public static final String DEFAULT_LOOK_AND_FEEL_CLASSNAME = FlatDarculaLaf.class.getName();

    @NotNull
    public static Settings createDefault() {
        return new Settings();
    }

    @Expose(serialize = false, deserialize = false)
    private transient int mModCount;

    @SerializedName("theme")
    @Nullable
    private String lookAndFeelClassName;

    // DEFAULT
    private Settings() {
        lookAndFeelClassName = DEFAULT_LOOK_AND_FEEL_CLASSNAME;
    }

    public int getModCount() {
        return mModCount;
    }

    public Settings setLookAndFeelClassName(@Nullable String lookAndFeelClassName) {
        if (!Objects.equals(this.lookAndFeelClassName, lookAndFeelClassName)) {
            this.lookAndFeelClassName = lookAndFeelClassName;
            mModCount++;
        }

        return this;
    }

    @NotNull
    public String getLookAndFeelClassName() {
        if (Format.isEmpty(lookAndFeelClassName)) {
            return DEFAULT_LOOK_AND_FEEL_CLASSNAME;
        }

        return lookAndFeelClassName;
    }



    /*...................................  JSON  ...........................................*/

    /* Save */

    @NotNull
    public String toJsonString() throws JsonParseException {
        return Json.get().gson.toJson(this, getClass());
    }

    public void writeJson(@NotNull Appendable writer) throws JsonParseException {
        Json.get().gson.toJson(this, getClass(), writer);
    }

    public void writeJson(@NotNull Path file, @NotNull Charset encoding) throws JsonParseException, IOException {
        try (final Writer writer = Files.newBufferedWriter(file, encoding)) {
            writeJson(writer);
        }
    }

    public void writeJson(@NotNull Path file) throws JsonParseException, IOException {
        writeJson(file, R.ENCODING);
    }

    @NotNull
    public Canceller writeJsonAsync(@NotNull Path file, @NotNull Charset encoding, @Nullable TaskConsumer<Void> consumer) {
        return Async.execute(() -> {
            writeJson(file, encoding);
            return null;
        }, consumer);
    }

    @NotNull
    public Canceller writeJsonAsync(@NotNull Path file, @Nullable TaskConsumer<Void> consumer) {
        return writeJsonAsync(file, R.ENCODING, consumer);
    }

    /* Load */

    @NotNull
    public static Settings loadFromJson(@NotNull Reader json) throws JsonParseException {
        return Json.get().gson.fromJson(json, Settings.class);
    }

    @NotNull
    public static Settings loadFromJson(@NotNull Path file, @NotNull Charset encoding) throws IOException, JsonParseException {
        try (final Reader reader = Files.newBufferedReader(file, encoding)) {
            return loadFromJson(reader);
        }
    }

    @NotNull
    public static Settings loadFromJson(@NotNull Path file) throws IOException, JsonParseException {
        return loadFromJson(file, R.ENCODING);
    }

    @NotNull
    public static Canceller loadFromJsonAsync(@NotNull Path file, @NotNull Charset encoding, @NotNull TaskConsumer<Settings> consumer) {
        return Async.execute(() -> loadFromJson(file, encoding), consumer);
    }

    @NotNull
    public static Canceller loadFromJsonAsync(@NotNull Path file, @NotNull TaskConsumer<Settings> consumer) {
        return loadFromJsonAsync(file, R.ENCODING, consumer);
    }
}
