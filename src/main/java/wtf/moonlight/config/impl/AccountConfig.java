package wtf.moonlight.config.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import wtf.moonlight.config.Config;
import wtf.moonlight.gui.main.alt.auth.Account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class AccountConfig extends Config {
    private static final ArrayList<Account> accounts = new ArrayList<>();

    public AccountConfig(String name) {
        super(name);
    }

    @Override
    public JsonObject saveConfig() {
        JsonArray jsonArray = new JsonArray();
        for (Account account : accounts) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("refreshToken", account.getRefreshToken());
            jsonObject.addProperty("accessToken", account.getAccessToken());
            jsonObject.addProperty("username", account.getUsername());
            jsonObject.addProperty("timestamp", account.getTimestamp());
            jsonObject.addProperty("uuid",account.getUUID());
            jsonArray.add(jsonObject);
        }
        JsonObject configObject = new JsonObject();
        configObject.add("accounts", jsonArray);
        return configObject;
    }

    @Override
    public void loadConfig(JsonObject object) {
        accounts.clear();
        JsonArray jsonArray = object.getAsJsonArray("accounts");
        if (jsonArray != null) {
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                accounts.add(new Account(
                        Optional.ofNullable(jsonObject.get("refreshToken")).map(JsonElement::getAsString).orElse(""),
                        Optional.ofNullable(jsonObject.get("accessToken")).map(JsonElement::getAsString).orElse(""),
                        Optional.ofNullable(jsonObject.get("username")).map(JsonElement::getAsString).orElse(""),
                        Optional.ofNullable(jsonObject.get("timestamp")).map(JsonElement::getAsLong).orElse(System.currentTimeMillis()),
                        Optional.ofNullable(jsonObject.get("uuid")).map(JsonElement::getAsString).orElse("")
                ));
            }
        }
    }

    public static Account get(int index) {
        return accounts.get(index);
    }

    public static void add(Account account) {
        accounts.add(account);
    }

    public static void remove(int index) {
        accounts.remove(index);
    }

    public static int size() {
        return accounts.size();
    }

    public static void swap(int i, int j) {
        Collections.swap(accounts, i, j);
    }
}
