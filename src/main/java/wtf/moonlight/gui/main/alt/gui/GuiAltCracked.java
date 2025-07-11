package wtf.moonlight.gui.main.alt.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import org.lwjglx.input.Keyboard;
import wtf.moonlight.gui.main.alt.utils.AltLoginThread;
import wtf.moonlight.gui.main.alt.utils.PasswordField;

import java.io.IOException;

public final class GuiAltCracked extends GuiScreen {
    private PasswordField password;
    private final GuiScreen previousScreen;
    private AltLoginThread thread;
    private GuiTextField username;

    public GuiAltCracked(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1: {
                this.mc.displayGuiScreen(this.previousScreen);
                break;
            }
            case 0: {
                this.thread = new AltLoginThread(this.username.getText(), this.password.getText());
                this.thread.start();
            }
        }
    }

    @Override
    public void drawScreen(int x2, int y2, float partialTicks) {
        this.drawDefaultBackground();

        this.username.drawTextBox();
        this.password.drawTextBox();
        drawCenteredString(mc.fontRendererObj, "Alt Login", width / 2, 20, -1);
        drawCenteredString(mc.fontRendererObj, this.thread == null ? EnumChatFormatting.GRAY + "Idle..." : this.thread.getStatus(), width / 2, 29, -1);
        if (this.username.getText().isEmpty()) {
            this.drawString(mc.fontRendererObj, "Username / E-Mail", width / 2 - 96, 66, -7829368);
        }
        if (this.password.getText().isEmpty()) {
            this.drawString(mc.fontRendererObj, "Password", width / 2 - 96, 106, -7829368);
        }
        super.drawScreen(x2, y2, partialTicks);
    }

    @Override
    public void initGui() {
        int y = height / 4 + 24;
        this.buttonList.add(new GuiButton(0, width / 2 - 100, y + 72 + 12, "Login"));
        this.buttonList.add(new GuiButton(1, width / 2 - 100, y + 72 + 12 + 24, "Back"));
        this.username = new GuiTextField(y, mc.fontRendererObj, width / 2 - 100, 60, 200, 20);
        this.password = new PasswordField(20, mc.fontRendererObj, width / 2 - 100, 100, 200, 20);
        this.username.setFocused(true);
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void keyTyped(char character, int key) {
        try {
            super.keyTyped(character, key);
        } catch (IOException e) {
            // e.printStackTrace();
        }
        
        if (character == '\t') {
            if (!this.username.isFocused() && !this.password.isFocused()) {
                this.username.setFocused(true);
            } else {
                this.username.setFocused(this.password.isFocused());
                this.password.setFocused(!this.username.isFocused());
            }
        }
        
        if (character == '\r') {
            this.actionPerformed(this.buttonList.get(0));
        }
        
        this.username.textboxKeyTyped(character, key);
        this.password.textboxKeyTyped(character, key);
    }

    @Override
    public void mouseClicked(int x2, int y2, int button) {
        try {
            super.mouseClicked(x2, y2, button);
        } catch (IOException e) {
            // e.printStackTrace();
        }
        this.username.mouseClicked(x2, y2, button);
        this.password.mouseClicked(x2, y2, button);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen() {
        this.username.updateCursorCounter();
        this.password.updateCursorCounter();
    }
}

