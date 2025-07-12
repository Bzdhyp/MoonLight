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
package wtf.moonlight.module.impl.visual;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import com.cubk.EventTarget;
import wtf.moonlight.events.player.UpdateEvent;
import wtf.moonlight.events.render.Render3DEvent;
import wtf.moonlight.module.Module;
import wtf.moonlight.module.Categor;
import wtf.moonlight.module.ModuleInfo;
import wtf.moonlight.module.impl.display.Interface;
import wtf.moonlight.module.values.impl.BoolValue;
import wtf.moonlight.module.values.impl.ListValue;
import wtf.moonlight.module.values.impl.SliderValue;
import wtf.moonlight.util.animations.advanced.impl.SmoothStepAnimation;
import wtf.moonlight.util.MathUti;
import wtf.moonlight.util.render.ColorUtil;
import wtf.moonlight.util.render.RenderUtil;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Particles", category = Categor.Visual)
public class Particles extends Module {
    private final ListValue physics = new ListValue("Physics", new String[]{"Drop", "Fly"}, "Fly", this);
    public final ListValue mode = new ListValue("Mode", new String[]{"Bloom", "Stars", "Hearts", "Dollars", "SnowFlake"}, "SnowFlake", this);

    private final BoolValue lime = new BoolValue("Lime", false, this);
    private final BoolValue lighting = new BoolValue("Lighting", false, this);
    private final SliderValue size = new SliderValue("Size", 7.0f, 1.0f, 200.0f, this);
    private final SliderValue count = new SliderValue("Count", 100, 20, 1000, this);

    private final ArrayList<Particles.FirePart> FIRE_PARTS_LIST = new ArrayList<>();
    private final Tessellator tessellator = Tessellator.getInstance();
    private final WorldRenderer buffer = this.tessellator.getWorldRenderer();

    private int getPartColor(FirePart part) {
        return getModule(Interface.class).color(0, (int) (part.animation.getOutput() * 255));
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.thePlayer != null && mc.thePlayer.ticksExisted == 1) {
            this.FIRE_PARTS_LIST.forEach(Particles.FirePart::setToRemove);
        }

        this.FIRE_PARTS_LIST.forEach(Particles.FirePart::updatePart);
        this.FIRE_PARTS_LIST.removeIf(Particles.FirePart::isToRemove);

