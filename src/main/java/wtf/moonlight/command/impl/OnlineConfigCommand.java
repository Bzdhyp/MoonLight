/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package wtf.moonlight.command.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import wtf.moonlight.Client;
import wtf.moonlight.command.Command;
import wtf.moonlight.util.DebugUtil;
import wtf.moonlight.util.misc.HttpUtil;

import java.io.IOException;
import java.util.Locale;

public class OnlineConfigCommand extends Command {
    @Override
    public String getUsage() {
        return "onlineconfig/ocf <load> <config>";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"onlineconfig", "ocf"};
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            DebugUtil.sendMessage("Usage: " + getUsage());
            return;
        }

        String url = Client.INSTANCE.getClientCloud();

        switch (args[1]) {
            case "load":
                JsonObject config;
                try {
                    config = new JsonParser().parse(HttpUtil.get(
                            url + "/configs/" + args[2].toLowerCase(Locale.getDefault())
                    )).getAsJsonObject();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (Client.INSTANCE.getConfigManager().loadOnlineConfig(Client.INSTANCE.getConfigManager().getSetting(),config)) {
                    DebugUtil.sendMessage("Loaded config: " + args[2]);
                } else {
                    DebugUtil.sendMessage("Invalid config: " + args[2]);
                }
                break;
        }
    }
}
