package wtf.moonlight.gui.main.alt.utils;

import java.net.URI;

@SuppressWarnings("unused")
public class SystemUtils {
  public static void openWebLink(URI url) {
    try {
      Class<?> desktop = Class.forName("java.awt.Desktop");
      Object object = desktop.getMethod("getDesktop").invoke(null);
      desktop.getMethod("browse", URI.class).invoke(object, url);
    } catch (Throwable throwable) {
      System.err.println(throwable.getCause().getMessage());
    }
  }
}
