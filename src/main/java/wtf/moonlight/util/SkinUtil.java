package wtf.moonlight.util;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;
import wtf.moonlight.util.misc.InstanceAccess;

import java.util.HashMap;
import java.util.Map;

public class SkinUtil implements InstanceAccess {
    private static final Map<String, ResourceLocation> SKIN_CACHE = new HashMap<>();

    public static ResourceLocation getResourceLocation(String uuid) {
        if (SKIN_CACHE.containsKey(uuid)) return SKIN_CACHE.get(uuid);
        String imageUrl = "http://crafatar.com/avatars/" + uuid;
        ResourceLocation resourceLocation = new ResourceLocation("skins/" + uuid + "?overlay=true");
        ThreadDownloadImageData headTexture = new ThreadDownloadImageData(null, imageUrl, null, null);
        mc.getTextureManager().loadTexture(resourceLocation, headTexture);
        SKIN_CACHE.put(uuid, resourceLocation);
        AbstractClientPlayer.getDownloadImageSkin(resourceLocation, uuid);
        return resourceLocation;
    }
}
