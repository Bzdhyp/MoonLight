package wtf.moonlight.gui.main.alt.elixir.compat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import wtf.moonlight.gui.main.alt.elixir.account.MicrosoftAccount;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class OAuthServer {
    private final OAuthHandler handler;
    private final MicrosoftAccount.AuthMethod authMethod;
    private final HttpServer httpServer;
    private final String context;
    private final ThreadPoolExecutor threadPoolExecutor;

    public OAuthServer(OAuthHandler handler, MicrosoftAccount.AuthMethod authMethod) throws IOException {
        this.handler = handler;
        this.authMethod = authMethod;
        this.httpServer = HttpServer.create(new InetSocketAddress("localhost", 21919), 0);
        this.context = "/login";
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    /**
     * Start the server.
     */
    public void start() {
        httpServer.createContext(context, new OAuthHttpHandler(this, authMethod));
        httpServer.setExecutor(threadPoolExecutor);
        httpServer.start();
        handler.openUrl(MicrosoftAccount.replaceKeys(authMethod, MicrosoftAccount.XBOX_PRE_AUTH_URL));
    }

    /**
     * Stop the server.
     */
    public void stop(boolean isInterrupt) {
        httpServer.stop(0);
        threadPoolExecutor.shutdown();
        if (isInterrupt) {
            handler.authError("Has been interrupted");
        }
    }

    /**
     * The handler of the OAuth redirect http request.
     */
    static class OAuthHttpHandler implements HttpHandler {
        private OAuthServer server;
        private MicrosoftAccount.AuthMethod authMethod;

        OAuthHttpHandler(OAuthServer server, MicrosoftAccount.AuthMethod authMethod) {
            this.server = server;
            this.authMethod = authMethod;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            final Map<String, String> query = getQueryParams(exchange.getRequestURI().getQuery());
            if (query.containsKey("code")) {
                try {
                    server.handler.authResult(MicrosoftAccount.buildFromAuthCode(query.get("code"), authMethod));
                    response(exchange, "Login Success", 200);
                } catch (Exception e) {
                    e.printStackTrace();
                    server.handler.authError(e.toString());
                    response(exchange, "Error: " + e, 500);
                }
            } else {
                server.handler.authError("No code in the query");
                response(exchange, "No code in the query", 500);
            }
            server.stop(false);
        }

        private Map<String, String> getQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    params.put(pair.substring(0, idx), pair.substring(idx + 1));
                }
            }
            return params;
        }

        private void response(HttpExchange exchange, String message, int code) throws IOException {
            byte[] bytes = message.getBytes();
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}