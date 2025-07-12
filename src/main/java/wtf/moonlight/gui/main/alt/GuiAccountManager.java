package wtf.moonlight.gui.main.alt;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.util.Session;
import org.apache.commons.lang3.StringUtils;
import org.lwjglx.input.Keyboard;
import wtf.moonlight.Client;
import wtf.moonlight.config.impl.AccountConfig;
import wtf.moonlight.gui.main.MainMenu;
import wtf.moonlight.gui.main.alt.auth.Account;
import wtf.moonlight.gui.main.alt.auth.MicrosoftAuth;
import wtf.moonlight.gui.main.alt.auth.SessionManager;
import wtf.moonlight.gui.main.alt.gui.GuiAltCracked;
import wtf.moonlight.gui.main.alt.gui.GuiMicrosoftAuth;
import wtf.moonlight.gui.main.alt.gui.GuiSessionLogin;
import wtf.moonlight.gui.main.alt.utils.Notification;
import wtf.moonlight.util.render.RoundedUtil;
import wtf.moonlight.util.SkinUtil;
import wtf.moonlight.util.render.StencilUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class GuiAccountManager extends GuiScreen {
  private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  private int selectedAccount = -1;
  private GuiButton loginButton = null;
  private GuiButton deleteButton = null;
  private GuiButton cancelButton = null;
  private final GuiScreen previousScreen;
  private ExecutorService executor = null;
  private Notification notification = null;
  private CompletableFuture<Void> task = null; // Changed to CompletableFuture<Void> for simplicity with different login types
  private GuiAccountList guiAccountList = null;

  public GuiAccountManager(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  public GuiAccountManager(GuiScreen previousScreen, Notification notification) {
    this.previousScreen = previousScreen;
    this.notification = notification;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);

    buttonList.clear();
    buttonList.add(loginButton = new GuiButton(0, (float) this.width / 2 - 160, this.height - 48, 78, 20, "Login"));

    buttonList.add(new GuiButton(5, (float) this.width / 2 + 3, this.height - 48, 78, 20, "Token"));

    buttonList.add(new GuiButton(1, (float) this.width / 2 - 160, this.height - 24, 78, 20, "Microsoft"));
    buttonList.add(new GuiButton(4, (float) this.width / 2 + 3, this.height - 24, 78, 20, "Offline"));
    buttonList.add(new GuiButton(7, (float) this.width / 2 - 78, this.height - 24, 78, 20, "Change Skin"));

    buttonList.add(deleteButton = new GuiButton(2, (float) this.width / 2 + 84, this.height - 48, 78, 20, "Delete"));
    buttonList.add(cancelButton = new GuiButton(3, (float) this.width / 2 + 84, this.height - 24, 78, 20, "Back"));

    guiAccountList = new GuiAccountList(mc);
    guiAccountList.registerScrollButtons(11, 12);
    updateScreen();
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
    this.drawDefaultBackground(); // Draw background first
    if (guiAccountList != null) {
      guiAccountList.drawScreen(mouseX, mouseY, renderPartialTicks);
    }

    drawCenteredString(fontRendererObj, String.format("§rAccount Manager §8(§7%s§8)§r", AccountConfig.size()), width / 2, 10, -1); // Adjusted Y
    if (notification != null && !notification.isExpired()) {
      drawCenteredString(this.fontRendererObj, notification.getMessage(), mc.currentScreen.width / 2, 22, -1); // Adjusted Y
    } else {
      drawCenteredString(this.fontRendererObj, "Username: §7" + mc.getSession().getUsername(), mc.currentScreen.width / 2, 22, -1); // Adjusted Y
    }

    super.drawScreen(mouseX, mouseY, renderPartialTicks); // Draw buttons on top
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);

    if (task != null && !task.isDone()) {
      task.cancel(true);
    }
    if (executor != null && !executor.isShutdown()) { // Properly shutdown executor
      executor.shutdownNow();
      executor = null;
    }
  }

  @Override
  public void updateScreen() {
    if (loginButton != null && deleteButton != null) {
      loginButton.enabled = deleteButton.enabled = selectedAccount >= 0 && (task == null || task.isDone());
    }
    if (notification != null && notification.isExpired()) {
      notification = null; // Clear expired notification
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    if (guiAccountList != null) {
      guiAccountList.handleMouseInput();
    }
    super.handleMouseInput();
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    switch (keyCode) {
      case Keyboard.KEY_UP: {
        if (selectedAccount > 0) {
          --selectedAccount;
          guiAccountList.scrollBy(-guiAccountList.getSlotHeight()); // Scroll view
          if (isCtrlKeyDown()) {
            AccountConfig.swap(selectedAccount, selectedAccount + 1);
          }
        }
      }
      break;
      case Keyboard.KEY_DOWN: {
        if (selectedAccount < AccountConfig.size() - 1) {
          ++selectedAccount;
          guiAccountList.scrollBy(guiAccountList.getSlotHeight()); // Scroll view
          if (isCtrlKeyDown()) {
            AccountConfig.swap(selectedAccount, selectedAccount - 1);
          }
        }
      }
      break;
      case Keyboard.KEY_RETURN: {
        if (loginButton.enabled) actionPerformed(loginButton);
      }
      break;
      case Keyboard.KEY_DELETE: {
        if (deleteButton.enabled) actionPerformed(deleteButton);
      }
      break;
      case Keyboard.KEY_ESCAPE: {
        actionPerformed(cancelButton);
      }
      break;
    }

    if (isKeyComboCtrlC(keyCode) && selectedAccount >= 0 && selectedAccount < AccountConfig.size()) {
      Account acc = AccountConfig.get(selectedAccount);
      if (acc != null && !StringUtils.isBlank(acc.getUsername()) && !acc.getUsername().equals("???")) {
        setClipboardString(acc.getUsername());
        this.notification = new Notification("§aCopied username to clipboard!", 2000L);
      } else if (acc != null && !StringUtils.isBlank(acc.getAccessToken())) {
        setClipboardString(acc.getAccessToken());
        this.notification = new Notification("§aCopied access token to clipboard!", 2000L);
      }
    }
  }

  @Override
  public void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) return;

    switch (button.id) {
      case 0: { // Login
        if (task != null && !task.isDone()) return;

        if (executor == null || executor.isShutdown()) {
          executor = Executors.newSingleThreadExecutor();
        }

        final Account account = AccountConfig.get(selectedAccount);
        final String originalUsername = StringUtils.isBlank(account.getUsername()) ? "???" : account.getUsername();

        notification = new Notification(String.format("§7Logging in... (%s)§r", originalUsername), -1L);
        updateScreen();

        // Session Token Account
        if (StringUtils.isBlank(account.getRefreshToken()) && !StringUtils.isBlank(account.getAccessToken())) {
          task = CompletableFuture.runAsync(() -> {
            try {
              String token = account.getAccessToken();
              String[] playerInfo = GuiSessionLogin.getProfileInfo(token); // Validates and gets info
              SessionManager.setSession(new Session(playerInfo[0], playerInfo[1], token, "mojang"));

              account.setUsername(playerInfo[0]);
              account.setTimestamp(System.currentTimeMillis());

              this.notification = new Notification(String.format("§aLogged in as %s!§r", playerInfo[0]), 5000L);
            } catch (IOException ioe) {
              this.notification = new Notification(String.format("§cLogin failed for %s: %s§r", originalUsername,
                      ioe.getMessage() != null && ioe.getMessage().contains("401") ? "Invalid/Expired Token" : "API Error"), 5000L);
            } catch (Exception e) { // Catch other potential errors like JSON parsing
              this.notification = new Notification(String.format("§cLogin failed for %s: Error processing profile§r", originalUsername), 5000L);
              // e.printStackTrace(); // Good for debugging
            }
          }, executor).whenComplete((res, ex) -> updateScreen()); // Re-enable button
        }
        // Microsoft Account
        else if (!StringUtils.isBlank(account.getRefreshToken())) {
          final AtomicReference<String> currentRefreshToken = new AtomicReference<>(account.getRefreshToken());
          final AtomicReference<String> currentAccessToken = new AtomicReference<>(account.getAccessToken());

          CompletableFuture<Session> loginAttemptFuture = MicrosoftAuth.login(currentAccessToken.get(), executor)
                  .handle((session, error) -> { // session is Session result, error is Throwable from initial login attempt
                    if (session != null) { // Login with current access token succeeded
                      this.notification = new Notification(String.format("§aSuccessful login! (%s)§r", session.getUsername()), 5000L);
                      return CompletableFuture.completedFuture(session); // Short-circuit to thenAccept
                    }

                    if (StringUtils.isBlank(currentRefreshToken.get())) {
                      throw new RuntimeException("Current access token invalid and no refresh token available.");
                    }

                    this.notification = new Notification(String.format("§7Refreshing Microsoft access tokens... (%s)§r", originalUsername), -1L);

                    return MicrosoftAuth.refreshMSAccessTokens(currentRefreshToken.get(), executor)
                            .thenComposeAsync(msAccessTokens -> {
                              this.notification = new Notification(String.format("§7Acquiring Xbox access token... (%s)§r", originalUsername), -1L);
                              currentRefreshToken.set(msAccessTokens.get("refresh_token")); // Update for saving
                              return MicrosoftAuth.acquireXboxAccessToken(msAccessTokens.get("access_token"), executor);
                            }, executor)
                            .thenComposeAsync(xboxAccessToken -> {
                              this.notification = new Notification(String.format("§7Acquiring Xbox XSTS token... (%s)§r", originalUsername), -1L);
                              return MicrosoftAuth.acquireXboxXstsToken(xboxAccessToken, executor);
                            }, executor)
                            .thenComposeAsync(xboxXstsData -> {
                              this.notification = new Notification(String.format("§7Acquiring Minecraft access token... (%s)§r", originalUsername), -1L);
                              return MicrosoftAuth.acquireMCAccessToken(xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor);
                            }, executor)
                            .thenComposeAsync(mcToken -> {
                              this.notification = new Notification(String.format("§7Fetching your Minecraft profile... (%s)§r", originalUsername), -1L);
                              currentAccessToken.set(mcToken); // This is the new Minecraft access token
                              return MicrosoftAuth.login(mcToken, executor);
                            }, executor);
                  })
                  .thenCompose(Function.identity());

          task = loginAttemptFuture.thenAccept(finalSession -> {
            account.setRefreshToken(currentRefreshToken.get());
            account.setAccessToken(finalSession.getToken());
            account.setUsername(finalSession.getUsername());
            account.setTimestamp(System.currentTimeMillis());
            SessionManager.setSession(finalSession);

            if (this.notification == null || !this.notification.getMessage().startsWith("§aSuccessful login")) {
              this.notification = new Notification(String.format("§aSuccessful login! (%s)§r", finalSession.getUsername()), 5000L);
            }
          }).exceptionally(ex -> {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex; // Unwrap CompletionException
            this.notification = new Notification(String.format("§cLogin failed for %s: %s§r", originalUsername, cause.getMessage()), 5000L);
            return null; // Required for exceptionally
          }).whenComplete((res, ex) -> updateScreen()); // Re-enable button
        } else {
          notification = new Notification(String.format("§cCannot login: Account %s has no token information.§r", originalUsername), 5000L);
          updateScreen(); // Re-enable button as no async task was started
        }
      }
      break;
      case 1: { // Add Microsoft
        mc.displayGuiScreen(new GuiMicrosoftAuth(this.previousScreen)); // Use this.previousScreen for consistency
      }
      break;
      case 2: { // Delete
        if (selectedAccount >= 0 && selectedAccount < AccountConfig.size()) {
          AccountConfig.remove(selectedAccount);
          if (selectedAccount >= AccountConfig.size() && AccountConfig.size() > 0) { // If last element was deleted
            selectedAccount = AccountConfig.size() - 1;
          } else if (AccountConfig.size() == 0) {
            selectedAccount = -1;
          }
        }
        updateScreen();
      }
      break;
      case 3: { // Cancel/Back
        this.mc.displayGuiScreen(this.previousScreen instanceof MainMenu ? this.previousScreen : new MainMenu());
      }
      break;
      case 4: { // Offline
        mc.displayGuiScreen(new GuiAltCracked(this));
      }
      break;
      case 5: { // Token Login GUI
        mc.displayGuiScreen(new GuiSessionLogin(this));
      }
      break;
      case 7: { // Change Skin
        try {
          JFileChooser jFileChooser = new JFileChooser() {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
              // intercept the dialog created by JFileChooser
              JDialog dialog = super.createDialog(parent);
              dialog.setModal(true);
              dialog.setAlwaysOnTop(true);
              return dialog;
            }
          };
          int returnVal = jFileChooser.showOpenDialog(null);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            File skinFile = jFileChooser.getSelectedFile();
            String url = "https://api.minecraftservices.com/minecraft/profile/skins";
            Map<String, String> headers = new HashMap<>();

            if (!skinFile.getName().endsWith(".png")) {
              SwingUtilities.invokeLater(() -> this.notification = new Notification("Its seems that the file isn't a skin..", 2000L));
              break;
            }

            // Skin file
            int result = JOptionPane.showConfirmDialog(null, "Is this a slim skin?", "alert", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.CANCEL_OPTION) break;
            String skinType;
            if (result == JOptionPane.YES_OPTION) {
              skinType = "slim";
            } else {
              skinType = "classic";
            }

            headers.put("Accept", "*/*");
            headers.put("Authorization", "Bearer " + mc.getSession().getToken());
            headers.put("User-Agent", "MojangSharp/0.1");

            HttpResponse response = HttpRequest.post(url)
                    .headerMap(headers, true)
                    .form("variant", skinType)
                    .form("file", skinFile)
                    .execute();
            if (response.getStatus() == 200 || response.getStatus() == 204) {
              SwingUtilities.invokeLater(() -> this.notification = new Notification("Skin changed!", 2000L));
            } else {
              SwingUtilities.invokeLater(() -> this.notification = new Notification("Failed to change skin.", 2000L));
              Client.LOGGER.error(response);
            }
          }
        } catch (Exception e) {
          SwingUtilities.invokeLater(() -> this.notification = new Notification("Failed to change skin.", 2000L));
          e.printStackTrace();
        }
        break;
      }
      default: {
        if (guiAccountList != null) {
          guiAccountList.actionPerformed(button);
        }
      }
    }
  }

  class GuiAccountList extends GuiSlot {
    public GuiAccountList(Minecraft mc) {
      super(mc, GuiAccountManager.this.width, GuiAccountManager.this.height, 32, GuiAccountManager.this.height - 64, 27);
    }

    @Override
    protected int getSize() {
      return AccountConfig.size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
      if (slotIndex < 0 || slotIndex >= getSize()) return;
      GuiAccountManager.this.selectedAccount = slotIndex;
      GuiAccountManager.this.updateScreen();
      if (isDoubleClick && loginButton.enabled) {
        GuiAccountManager.this.actionPerformed(loginButton);
      }
    }

    @Override
    protected boolean isSelected(int slotIndex) {
      return slotIndex == GuiAccountManager.this.selectedAccount;
    }

    @Override
    protected int getContentHeight() {
      return this.getSize() * this.slotHeight;
    }

    @Override
    protected void drawBackground() {
      GuiAccountManager.this.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int entryID, int x, int y, int k, int mouseXIn, int mouseYIn) {
      if (entryID < 0 || entryID >= getSize()) return; // Bounds check
      Account account = AccountConfig.get(entryID);

      String username = StringUtils.isBlank(account.getUsername()) ? "???" : account.getUsername();

      String accountType = "";
      String accountTypeColor = "§7";

      if (!StringUtils.isBlank(account.getRefreshToken())) {
        accountType = " (Microsoft)";
        accountTypeColor = "§9";
      } else if (!StringUtils.isBlank(account.getAccessToken())) {
        accountType = " (Token)";
        accountTypeColor = "§6";
      } else if (username.equals("???")) {
        accountTypeColor = "§7";
      }
      String displayName = String.format("%s%s%s§r",
              SessionManager.getSession().getUsername().equals(username) ? "§a§l" : accountTypeColor,
              username,
              accountType
      );

      GuiAccountManager.this.drawString(GuiAccountManager.this.fontRendererObj, displayName, x + 30, y + 3, -1);

      String time = String.format("§8§o%s§r", sdf.format(new Date(account.getTimestamp())));

      renderHead(x + 3, y + 1f, 21, account.getUUID());
      GuiAccountManager.this.drawString(GuiAccountManager.this.fontRendererObj, time, x + 30, y + 14, -1);
    }
  }

  private void renderHead(final double x, final double y, final int size, String uuid) {
    if (uuid == null || uuid.isEmpty()) {
      uuid = "8667ba71-b85a-4004-af54-457a9734eed7";
    }
    StencilUtil.initStencilToWrite();
    RoundedUtil.drawRound((float) x, (float) y, size, size, 2.5f, Color.WHITE);
    StencilUtil.readStencilBuffer(1);
    mc.getTextureManager().bindTexture(SkinUtil.getResourceLocation(uuid));
    Gui.drawModalRectWithCustomSizedTexture((float) x, (float) y, 0.0f, 0.0f, size, size, size, size);
    StencilUtil.uninitStencilBuffer();
  }
}