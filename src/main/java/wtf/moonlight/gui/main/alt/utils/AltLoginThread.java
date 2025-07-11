package wtf.moonlight.gui.main.alt.utils;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;

import static wtf.moonlight.utils.misc.InstanceAccess.mc;

public final class AltLoginThread extends Thread {
    private final String password;
    @Getter @Setter
    private String status;
    private final String username;

    public AltLoginThread(String username, String password) {
        super("Alt Login Thread");
        this.username = username;
        this.password = password;
        this.status = EnumChatFormatting.GRAY + "Waiting...";
    }

    private Session createSession(String username, String password) {
        try {
            final MicrosoftAuthResult result = (new MicrosoftAuthenticator()).loginWithCredentials(username, password);
            return new Session(result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), "microsoft");
        } catch (MicrosoftAuthenticationException e) {
            // e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        if (this.password.isEmpty()) {
            mc.session = new Session(this.username, "", "", "mojang");
            this.status = EnumChatFormatting.GREEN + "Logged in. (" + this.username + " - offline name)";
            return;
        }

        this.status = EnumChatFormatting.YELLOW + "Logging in...";
        final Session auth = createSession(username, password);

        if (auth == null) {
            this.status = EnumChatFormatting.RED + "Login failed!";
        } else {
            this.status = EnumChatFormatting.GREEN + "Logged in as " + auth.getUsername();
            mc.session = auth;
        }
    }
}

