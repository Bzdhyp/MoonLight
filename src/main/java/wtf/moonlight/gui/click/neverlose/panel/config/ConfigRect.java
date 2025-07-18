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
package wtf.moonlight.gui.click.neverlose.panel.config;

import lombok.Getter;
import lombok.Setter;
import wtf.moonlight.Client;
import wtf.moonlight.config.Config;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.gui.font.Fonts;
import wtf.moonlight.util.render.animations.advanced.Animation;
import wtf.moonlight.util.render.animations.advanced.Direction;
import wtf.moonlight.util.render.animations.advanced.impl.DecelerateAnimation;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.MouseUtil;
import wtf.moonlight.util.render.RoundedUtil;


import java.awt.*;

import static wtf.moonlight.gui.click.neverlose.NeverLose.*;

@Getter
public class ConfigRect extends Component {
    private final Config config;
    @Setter
    private float posX, posY, scroll;
    @Setter
    private boolean selected;
    private final Animation hover = new DecelerateAnimation(250,1);
    private final Animation select = new DecelerateAnimation(250,1);
    public ConfigRect(Config config) {
        this.config = config;
        setHeight(36);
        hover.setDirection(Direction.BACKWARDS);
        select.setDirection(Direction.BACKWARDS);
    }
    @Override
    public void drawScreen(int mouseX, int mouseY) {
        //coordinate
        float y = getPosY() + scroll;
        //anim
        hover.setDirection(MouseUtil.isHovered2(getPosX() + 290,y + 20,60,18,mouseX,mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);
        select.setDirection(selected ? Direction.FORWARDS : Direction.BACKWARDS);
        //render
        String name = config.getName().replace(".json","") + (Client.INSTANCE.getConfigManager().getCurrentConfig().equals(config.getName()) ? " (Current Config)" : "");
        RoundedUtil.drawRoundOutline(getPosX(),y + 10,358,getHeight(),4,0.1f,bgColor,new Color(ColorUtil.interpolateColor2(categoryBgColor, categoryBgColor.brighter().brighter(), (float) select.getOutput())));
        Fonts.interSemiBold.get(17).drawString(name,posX + 8,y + 17,-1);
        //button
        RoundedUtil.drawRoundOutline(getPosX() + 290,y + 20,60,18,2,0.1f, new Color(ColorUtil.interpolateColor2(categoryBgColor, categoryBgColor.brighter().brighter(), (float) hover.getOutput())), new Color(iconRGB));
        Fonts.neverlose.get(20).drawString("k",getPosX() + 296,y + 27,-1);
        Fonts.interSemiBold.get(16).drawString("Save",getPosX() + 302 + Fonts.neverlose.get(20).getStringWidth("k"),y + 27,-1);
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (MouseUtil.isHovered2(getPosX() + 290,getPosY() + scroll + 20,60,18,mouseX,mouseY) && mouseButton == 0) {
            config.saveConfig();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
    }
    public int getMaxScroll() {
        return (int) (INSTANCE.getNeverLose().getPosY() + 80 + getHeight());
    }
    public boolean isHovered(int mouseX,int mouseY) {
        return MouseUtil.isHovered2(getPosX(),getPosY() + scroll + 10,358,getHeight(),mouseX,mouseY);
    }
}
