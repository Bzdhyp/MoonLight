package wtf.moonlight.gui.main.alt.elixir.compat;

public final class Session {
    private final String username;
    private final String uuid;
    private final String token;
    private final String type;

    public Session(String username, String uuid, String token, String type) {
        this.username = username;
        this.uuid = uuid;
        this.token = token;
        this.type = type;
    }

    public String getUsername() {
        return this.username;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getToken() {
        return this.token;
    }

    public String getType() {
        return this.type;
    }

    public Session copy(String username, String uuid, String token, String type) {
        return new Session(username, uuid, token, type);
    }

    public String toString() {
        return "Session(username=" + this.username + ", uuid=" + this.uuid + ", token=" + this.token + ", type=" + this.type + ')';
    }

    public int hashCode() {
        int result = this.username.hashCode();
        result = result * 31 + this.uuid.hashCode();
        result = result * 31 + this.token.hashCode();
        result = result * 31 + this.type.hashCode();
        return result;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Session)) {
            return false;
        }
        Session session = (Session)other;
        if (!this.username.equals(session.username)) {
            return false;
        }
        if (!this.uuid.equals(session.uuid)) {
            return false;
        }
        if (this.token.equals(session.token)) {
            return false;
        }
        return this.type.equals(session.type);
    }
}

