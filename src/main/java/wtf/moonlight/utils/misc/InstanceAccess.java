/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package wtf.moonlight.utils.misc;

import net.minecraft.client.Minecraft;
import wtf.moonlight.Client;

public interface InstanceAccess {

    Minecraft mc = Minecraft.getMinecraft();

    Client INSTANCE = Client.INSTANCE;
}

