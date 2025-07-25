package wtf.moonlight.module.impl.display;

import com.cubk.EventTarget;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import wtf.moonlight.Client;
import wtf.moonlight.events.render.Render2DEvent;
import wtf.moonlight.gui.click.neverlose.NeverLose;
import wtf.moonlight.gui.font.FontRenderer;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.MultiBoolValue;
import wtf.moonlight.util.player.MovementUtil;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;
import wtf.moonlight.util.render.RoundedUtil;

import java.awt.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static net.minecraft.util.EnumChatFormatting.GRAY;
import static net.minecraft.util.EnumChatFormatting.WHITE;
import static wtf.moonlight.gui.click.neverlose.NeverLose.*;
import static wtf.moonlight.gui.click.neverlose.NeverLose.textRGB;

@ModuleInfo(name = "Watermark", category = Categor.Display)
public class Watermark extends Module {
    public final ListValue watemarkMode = new ListValue("Watermark Mode", new String[]{"Text","Styles 2", "Rect", "Nursultan", "Exhi", "Exhi 2",
            "Exhi 3", "Nursultan 2", "NeverLose", "Novo", "Novo 2", "Novo 3", "OneTap"}, "Text", this);

    public final MultiBoolValue nlOptions = new MultiBoolValue("NeverLose Options", Arrays.asList(
            new BoolValue("User",true),
            new BoolValue("FPS",true),
            new BoolValue("Time",false)), this, () -> watemarkMode.is("NeverLose"));

    private final DateFormat dateFormat = new SimpleDateFormat("hh:mm");
    private final DateFormat dateFormat2 = new SimpleDateFormat("hh:mm:ss");
    private final DecimalFormat bpsFormat = new DecimalFormat("0.00");
    private final DecimalFormat fpsFormat = new DecimalFormat("0");

    public Watermark() {
        setEnabled(true);
    }

    protected Interface setting;

