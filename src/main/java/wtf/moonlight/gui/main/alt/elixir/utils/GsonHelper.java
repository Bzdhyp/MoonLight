package wtf.moonlight.gui.main.alt.elixir.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class GsonHelper {
    public static void set(JsonObject object, String key, JsonElement value) {
        object.add(key, value);
    }

    public static void set(JsonObject object, String key, char value) {
        object.addProperty(key, value);
    }

    public static void set(JsonObject object, String key, Number value) {
        object.addProperty(key, value);
    }

    public static void set(JsonObject object, String key, String value) {
        object.addProperty(key, value);
    }

    public static void set(JsonObject object, String key, boolean value) {
        object.addProperty(key, value);
    }

    public static String string(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsString() : null;
    }

    public static JsonObject obj(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsJsonObject() : null;
    }

    public static JsonArray array(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsJsonArray() : null;
    }

    public static String string(JsonArray array, int index) {
        return array.size() > index ? array.get(index).getAsString() : null;
    }

    public static JsonObject obj(JsonArray array, int index) {
        return array.size() > index ? array.get(index).getAsJsonObject() : null;
    }

    public static JsonArray array(JsonArray array, int index) {
        return array.size() > index ? array.get(index).getAsJsonArray() : null;
    }
}

