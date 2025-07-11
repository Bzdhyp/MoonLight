package wtf.moonlight.gui.main.alt.elixir.account;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import wtf.moonlight.gui.main.alt.elixir.compat.OAuthHandler;
import wtf.moonlight.gui.main.alt.elixir.compat.OAuthServer;
import wtf.moonlight.gui.main.alt.elixir.compat.Session;
import wtf.moonlight.gui.main.alt.elixir.exception.LoginException;
import wtf.moonlight.gui.main.alt.elixir.utils.GsonHelper;
import wtf.moonlight.gui.main.alt.elixir.utils.HttpUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicrosoftAccount extends MinecraftAccount {
    private String name = "";
    private String uuid = "";
    private String accessToken = "";
    private String refreshToken = "";
    private AuthMethod authMethod = AuthMethod.MICROSOFT;

    public static final String XBOX_PRE_AUTH_URL = "https://login.live.com/oauth20_authorize.srf?client_id=<client_id>&redirect_uri=<redirect_uri>&response_type=code&display=touch&scope=<scope>&prompt=select_account";
    public static final String XBOX_AUTH_URL = "https://login.live.com/oauth20_token.srf";
    public static final String XBOX_XBL_URL = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String XBOX_XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MC_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    public static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    public static final String XBOX_AUTH_DATA = "client_id=<client_id>&redirect_uri=<redirect_uri>&grant_type=authorization_code&code=";
    public static final String XBOX_REFRESH_DATA = "client_id=<client_id>&scope=<scope>&grant_type=refresh_token&redirect_uri=<redirect_uri>&refresh_token=";
    public static final String XBOX_XBL_DATA = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"<rps_ticket>\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
    public static final String XBOX_XSTS_DATA = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"<xbl_token>\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
    public static final String MC_AUTH_DATA = "{\"identityToken\":\"XBL3.0 x=<userhash>;<xsts_token>\"}";

    public MicrosoftAccount() {
        super("Microsoft");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        this.loadHeadResource(name);
    }

    @Override
    public Session getSession() {
        if (uuid.isEmpty() || accessToken.isEmpty()) {
            try {
                update();
            } catch (final Exception exception) {
            }
        }

        return new Session(name, uuid, accessToken, "mojang");
    }

    @Override
    public void update() throws IOException, LoginException {
        final Map<String, String> jsonPostHeader = new HashMap<>();
        jsonPostHeader.put("Content-Type", "application/json");
        jsonPostHeader.put("Accept","application/json");

        // get the microsoft access token
        JsonObject msRefreshJson = new JsonParser().parse(
                HttpUtils.toReader(HttpUtils.make(
                        XBOX_AUTH_URL, "POST", replaceKeys(authMethod, XBOX_REFRESH_DATA) + refreshToken,
                        Collections.singletonMap("Content-Type", "application/x-www-form-urlencoded"))
        )).getAsJsonObject();
        final String msAccessToken = GsonHelper.string(msRefreshJson, "access_token");
        if (msAccessToken == null) throw new LoginException("Microsoft access token is null");
        // refresh token is changed after refresh
        refreshToken = GsonHelper.string(msRefreshJson, "refresh_token");
        if (refreshToken == null) throw new LoginException("Microsoft new refresh token is null");

        // authenticate with XBL
        final JsonObject xblJson = new JsonParser().parse(HttpUtils.toReader(HttpUtils.make(XBOX_XBL_URL, "POST", XBOX_XBL_DATA.replace("<rps_ticket>", authMethod.rpsTicketRule.replace("<access_token>", msAccessToken)), jsonPostHeader))).getAsJsonObject();
        final String xblToken = GsonHelper.string(xblJson, "Token");
        if (xblToken == null) throw new LoginException("Microsoft XBL token is null");

        final String userhash = GsonHelper.string(GsonHelper.array(GsonHelper.obj(xblJson, "DisplayClaims"), "xui").get(0).getAsJsonObject(), "uhs");
        if (userhash == null) throw new LoginException("Microsoft XBL userhash is null");

        // authenticate with XSTS
        final JsonObject xstsJson = new JsonParser().parse(HttpUtils.toReader(HttpUtils.make(XBOX_XSTS_URL, "POST", XBOX_XSTS_DATA.replace("<xbl_token>", xblToken), jsonPostHeader))).getAsJsonObject();
        final String xstsToken = GsonHelper.string(xstsJson, "Token");
        if (xstsToken == null) throw new LoginException("Microsoft XSTS token is null");

        // get the minecraft access token
        final JsonObject mcJson = new JsonParser().parse(HttpUtils.toReader(HttpUtils.make(MC_AUTH_URL, "POST", MC_AUTH_DATA.replace("<userhash>", userhash).replace("<xsts_token>", xstsToken), jsonPostHeader))).getAsJsonObject();
        accessToken = GsonHelper.string(mcJson, "access_token");
        if (accessToken == null) throw new LoginException("Minecraft access token is null");

        // get the minecraft account profile
        final JsonObject mcProfileJson = new JsonParser().parse(HttpUtils.toReader(HttpUtils.make(MC_PROFILE_URL, "GET", "", Collections.singletonMap("Authorization", "Bearer " + accessToken)))).getAsJsonObject();
        this.setName(GsonHelper.string(mcProfileJson, "name"));
        uuid = GsonHelper.string(mcProfileJson, "id");
        if (name == null || uuid == null) {
            final JsonObject activeStore = new JsonParser().parse(HttpUtils.toReader(HttpUtils.make("https://api.minecraftservices.com/entitlements/mcstore", "GET", "", Collections.singletonMap("Authorization", "Bearer " + accessToken)))).getAsJsonObject();
            if (activeStore.has("items")) {
                AtomicBoolean hasMinecraft = new AtomicBoolean(false);
                final JsonArray itemsJson = activeStore.getAsJsonArray("items");
                itemsJson.forEach((j) -> {
                    if (j.getAsJsonObject().has("name") && (j.getAsJsonObject().get("name").getAsString().equals("product_minecraft") || j.getAsJsonObject().get("name").getAsString().equals("game_minecraft"))) {
                        hasMinecraft.set(true);
                    }
                });
                if (!hasMinecraft.get()) {
                    throw new LoginException("This Microsoft account dont have minecraft.");
                }
            }
            try {
                JFrame jf = new JFrame();
                jf.setAlwaysOnTop(true);
                String name = JOptionPane.showInputDialog(jf, "No minecraft profile found, please set a new name.");
                if (name == null) {
                    throw new LoginException("Minecraft name is null");
                }
                jf.dispose();
                String url = "https://api.minecraftservices.com/minecraft/profile/name/" + name;
                Map<String, String> headers = new HashMap<>();

                headers.put("Accept", "*/*");
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("User-Agent", "MojangSharp/0.1");
                headers.put("Content-Type", "application/json");

                HttpResponse response = HttpRequest.put(url)
                        .headerMap(headers, true)
                        .execute();
                if (response.getStatus() == 200 || response.getStatus() == 204) {
//                    Faiths.notificationManager.pop("Name changed!", NotificationType.SUCCESS);
                } else {
                    String cause = "Unknown";
                    switch (response.getStatus()) {
                        case 400:
                            cause = "Name is invaild";
                            break;
                        case 403:
                            cause = "Name is unlivable";
                            break;
                        case 401:
                            cause = "Unauthorized";
                            break;
                        case 429:
                            cause = "Too many requests";
                            break;
                        case 500:
                            cause = "Mojang API lags";
                            break;
                    }
//                    Faiths.notificationManager.pop("Failed to change name due to " + cause, NotificationType.ERROR);
                }
            } catch (Exception e) {
//                Faiths.notificationManager.pop("Failed to change name.", NotificationType.ERROR);
                e.printStackTrace();
            }
            final JsonObject mcJson2 = new JsonParser().parse(HttpUtils.toReader(HttpUtils.make(MC_AUTH_URL, "POST", MC_AUTH_DATA.replace("<userhash>", userhash).replace("<xsts_token>", xstsToken), jsonPostHeader))).getAsJsonObject();
            accessToken = GsonHelper.string(mcJson, "access_token");
            if (accessToken == null) throw new LoginException("Minecraft access token is null");
            final JsonObject mcProfileJson2 = new JsonParser().parse(HttpUtils.toReader(HttpUtils.make(MC_PROFILE_URL, "GET", "", Collections.singletonMap("Authorization", "Bearer " + accessToken)))).getAsJsonObject();
            this.setName(GsonHelper.string(mcProfileJson, "name"));
            uuid = GsonHelper.string(mcProfileJson, "id");
        };

    }

    @Override
    public void toRawYML(final Map<String, String> data) {
        data.put("name", this.getName());
        data.put("refreshToken", this.refreshToken);
        data.put("authMethod", AuthMethod.registry.entrySet().stream().filter(method -> method.getValue() == this.authMethod).findFirst().orElse(null).getKey());
    }

    @Override
    public void fromRawYML(final Map<String, String> data) {
        this.setName(data.get("name"));
        this.refreshToken = data.get("refreshToken");
        this.authMethod = AuthMethod.registry.get(data.get("authMethod"));
    }


    public static MicrosoftAccount buildFromAuthCode(String code, AuthMethod method) throws LoginException, IOException {
        JsonObject data = new JsonParser().parse(new InputStreamReader(HttpUtils.make(XBOX_AUTH_URL, "POST", replaceKeys(method, XBOX_AUTH_DATA) + code, Collections.singletonMap("Content-Type", "application/x-www-form-urlencoded")).getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
        if (data.has("refresh_token")) {
            MicrosoftAccount account = new MicrosoftAccount();
            account.refreshToken = data.get("refresh_token").getAsString();
            account.authMethod = method;
            account.update();
            return account;
        } else {
            throw new LoginException("Failed to get refresh token");
        }
    }

    public static MicrosoftAccount buildFromRefreshToken(String token, AuthMethod method) throws LoginException, IOException {
        MicrosoftAccount account = new MicrosoftAccount();
        account.refreshToken = token;
        account.authMethod = method;
        account.update();
        return account;
    }

    public static String findArgs(String resp, String arg) throws LoginException {
        if (resp.contains(arg)) {
            String afterArg = resp.substring(resp.indexOf(arg + ":'") + arg.length() + 2);
            return afterArg.substring(0, afterArg.indexOf("',"));
        } else {
            throw new LoginException("Failed to find argument in response " + arg);
        }
    }

    public static MicrosoftAccount buildFromPassword(String username, String password, AuthMethod authMethod) throws LoginException, IOException {
        // first, get the pre-auth url
        final HttpURLConnection preAuthConnection = HttpUtils.make(replaceKeys(authMethod, XBOX_PRE_AUTH_URL), "GET");
        final String html = HttpUtils.readText(preAuthConnection);

        List<String> setCookie = preAuthConnection.getHeaderFields().get("Set-Cookie");
        if (setCookie == null) {
            setCookie = new ArrayList<>();
        }

        String cookies = String.join(";", setCookie);

        final String urlPost = findArgs(html, "urlPost");
        String sFTTag = findArgs(html, "sFTTag");
        String ppft = sFTTag.substring(sFTTag.indexOf("value=\"") + 7, sFTTag.length() - 3);
        preAuthConnection.disconnect();
        final Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookies);
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        // then, post the login form
        final HttpURLConnection authConnection = HttpUtils.make(urlPost, "POST",
                "login="+ username + "&loginfmt=" + username + "&passwd=" + password + "&PPFT=" + ppft,
                headers);
        HttpUtils.readText(authConnection);

        String url = authConnection.getURL().toString();
        String code;
        if (!url.contains("code=")) {
            throw new LoginException("Failed to get auth code from response");
        } else {
            String pre = url.substring(url.indexOf("code=") + 5);
            code = pre.substring(0, pre.indexOf("&"));
        }

        authConnection.disconnect();

        // pass the code to [buildFromAuthCode]
        return buildFromAuthCode(code, authMethod);
    }

    public static OAuthServer buildFromOpenBrowser(final OAuthHandler handler, final AuthMethod authMethod) throws IOException {
        final OAuthServer server = new OAuthServer(handler, authMethod);
        server.start();
        return server;
    }

    public static String replaceKeys(AuthMethod method, String string) {
        return string.replace("<client_id>", method.clientId)
                .replace("<redirect_uri>", method.redirectUri)
                .replace("<scope>", method.scope);
    }

    public static class AuthMethod {
        public static final Map<String, AuthMethod> registry = new HashMap<>();

        public static final AuthMethod MICROSOFT = new AuthMethod("00000000441cc96b", "https://login.live.com/oauth20_desktop.srf", "service::user.auth.xboxlive.com::MBI_SSL", "<access_token>");
        public static final AuthMethod TOKENLOGIN = new AuthMethod("00000000402b5328", "https://login.live.com/oauth20_desktop.srf", "service::user.auth.xboxlive.com::MBI_SSL", "<access_token>");
        public static final AuthMethod AZURE_APP = new AuthMethod("0add8caf-2cc6-4546-b798-c3d171217dd9", "http://localhost:21919/login", "XboxLive.signin%20offline_access", "d=<access_token>");

        public final String clientId;
        public final String redirectUri;
        public final String scope;
        public final String rpsTicketRule;

        public AuthMethod(String clientId, String redirectUri, String scope, String rpsTicketRule) {
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.scope = scope;
            this.rpsTicketRule = rpsTicketRule;
        }

        static {
            registry.put("MICROSOFT", MICROSOFT);
            registry.put("AZURE_APP", AZURE_APP);
            registry.put("TOKENLOGIN", TOKENLOGIN);
        }
    }
}