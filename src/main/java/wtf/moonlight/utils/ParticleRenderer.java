package wtf.moonlight.utils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.utils.misc.InstanceAccess;
import wtf.moonlight.utils.render.Particle;

import java.util.ArrayList;
import java.util.List;

public class ParticleRenderer implements InstanceAccess {
    public static final List<Particle> particles = new ArrayList<>();
    public static int rendered;
    public static final TimerUtils timer = new TimerUtils();
    private static boolean sentParticles;

    public static void renderParticle(EntityLivingBase target, float x, float y) {

        for (Particle p : particles) {
            GlStateManager.color(1, 1, 1, 1);
            if (p.opacity > 4) p.render2D();
        }
        if (timer.hasTimeElapsed(1000 / 60)) {
            for (Particle p : particles) {
                p.updatePosition();
                if (p.opacity < 1) particles.remove(p);
            }
            timer.reset();
        }
        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 10; i++) {
                Particle particle = new Particle();
                particle.init(x + 20, y + 20, (float) (((Math.random() - 0.5) * 2) * 1.4), (float) (((Math.random() - 0.5) * 2) * 1.4),
                        (float) (MathUtils.randomizeDouble(4, 5)), i % 2 == 0 ? INSTANCE.getModuleManager().getModule(Interface.class).color(i * 100, 255) :INSTANCE.getModuleManager().getModule(Interface.class).color(-i * 100, 255));
                particles.add(particle);
            }
            sentParticles = true;
        }
        if (target.hurtTime == 8) sentParticles = false;
    }
}
