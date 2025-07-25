package wtf.moonlight.gui.main.alt.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjglx.input.Keyboard;
import wtf.moonlight.config.impl.AccountConfig;
import wtf.moonlight.gui.main.alt.GuiAccountManager;
import wtf.moonlight.gui.main.alt.auth.Account;
import wtf.moonlight.gui.main.alt.auth.MicrosoftAuth;
import wtf.moonlight.gui.main.alt.auth.SessionManager;
import wtf.moonlight.gui.main.alt.utils.Notification;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class GuiMicrosoftAuth extends GuiScreen {
  private String cause = null;
  private String status = null;
  private GuiButton cancelButton = null;
  private final GuiScreen previousScreen;
  private ExecutorService executor = null;
  private CompletableFuture<Void> task = null;

  public GuiMicrosoftAuth(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    buttonList.clear();

    buttonList.add(cancelButton = new GuiButton(0, width / 2 - 100, height / 2 + fontRendererObj.FONT_HEIGHT / 2 + fontRendererObj.FONT_HEIGHT, "Cancel"));
    if (task == null) {
      if (executor == null) {
        executor = Executors.newSingleThreadExecutor();
      }
      AtomicReference<String> refreshToken = new AtomicReference<>("");
      AtomicReference<String> accessToken = new AtomicReference<>("");
      status = "§fCheck your browser to continue...§r";
      task = MicrosoftAuth.acquireMSAuthCode(executor).thenComposeAsync(msAuthCode -> {
        status = "§fAcquiring Microsoft access tokens§r";
        return MicrosoftAuth.acquireMSAccessTokens(msAuthCode, executor);
      }).thenComposeAsync(msAccessTokens -> {
        status = "§fAcquiring Xbox access token§r";
        refreshToken.set(msAccessTokens.get("refresh_token"));
        return MicrosoftAuth.acquireXboxAccessToken(msAccessTokens.get("access_token"), executor);
      }).thenComposeAsync(xboxAccessToken -> {
        status = "§fAcquiring Xbox XSTS token§r";
        return MicrosoftAuth.acquireXboxXstsToken(xboxAccessToken, executor);
      }).thenComposeAsync(xboxXstsData -> {
        status = "§fAcquiring Minecraft access token§r";
        return MicrosoftAuth.acquireMCAccessToken(xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor);
      }).thenComposeAsync(mcToken -> {
                status = "§fFetching your Minecraft profile§r";
                accessToken.set(mcToken);
                return MicrosoftAuth.login(mcToken, executor);
              }
      ).thenAccept(session -> {
        status = null;
        AccountConfig.add(new Account(refreshToken.get(), accessToken.get(), session.getUsername(), System.currentTimeMillis(), session.getProfile().getId().toString()));
        SessionManager.setSession(session);
        mc.displayGuiScreen(new GuiAccountManager(previousScreen, new Notification(String.format("§aSuccessful login! (%s)§r", session.getUsername()), 5000L)));
      }).exceptionally(error -> {
        status = String.format("§c%s§r", error.getMessage());
        cause = String.format("§c%s§r", error.getCause().getMessage());
        return null;
      });
    }
  }

  @Override
  public void onGuiClosed() {
    if (task != null && !task.isDone()) {
      task.cancel(true);
      executor.shutdownNow();
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    this.drawDefaultBackground();

    drawCenteredString(fontRendererObj, "Microsoft Authentication", width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2 - fontRendererObj.FONT_HEIGHT * 2, 11184810);
    if (status != null) {
      drawCenteredString(fontRendererObj, status, width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2, -1);
    }

    if (cause != null) {
      String causeText = cause;
      Gui.drawRect(0, height - 2 - fontRendererObj.FONT_HEIGHT - 3, 3 + this.fontRendererObj.getStringWidth(causeText) + 3, height, 0x64000000);
      drawString(fontRendererObj, cause, 3, height - 2 - fontRendererObj.FONT_HEIGHT, -1);
    }
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    if (keyCode == Keyboard.KEY_ESCAPE) {
      actionPerformed(cancelButton);
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button != null && button.id == 0) {
      mc.displayGuiScreen(new GuiAccountManager(previousScreen));
    }

  }
}
