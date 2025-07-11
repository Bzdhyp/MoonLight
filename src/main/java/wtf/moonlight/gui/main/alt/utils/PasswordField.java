package wtf.moonlight.gui.main.alt.utils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public final class PasswordField extends GuiTextField {
    public PasswordField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {
        super(componentId, fontrendererObj, x, y, width, height);
    }

    @Override
    public void drawTextBox() {
        String s = this.getText();
        this.setText(this.getText());
        super.drawTextBox();
        this.setText(s);
    }
}

