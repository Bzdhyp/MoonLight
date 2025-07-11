package wtf.moonlight.gui.main.alt.elixir.compat;


import wtf.moonlight.gui.main.alt.elixir.account.MicrosoftAccount;

public interface OAuthHandler {
    /**
     * Called when the server has prepared the user for authentication
     */
    void openUrl(final String url);

    /**
     * Called when the user has completed authentication
     */
    void authResult(final MicrosoftAccount account);

    /**
     * Called when the user has cancelled the authentication process or the thread has been interrupted
     */
    void authError(final String error);
}