        while (FIRE_PARTS_LIST.size() < count.getValue()) {
            Vec3 spawnPos;
            FirePart newPart;

            spawnPos = mc.thePlayer.getPositionVector().addVector(
                    MathUti.randomizeDouble(-72.0, 72.0), // X
                    MathUti.randomizeDouble(-40.0, 80.0), // Y
                    MathUti.randomizeDouble(-85.0, 85.0) // Z
            );

            if (physics.is("Drop")) {
                newPart = new FirePart(spawnPos);
                newPart.motionX = 0;
                newPart.motionZ = 0;
                newPart.motionY = (float) MathUti.randomizeDouble(-0.08f, -0.03f);
            } else {
                newPart = new FirePart(spawnPos);
                newPart.motionX = (float) MathUti.randomizeDouble(-0.4f, 0.4f);
                newPart.motionZ = (float) MathUti.randomizeDouble(-0.4f, 0.4f);
                newPart.motionY = (float) MathUti.randomizeDouble(-0.1f, 0.1f);
            }

            FIRE_PARTS_LIST.add(newPart);
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!this.FIRE_PARTS_LIST.isEmpty()) {
            this.setupGLDrawsFireParts(() -> {
                switch (mode.getValue()) {
                    case "Bloom": {
                        this.bindResource(new ResourceLocation("moonlight/texture/fireflies/firefly.png"));
                        break;
                    }
                    case "Stars": {
                        this.bindResource(new ResourceLocation("moonlight/texture/fireflies/star.png"));
                        break;
                    }
                    case "Hearts": {
                        this.bindResource(new ResourceLocation("moonlight/texture/fireflies/heart.png"));
                        break;
                    }
                    case "Dollars": {
                        this.bindResource(new ResourceLocation("moonlight/texture/fireflies/dollar.png"));
                        break;
                    }
                    case "SnowFlake": {
                        this.bindResource(new ResourceLocation("moonlight/texture/fireflies/snowflake.png"));
                        break;
                    }
                }
                this.FIRE_PARTS_LIST.forEach(part -> this.drawPart(part, event.partialTicks()));
            });
        }
    }

    private void bindResource(ResourceLocation toBind) {
        mc.getTextureManager().bindTexture(toBind);
    }

    private void drawBindedTexture(float x, float y, float x2, float y2, int c) {
        this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        this.buffer.pos(x, y).tex(0.0, 0.0).color(c).endVertex();
        this.buffer.pos(x, y2).tex(0.0, 1.0).color(c).endVertex();
        this.buffer.pos(x2, y2).tex(1.0, 1.0).color(c).endVertex();
        this.buffer.pos(x2, y).tex(1.0, 0.0).color(c).endVertex();
        this.tessellator.draw();
    }

    private void drawPart(Particles.FirePart part, float pTicks) {
        if (lime.get()) {
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            this.drawSparkPartsList(getPartColor(part), part, pTicks);
            this.drawTrailPartsList(getPartColor(part), part);
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        }

        Vec3 pos = part.getRenderPosVec(pTicks);
        GL11.glPushMatrix();
        GL11.glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);
        GL11.glNormal3d(1.0, 1.0, 1.0);
        GL11.glRotated(-Particles.mc.getRenderManager().playerViewY, 0.0, 1.0, 0.0);
        GL11.glRotated(Particles.mc.getRenderManager().playerViewX, Particles.mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0, 0.0, 0.0);
        GL11.glScaled(-0.1, -0.1, 0.1);
        float scale = size.getValue();
        this.drawBindedTexture(-scale / 2.0f, -scale / 2.0f, scale / 2.0f, scale / 2.0f, getPartColor(part));
        if (this.lighting.get()) {
            this.drawBindedTexture(-(scale *= 3.0f) / 2.0f, -scale / 2.0f, scale / 2.0f, scale / 2.0f, ColorUtil.applyOpacity(ColorUtil.darker(getPartColor(part), 0.2f), (float) (part.animation.getOutput() / 7.0f)));
        }
        GL11.glPopMatrix();
    }

    private void drawSparkPartsList(int color, Particles.FirePart firePart, float partialTicks) {
        if (firePart.SPARK_PARTS.size() < 2) {
            return;
        }
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glDisable(3008);
        GL11.glEnable(2832);
        GL11.glPointSize(1.5f + 6.0f * MathHelper.clamp_float(1.0f - (mc.thePlayer.getSmoothDistanceToCoord((float) firePart.getPosVec().xCoord, (float) firePart.getPosVec().yCoord + 1.6f, (float) firePart.getPosVec().zCoord) - 3.0f) / 10.0f, 0.0f, 1.0f));
        GL11.glBegin(0);
        for (Particles.SparkPart spark : firePart.SPARK_PARTS) {
            RenderUtil.color(color);
            double x = spark.prevPosX + (spark.posX - spark.prevPosX) * partialTicks;
            double y = spark.prevPosY + (spark.posY - spark.prevPosY) * partialTicks;
            double z = spark.prevPosZ + (spark.posZ - spark.prevPosZ) * partialTicks;
            GL11.glVertex3d(x, y, z);
        }
        GL11.glEnd();
        GlStateManager.resetColor();
        GL11.glEnable(3008);
        GL11.glEnable(3553);
    }


    private void setupGLDrawsFireParts(Runnable partsRender) {
        double glX = mc.getRenderManager().viewerPosX;
        double glY = mc.getRenderManager().viewerPosY;
        double glZ = mc.getRenderManager().viewerPosZ;
        GL11.glPushMatrix();
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        Particles.mc.entityRenderer.disableLightmap();
        GL11.glEnable(3042);
        GL11.glLineWidth(1.0f);
        GL11.glEnable(3553);
        GL11.glDisable(2896);
        GL11.glShadeModel(7425);
        GL11.glDisable(3008);
        GL11.glDisable(2884);
        GL11.glDepthMask(false);
        GL11.glTranslated(-glX, -glY, -glZ);
        partsRender.run();
        GL11.glTranslated(glX, glY, glZ);
        GL11.glDepthMask(true);
        GL11.glEnable(2884);
        GL11.glEnable(3008);
        GL11.glLineWidth(1.0f);
        GL11.glShadeModel(7424);
        GL11.glEnable(3553);
        GlStateManager.resetColor();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glPopMatrix();
    }

    private void drawTrailPartsList(int color, Particles.FirePart firePart) {
        if (firePart.TRAIL_PARTS.size() < 2) {
            return;
        }
        GL11.glDisable(3553);
        GL11.glLineWidth(1.0E-5f + 8.0f * MathHelper.clamp_float(1.0f - (mc.thePlayer.getSmoothDistanceToCoord((float) firePart.getPosVec().xCoord, (float) firePart.getPosVec().yCoord + 1.6f, (float) firePart.getPosVec().zCoord) - 3.0f) / 20.0f, 0.0f, 1.0f));
        GL11.glEnable(3042);
        GL11.glDisable(3008);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glBegin(3);
        for (Particles.TrailPart trail : firePart.TRAIL_PARTS) {
            RenderUtil.color(color);
            GL11.glVertex3d(trail.x, trail.y, trail.z);
        }
        GL11.glEnd();
        GlStateManager.resetColor();
        GL11.glEnable(3008);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glLineWidth(1.0f);
        GL11.glEnable(3553);
    }

    private class FirePart {
        List<Particles.TrailPart> TRAIL_PARTS;
        List<Particles.SparkPart> SPARK_PARTS = new ArrayList<>();
        Vec3 prevPos;
        Vec3 pos;
        public SmoothStepAnimation animation = new SmoothStepAnimation(400,1);
        float motionX, motionY, motionZ;
        protected int age = (int) MathUti.randomizeDouble(100, 300);
        @Getter
        boolean toRemove;

        public FirePart(Vec3 pos) {
            this.pos = pos;
            this.prevPos = pos;
            TRAIL_PARTS = new ArrayList<>();
        }

        public Vec3 getPosVec() {
            return this.pos;
        }

        public Vec3 getRenderPosVec(float pTicks) {
            Vec3 pos = this.getPosVec();
            return pos.addVector(-(this.prevPos.xCoord - pos.xCoord) * (double) pTicks, -(this.prevPos.yCoord - pos.yCoord) * (double) pTicks, -(this.prevPos.zCoord - pos.zCoord) * (double) pTicks);
        }

        public void updatePart() {
            double distanceSq = mc.thePlayer.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord);
            if (distanceSq > 4096) {
                age -= 8;
            } else if (!mc.theWorld.isAirBlock(new BlockPos(pos.xCoord, pos.yCoord, pos.zCoord))) {
                age -= 8;
            } else {
                age--;
            }

            this.prevPos = this.pos;
            this.pos = this.pos.addVector(motionX, motionY, motionZ);

            if (physics.is("Drop")) {
                motionX *= 0.98f;
                motionZ *= 0.98f;
                motionY -= 0.005f;

                if (pos.yCoord < mc.thePlayer.posY + 5) {
                    motionY *= 0.95f;
                }
            } else {
                motionX *= 0.99f;
                motionZ *= 0.99f;
                motionY *= 0.99f;

                if (mc.theWorld.rand.nextInt(20) == 0) {
                    motionX += (mc.theWorld.rand.nextFloat() - 0.5f) * 0.02f;
                    motionZ += (mc.theWorld.rand.nextFloat() - 0.5f) * 0.02f;
                    motionY += (mc.theWorld.rand.nextFloat() - 0.5f) * 0.01f;
                }
            }

            if (age <= 0) this.setToRemove();

            if (!this.TRAIL_PARTS.isEmpty()) {
                this.TRAIL_PARTS.removeIf(trailPart -> trailPart.age <= 0);
            }
            if (!this.SPARK_PARTS.isEmpty()) {
                this.SPARK_PARTS.removeIf(sparkPart -> sparkPart.age <= 0);
            }

            this.TRAIL_PARTS.add(new TrailPart(this));

            for (int i = 0; i < 2; ++i) {
                this.SPARK_PARTS.add(new SparkPart(this));
            }
            this.SPARK_PARTS.forEach(Particles.SparkPart::motionSparkProcess);
        }

        public void setToRemove() {
            this.toRemove = true;
        }
    }

    private static class TrailPart {
        double x, y, z;
        int age = 30;

        public TrailPart(Particles.FirePart part) {
            this.x = part.getPosVec().xCoord;
            this.y = part.getPosVec().yCoord;
            this.z = part.getPosVec().zCoord;
        }
    }

    private static class SparkPart {
        double posX, posY, posZ;
        double prevPosX, prevPosY, prevPosZ;
        double speed = Math.random() / 30.0;
        double radianYaw = Math.random() * 360.0;
        double radianPitch = -90.0 + Math.random() * 180.0;
        int age = 15;

        SparkPart(Particles.FirePart part) {
            this.posX = part.getPosVec().xCoord;
            this.posY = part.getPosVec().yCoord;
            this.posZ = part.getPosVec().zCoord;
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
        }

        void motionSparkProcess() {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            double radYaw = Math.toRadians(this.radianYaw);
            this.posX += Math.sin(radYaw) * this.speed;
            this.posY += Math.cos(Math.toRadians(this.radianPitch - 90.0)) * this.speed;
            this.posZ += Math.cos(radYaw) * this.speed;

            age--;
        }
    }
}