    @Override
    public void onEnable() {
        setting = INSTANCE.getModuleManager().getModule(Interface.class);
        super.onEnable();
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (mc.gameSettings.showDebugInfo) return;

        String clientName = setting.clientName.getValue();

        if (watemarkMode.canDisplay()) {
            switch (watemarkMode.getValue()) {
                case "Text": {
                    Fonts.interBold.get(30).drawStringWithShadow(clientName, 10, 10, setting.color(0));
                }
                break;
                case "Styles 2": {
                    String dateString2 = dateFormat2.format(new Date());

                    String stylesname = getString(dateString2);

                    int x = 7;
                    int y = 7;
                    int width = Fonts.interSemiBold.get(17).getStringWidth("") + Fonts.interSemiBold.get(17).getStringWidth(stylesname) + 5;
                    int height = Fonts.interSemiBold.get(17).getHeight() + 3;

                    RoundedUtil.drawRound(x, y, width, height, 4, new Color(getModule(Interface.class).bgColor(), true));
                    Fonts.interSemiBold.get(17).drawString(stylesname, Fonts.interBold.get(17).getStringWidth("") + x + 2, y + 4.5f, new Color(setting.color(1)).getRGB());
                    break;
                }

                case "Nursultan": {
                    RoundedUtil.drawRound(7, 7.5f, 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 5, 15, 4, new Color(setting.bgColor(0)));
                    Fonts.nursultan.get(16).drawString("P", 13, 14, setting.color(0));
                    RenderUtil.drawRect(25, 10.5f, 1, 8.5f, new Color(47, 47, 47).getRGB());
                    Fonts.interMedium.get(15).drawString(INSTANCE.getVersion(), 29, 13, setting.color(0));

                    RenderUtil.drawRect(7 + 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 2.5f + 11 + 15, 10.5f, 1, 8.5f, new Color(47, 47, 47).getRGB());
                    RoundedUtil.drawRound(7 + 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 2.5f + 11, 7.5f, Fonts.interMedium.get(15).getStringWidth(mc.thePlayer.getName()) + 25, 15, 4, new Color(setting.bgColor(0)));
                    Fonts.nursultan.get(16).drawString("W", 7 + 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 2.5f + 11 + 5, 14, setting.color(0));
                    Fonts.interMedium.get(15).drawString(mc.thePlayer.getName(), 7 + 20 + Fonts.interMedium.get(15).getStringWidth(INSTANCE.getVersion()) + 2.5f + 11 + 15 + 5, 13, -1);
                }
                break;
                case "Exhi": {
                    boolean shouldChange = RenderUtil.COLOR_PATTERN.matcher(clientName).find();
                    String text = shouldChange ? "§r" + clientName : clientName.charAt(0) + "§r§f" + clientName.substring(1) +
                            " §7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r ";
                    mc.fontRendererObj.drawStringWithShadow(text, 2.0f, 2.0f, setting.color());
                }
                break;
                case "Exhi 2": {
                    boolean shouldChange = RenderUtil.COLOR_PATTERN.matcher(clientName).find();
                    String text = shouldChange ? "§r" + clientName : clientName.charAt(0) + "§r§f" + clientName.substring(1) +
                            " §7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r ";
                    Fonts.Tahoma.get(15).drawStringWithShadow(text, 1.0f, 2.0f, setting.color());
                }
                break;
                case "Exhi 3": {
                    String text = "§7§l§o§n" + clientName + "§r" +
                            " §7[§f" + ViaLoadingBase.getInstance().getTargetVersion().getName() + "§7]§r" +
                            " §7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r ";
                    mc.fontRendererObj.drawStringWithShadow(text, 2.0f, 2.0f, setting.color());
                }
                break;
                case "Nursultan 2": {
                    float posX = 7f;
                    float posY = 7.5f;
                    float fontSize = 15f;
                    float iconSize = 5.0F;
                    float rectWidth = 10.0F;
                    String title = " | MoonLight";
                    float titleWidth = Fonts.interMedium.get(fontSize).getStringWidth(title);

                    RoundedUtil.drawRound(posX, posY, rectWidth + iconSize * 2.5F + titleWidth, 15, 4.0F, new Color(getModule(Interface.class).bgColor(), true));

                    Fonts.nursultan.get(18).drawString("S", posX + iconSize, posY + 2 + iconSize - 1.0F, setting.color());

                    Fonts.interMedium.get(fontSize).drawString(title, posX + rectWidth + iconSize * 1.5F, posY + rectWidth / 2.0F + 1.5F, setting.color());

                    String playerName = mc.thePlayer.getName();
                    float playerNameWidth = Fonts.interMedium.get(fontSize).getStringWidth(playerName);
                    float playerNameX = posX + rectWidth + iconSize * 2.5F + titleWidth + iconSize;

                    RoundedUtil.drawRound(playerNameX, posY, rectWidth + iconSize * 2.5F + playerNameWidth, 15, 4.0F, new Color(getModule(Interface.class).bgColor(), true));

                    Fonts.nursultan.get(fontSize).drawString("W", playerNameX + iconSize, posY + 1 + iconSize, setting.color());

                    Fonts.interMedium.get(fontSize).drawString(playerName, playerNameX + iconSize * 1.5F + rectWidth, posY + rectWidth / 2.0F + 1.5F, -1);

                    int fps = Minecraft.getDebugFPS();
                    String fpsText = fps + " Fps";
                    float fpsTextWidth = Fonts.interMedium.get(fontSize).getStringWidth(fpsText);
                    float fpsX = playerNameX + rectWidth + iconSize * 2.5F + playerNameWidth + iconSize;

                    RoundedUtil.drawRound(fpsX, posY, rectWidth + iconSize * 2.5F + fpsTextWidth, 15, 4.0F, new Color(getModule(Interface.class).bgColor(), true));

                    Fonts.nursultan.get(18).drawString("X", fpsX + iconSize, posY + 1 + iconSize, setting.color());

                    Fonts.interMedium.get(fontSize).drawString(fpsText, fpsX + rectWidth + iconSize * 1.5F, posY + rectWidth / 2.0F + 1.5F, -1);

                    String playerPosition = (int) mc.thePlayer.posX + " " + (int) mc.thePlayer.posY + " " + (int) mc.thePlayer.posZ;
                    float positionTextWidth = Fonts.interMedium.get(fontSize).getStringWidth(playerPosition);
                    float positionY = posY + 15 + iconSize;

                    RoundedUtil.drawRound(posX, positionY, rectWidth + iconSize * 2.5F + positionTextWidth, 15, 4.0F, new Color(getModule(Interface.class).bgColor(), true));

                    Fonts.nursultan.get(18).drawString("F", posX + iconSize, positionY + 1.5F + iconSize, setting.color());

                    Fonts.interMedium.get(fontSize).drawString(playerPosition, posX + iconSize * 1.5F + rectWidth, positionY + rectWidth / 2.0F + 1.5F, -1);

                    String pingText = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() + " Ping";
                    float pingTextWidth = Fonts.interMedium.get(fontSize).getStringWidth(pingText);
                    float pingX = posX + rectWidth + iconSize * 2.5F + positionTextWidth + iconSize;

                    RoundedUtil.drawRound(pingX, positionY, rectWidth + iconSize * 2.5F + pingTextWidth, 15, 4.0F, new Color(getModule(Interface.class).bgColor(), true));

                    Fonts.nursultan.get(18).drawString("Q", pingX + iconSize, positionY + 1 + iconSize, setting.color());

                    Fonts.interMedium.get(fontSize).drawString(pingText, pingX + iconSize * 1.5F + rectWidth, positionY + rectWidth / 2.0F + 1.5F, -1);
                }
                break;
                case "Novo": {
                    float x = 1;
                    String name = clientName.charAt(0) + clientName.substring(1);
                    for (int i = 0; i < name.length(); i++) {
                        if (i == 0) {
                            Fonts.sfui.get(20).drawStringWithShadow(String.valueOf(name.charAt(i)), x, 4.0F, setting.color(0));
                        } else {
                            Fonts.sfui.get(20).drawStringWithShadow(WHITE + String.valueOf(name.charAt(i)), x, 4.0F, setting.color(0));
                        }
                        x += Fonts.sfui.get(20).getStringWidth(name.charAt(i) + "");
                    }
                    Fonts.sfui.get(20).drawStringWithShadow(GRAY + " (" + WHITE + dateFormat.format(new Date()) + GRAY + ")", x, 4.0F, setting.color());
                }
                break;

                case "Novo 3": {
                    float x = 1.0F;
                    String name = clientName.charAt(0) + clientName.substring(1);
                    for (int i = 0; i < name.length(); i++) {
                        if (i == 0) {
                            Fonts.sfui.get(20).drawStringWithShadow(String.valueOf(name.charAt(i)), x, 2.0F, setting.color(0));
                        } else {
                            Fonts.sfui.get(20).drawStringWithShadow(WHITE + String.valueOf(name.charAt(i)), x, 2.0F, setting.color(0));
                        }
                        x += Fonts.sfui.get(20).getStringWidth(name.charAt(i) + "");
                    }
                    Fonts.sfui.get(20).drawStringWithShadow(GRAY + " [" + WHITE + fpsFormat.format(Minecraft.getDebugFPS()) + " FPS" + GRAY + "] " + "[" + WHITE + bpsFormat.format(MovementUtil.getBPS()) + " BPS" + GRAY + "]", x, 2.0F, setting.color());
                }
                break;

                // not going to add shaders to this one cuz it breaks the modulelist
                case "Rect": {
                    String rectText = WHITE + " - " + dateFormat.format(new Date()) + " - " + mc.thePlayer.getName() + " - " + fpsFormat.format(Minecraft.getDebugFPS()) + " FPS";
                    float x = 9.0F;
                    String name = clientName.charAt(0) + clientName.substring(1);

                    RenderUtil.drawRect(x - 2.5f, 5.5f, 2 + Fonts.sfui.get(18).getStringWidth(clientName + " - " + dateFormat.format(new Date()) + " - " + mc.thePlayer.getName() + " - " + fpsFormat.format(Minecraft.getDebugFPS()) + " FPS"), 12, setting.bgColor());
                    if (setting.colorMode.is("Fade")) {
                        RenderUtil.drawHorizontalGradientSideways(x - 2.5f, 5.5f, 2 + Fonts.sfui.get(18).getStringWidth(clientName + rectText), 1, setting.getMainColor().getRGB(), setting.getSecondColor().getRGB());
                    } else if (setting.colorMode.is("Dynamic")) {
                        RenderUtil.drawHorizontalGradientSideways(x - 2.5f, 5.5f, 2 + Fonts.sfui.get(18).getStringWidth(clientName + rectText), 1, setting.getMainColor().getRGB(), ColorUtil.darker(setting.getMainColor().getRGB(), 0.25F));
                    } else {
                        RenderUtil.drawHorizontalGradientSideways(x - 2.5f, 5.5f, 2 + Fonts.sfui.get(18).getStringWidth(clientName + rectText), 1, setting.color(0), setting.color(90));
                    }
                    for (int i = 0; i < name.length(); i++) {
                        String newstr = WHITE + String.valueOf(name.charAt(i));
                        if (setting.colorMode.is("Fade") || setting.colorMode.is("Dynamic")) {
                            if (i == 0) {
                                Fonts.sfui.get(18).drawStringWithShadow(String.valueOf(name.charAt(i)), x - 1.0f, 9.0f, setting.getMainColor().getRGB());
                            } else {
                                Fonts.sfui.get(18).drawStringWithShadow(newstr, x - 1.0f, 9.0f, setting.color(0));
                            }
                        } else {
                            if (i == 0) {
                                Fonts.sfui.get(18).drawStringWithShadow(String.valueOf(name.charAt(i)), x - 1.0f, 9.0f, setting.color(0));
                            } else {
                                Fonts.sfui.get(18).drawStringWithShadow(newstr, x - 1.0f, 9.0f, setting.color(0));
                            }
                        }
                        x += Fonts.sfui.get(18).getStringWidth(name.charAt(i) + "");
                    }
                    Fonts.sfui.get(18).drawStringWithShadow(rectText, x - 1.0f, 9.0f, setting.color());
                }
                break;

                case "OneTap": {
                    String dateString3 = dateFormat2.format(new Date());
                    String serverip = mc.isSingleplayer() ? "localhost:25565" : !mc.getCurrentServerData().serverIP.contains(":") ? mc.getCurrentServerData().serverIP + ":25565" : mc.getCurrentServerData().serverIP;
                    String onetapinfo = "moonlight | " + Client.INSTANCE.getDiscordRP().getName() + " | " + serverip + " | " + "delay: " + mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() + "ms | " + dateString3;

                    RenderUtil.drawRect(5, 5, Fonts.interSemiBold.get(14).getStringWidth(onetapinfo) + 4, 12.5f, setting.bgColor());
                    RenderUtil.drawRoundedRect(5, 5, Fonts.interSemiBold.get(14).getStringWidth(onetapinfo) + 4, 2f, 1, setting.color(0));
                    Fonts.interSemiBold.get(14).drawStringWithShadow(onetapinfo, 7, 11f, Color.WHITE.getRGB());
                }

                break;

                case "Novo 2": {
                    String novo2Info = clientName + " " + GRAY + "(" + WHITE + dateFormat.format(new Date()) + GRAY + ")" + WHITE + " - " + INSTANCE.getVersion() + " Build";

                    RenderUtil.drawRect(5, 5, Fonts.interSemiBold.get(20).getStringWidth(novo2Info) + 4, Fonts.interSemiBold.get(20).getHeight() + Fonts.interSemiBold.get(20).getHeight() / 2f, setting.bgColor());
                    RenderUtil.drawRect(5, 5, Fonts.interSemiBold.get(20).getStringWidth(novo2Info) + 4, 1f, setting.color(0));
                    Fonts.interSemiBold.get(20).drawStringWithShadow(novo2Info, 7, 11f, Color.WHITE.getRGB());
                }
                break;
            }
        }
    }

    private String getString(String dateString2) {
        String serverString;
        if (mc.isSingleplayer()) {
            serverString = "singleplayer";
        } else
            serverString = mc.getCurrentServerData().serverIP.toLowerCase();

        return setting.clientName.getValue() + EnumChatFormatting.WHITE +
                " | " + mc.thePlayer.getName() +
                " | " + Minecraft.getDebugFPS() + "fps" +
                " | " + serverString + " | " + dateString2;
    }
}
