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
package wtf.moonlight.util;

import lombok.Getter;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiSelectWorld;
import wtf.moonlight.component.BackgroundProcess;
import wtf.moonlight.module.Module;
import wtf.moonlight.util.misc.InstanceAccess;

public class DiscordInfo implements InstanceAccess {
    private boolean running = true;
    private long timeElapsed = 0;
    @Getter
    private String name;
    @Getter
    private String id;
    @Getter
    private String smallImageText;

    public int getTotal() {
        return INSTANCE.getModuleManager().getModules().size();
    }

    public long getCount() {
        return INSTANCE.getModuleManager().getModules().stream().filter(Module::isEnabled).count();
    }

    public void init() {
        this.timeElapsed = System.currentTimeMillis();
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(discordUser -> {
            System.out.println("[Discord] Connected to user " + discordUser.username + "#" + discordUser.discriminator);
            if (discordUser.userId != null) {
                name = discordUser.username + (discordUser.discriminator.equals("0") ? "" : discordUser.discriminator);
            } else {
                System.exit(0);
            }
        }).build();

        DiscordRPC.discordInitialize("1266031153572479107", handlers, true);
        Workers.IO.execute(() -> {
            while (running) {
                int killed = BackgroundProcess.killed;
                int win = BackgroundProcess.won;
                if (mc.thePlayer != null) {
                    if (mc.isSingleplayer()) {
                        update("Ig: " + detectUsername(), "is in SinglePlayer", true);
                        updateSmallImageText(getCount() + "/" + getTotal() + " modules Enabled");
                    } else if (mc.getCurrentServerData() != null) {
                        if (ServerUtil.isOnHypixel()) {
                            update("Ig: " + detectUsername(), "Kills: " + killed + " " + "Wins: " + win, true);
                            updateSmallImageText("playing on '" + mc.getCurrentServerData().serverIP + "' with " + getCount() + "/" + getTotal() + " modules Enabled");
                        } else {
                            update("Ig: " + detectUsername(), "is playing on " + mc.getCurrentServerData().serverIP, true);
                            updateSmallImageText("raping kids | " + getCount() + "/" + getTotal() + " modules Enabled");
                        }
                    } else if (mc.currentScreen instanceof GuiDownloadTerrain) {
                        update("...", "", false);
                    }
                } else {
                    if (mc.currentScreen instanceof GuiSelectWorld) {
                        update("Selecting World...", "", false);
                    } else if (mc.currentScreen instanceof GuiMultiplayer) {
                        update("Selecting Server...", "", false);
                    } else if (mc.currentScreen instanceof GuiDownloadTerrain) {
                        update("...", "", false);
                    } else {
                        update("Idling...", "", false);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

                DiscordRPC.discordRunCallbacks();
            }
        });
    }

    public void stop() {
        running = false;
        DiscordRPC.discordShutdown();
    }

    public String detectUsername() {
        String string;
        string = mc.thePlayer.getName();

        return string;
    }

    public void update(String line1, String line2, Boolean smallImage) {
        DiscordRichPresence.Builder rpc = new DiscordRichPresence.Builder(line2).setDetails(line1).setBigImage("logo", "Moonlight [#" + INSTANCE.version + "]");
        if (smallImage) {
            rpc.setSmallImage("closer", smallImageText);
        }
        rpc.setStartTimestamps(timeElapsed);
        DiscordRPC.discordUpdatePresence(rpc.build());
    }

    public void updateSmallImageText(String text) {
        smallImageText = text;
    }
}