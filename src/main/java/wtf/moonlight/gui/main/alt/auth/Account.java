package wtf.moonlight.gui.main.alt.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
  private String refreshToken;
  private String accessToken;
  private String username;
  private long timestamp;
  private String uuid;

  public Account(String refreshToken, String accessToken, String username, long timestamp, String uuid) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.username = username;
    this.timestamp = timestamp;
    this.uuid = uuid;
  }

  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }
}
