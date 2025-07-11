package wtf.moonlight.gui.main.alt.utils;

import lombok.Getter;

@SuppressWarnings("unused")
public class Notification {
  @Getter
  private final String message;
  private final long duration;
  private final long startTime;

  public Notification(String message, long duration) {
    this.message = message;
    this.duration = duration;
    this.startTime = System.currentTimeMillis();
  }

  public boolean isExpired() {
    return duration >= 0 && duration < System.currentTimeMillis() - startTime;
  }
}
