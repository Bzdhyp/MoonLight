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
package wtf.moonlight.gui.click.neverlose.components.espelements;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import wtf.moonlight.module.impl.visual.ESP2D;
import wtf.moonlight.gui.click.Component;
import wtf.moonlight.util.MathUtil;
import wtf.moonlight.util.render.RenderUtil;

import java.util.ArrayList;
import java.util.List;

public class NameElement extends Component {
    private int x;
    private int y;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
            x = INSTANCE.getNeverLose().espPreviewComponent.getPosX() + INSTANCE.getNeverLose().getWidth() + 120;
            y = (int) (INSTANCE.getNeverLose().espPreviewComponent.getPosY() + 35 + 75 * (1 - INSTANCE.getNeverLose().espPreviewComponent.getElementsManage().open.getOutput()));

        if (INSTANCE.getModuleManager().getModule(ESP2D.class).tags.get()) {
            final FontRenderer fontRenderer = mc.fontRendererObj;
            final String name = mc.thePlayer.getDisplayName().getFormattedText() + " " + (MathUtil.roundToHalf(mc.thePlayer.getHealth())) + EnumChatFormatting.RED + "❤";

            if (INSTANCE.getModuleManager().getModule(ESP2D.class).tagsBackground.get()) {

                RenderUtil.drawRect(
                        (x - fontRenderer.getStringWidth(name) / 2f * INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue()),
                        (y - 2),
                        fontRenderer.getStringWidth(name) * INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue(),
                        fontRenderer.FONT_HEIGHT * INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue() + 1,
                        0x96000000);
            }
            fontRenderer.drawScaledString(name, (x - fontRenderer.getStringWidth(name) / 2f * INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue()), y, INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue(), -1);


            if (INSTANCE.getModuleManager().getModule(ESP2D.class).item.get()) {
                List<ItemStack> items = new ArrayList<>();
                if (mc.thePlayer.getHeldItem() != null) {
                    items.add(mc.thePlayer.getHeldItem());
                }
                for (int index = 3; index >= 0; index--) {
                    ItemStack stack = mc.thePlayer.inventory.armorInventory[index];
                    if (stack != null) {
                        items.add(stack);
                    }
                }
                float armorX = x - fontRenderer.getStringWidth(name) / 2f - ((float) (items.size() * 18) / 2) * INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue();

                for (ItemStack stack : items) {
                    RenderUtil.renderItemStack(stack, armorX, y - 25 * INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue(), INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue() + INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue() / 2, true);
                    armorX += 18 * INSTANCE.getModuleManager().getModule(ESP2D.class).tagsSize.getValue();
                }
            }
        }
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
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
}