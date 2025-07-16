package net.minecraft.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.optifine.DynamicLights;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;
import wtf.moonlight.Client;
import wtf.moonlight.component.SpoofSlotComponent;
import wtf.moonlight.module.impl.combat.KillAura;
import wtf.moonlight.module.impl.visual.Animations;
import wtf.moonlight.module.impl.visual.Camera;

public class ItemRenderer {
    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");

    /** A reference to the Minecraft object. */
    private final Minecraft mc;
    private ItemStack itemToRender;

    /**
     * How far the current item has been equipped (0 disequipped and 1 fully up)
     */
    private float equippedProgress;
    private float prevEquippedProgress;
    private final RenderManager renderManager;
    private final RenderItem itemRenderer;

    /** The index of the currently held item (0-8, or -1 if not yet updated) */
    private int equippedItemSlot = -1;

    public ItemRenderer(Minecraft mcIn) {
        this.mc = mcIn;
        this.renderManager = mcIn.getRenderManager();
        this.itemRenderer = mcIn.getRenderItem();
    }

    public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform) {
        if (heldStack != null) {
            Item item = heldStack.getItem();
            Block block = Block.getBlockFromItem(item);
            GL11.glPushMatrix();
            if (this.itemRenderer.shouldRenderItemIn3D(heldStack)) {
                GL11.glScalef(2.0f, 2.0f, 2.0f);
                if (!(!this.isBlockTranslucent(block) || Config.isShaders() && Shaders.renderItemKeepDepthMask)) {
                    GlStateManager.depthMask(false);
                }
            }
            this.itemRenderer.renderItemModelForEntity(heldStack, entityIn, transform);
            if (this.isBlockTranslucent(block)) {
                GlStateManager.depthMask(true);
            }
            GL11.glPopMatrix();
        }
    }

    /**
     * Returns true if given block is translucent
     */
    private boolean isBlockTranslucent(Block blockIn) {
        return blockIn != null && blockIn.getBlockLayer() == EnumWorldBlockLayer.TRANSLUCENT;
    }

    private void rotateArroundXAndY(float angle, float angleY) {
        GL11.glPushMatrix();
        GL11.glRotatef(angle, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
    }

    private void setLightMapFromPlayer(AbstractClientPlayer clientPlayer) {
        int i = this.mc.theWorld.getCombinedLight(new BlockPos(clientPlayer.posX, clientPlayer.posY + (double)clientPlayer.getEyeHeight(), clientPlayer.posZ), 0);
        if (Config.isDynamicLights()) {
            i = DynamicLights.getCombinedLight(this.mc.getRenderViewEntity(), i);
        }
        float f = i & 0xFFFF;
        float f2 = i >> 16;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f2);
    }

    private void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks) {
        float f = entityplayerspIn.prevRenderArmPitch + (entityplayerspIn.renderArmPitch - entityplayerspIn.prevRenderArmPitch) * partialTicks;
        float f2 = entityplayerspIn.prevRenderArmYaw + (entityplayerspIn.renderArmYaw - entityplayerspIn.prevRenderArmYaw) * partialTicks;
        GL11.glRotatef((entityplayerspIn.rotationPitch - f) * 0.1f, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef((entityplayerspIn.rotationYaw - f2) * 0.1f, 0.0f, 1.0f, 0.0f);
    }

    private float getMapAngleFromPitch(float pitch) {
        float f = 1.0f - pitch / 45.0f + 0.1f;
        f = MathHelper.clamp_float(f, 0.0f, 1.0f);
        f = -MathHelper.cos(f * (float)Math.PI) * 0.5f + 0.5f;
        return f;
    }

    private void renderRightArm(RenderPlayer renderPlayerIn) {
        GL11.glPushMatrix();
        GL11.glRotatef(54.0f, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(64.0f, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(-62.0f, 0.0f, 0.0f, 1.0f);
        GL11.glTranslatef(0.25f, -0.85f, 0.75f);
        renderPlayerIn.renderRightArm(this.mc.thePlayer);
        GL11.glPopMatrix();
    }

    private void renderLeftArm(RenderPlayer renderPlayerIn) {
        GL11.glPushMatrix();
        GL11.glRotatef(92.0f, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(45.0f, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(41.0f, 0.0f, 0.0f, 1.0f);
        GL11.glTranslatef(-0.3f, -1.1f, 0.45f);
        renderPlayerIn.renderLeftArm(this.mc.thePlayer);
        GL11.glPopMatrix();
    }

    private void renderPlayerArms(AbstractClientPlayer clientPlayer) {
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        Object render = this.renderManager.getEntityRenderObject(this.mc.thePlayer);
        if (render instanceof RenderPlayer renderplayer) {
            if (!clientPlayer.isInvisible()) {
                GlStateManager.disableCull();
                this.renderRightArm(renderplayer);
                this.renderLeftArm(renderplayer);
                GlStateManager.enableCull();
            }
        }
    }

    private void renderItemMap(AbstractClientPlayer clientPlayer, float p_178097_2_, float p_178097_3_, float p_178097_4_) {
        float f = -0.4f * MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float)Math.PI);
        float f2 = 0.2f * MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float)Math.PI * 2.0f);
        float f3 = -0.2f * MathHelper.sin(p_178097_4_ * (float)Math.PI);
        GL11.glTranslatef(f, f2, f3);
        float f4 = this.getMapAngleFromPitch(p_178097_2_);
        GL11.glTranslatef(0.0f, 0.04f, -0.72f);
        GL11.glTranslatef(0.0f, p_178097_3_ * -1.2f, 0.0f);
        GL11.glTranslatef(0.0f, f4 * -0.5f, 0.0f);
        GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(f4 * -85.0f, 0.0f, 0.0f, 1.0f);
        GL11.glRotatef(0.0f, 1.0f, 0.0f, 0.0f);
        this.renderPlayerArms(clientPlayer);
        float f5 = MathHelper.sin(p_178097_4_ * p_178097_4_ * (float)Math.PI);
        float f6 = MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float)Math.PI);
        GL11.glRotatef(f5 * -20.0f, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(f6 * -20.0f, 0.0f, 0.0f, 1.0f);
        GL11.glRotatef(f6 * -80.0f, 1.0f, 0.0f, 0.0f);
        GL11.glScalef(0.38f, 0.38f, 0.38f);
        GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
        GL11.glRotatef(0.0f, 1.0f, 0.0f, 0.0f);
        GL11.glTranslatef(-1.0f, -1.0f, 0.0f);
        GL11.glScalef(0.015625f, 0.015625f, 0.015625f);
        this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GL11.glNormal3f(0.0f, 0.0f, -1.0f);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-7.0, 135.0, 0.0).tex(0.0, 1.0).endVertex();
        worldrenderer.pos(135.0, 135.0, 0.0).tex(1.0, 1.0).endVertex();
        worldrenderer.pos(135.0, -7.0, 0.0).tex(1.0, 0.0).endVertex();
        worldrenderer.pos(-7.0, -7.0, 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();
        MapData mapdata = Items.filled_map.getMapData(this.itemToRender, this.mc.theWorld);
        if (mapdata != null) {
            this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
        }
    }

    /**
     * Render the player's arm
     *
     * @param equipProgress The progress of equiping the item
     * @param swingProgress The swing movement progression
     */
    private void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress) {
        float f = -0.3F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        float f1 = 0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI * 2.0F);
        float f2 = -0.4F * MathHelper.sin(swingProgress * (float) Math.PI);
        GlStateManager.translate(f, f1, f2);
        GlStateManager.translate(0.64000005F, -0.6F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f3 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f4 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f4 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * -20.0F, 0.0F, 0.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        GlStateManager.translate(-1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        GlStateManager.translate(5.6F, 0.0F, 0.0F);
        Render<AbstractClientPlayer> render = this.renderManager.getEntityRenderObject(this.mc.thePlayer);
        GlStateManager.disableCull();
        RenderPlayer renderplayer = (RenderPlayer) render;
        renderplayer.renderRightArm(this.mc.thePlayer);
        GlStateManager.enableCull();
    }

    private void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks) {
        float f = (float)clientPlayer.getItemInUseCount() - partialTicks + 1.0f;
        float f2 = f / (float)this.itemToRender.getMaxItemUseDuration();
        float f3 = MathHelper.abs(MathHelper.cos(f / 4.0f * (float)Math.PI) * 0.1f);
        if (f2 >= 0.8f) {
            f3 = 0.0f;
        }
        GL11.glTranslatef(0.0f, f3, 0.0f);
        float f4 = 1.0f - (float)Math.pow(f2, 27.0);
        GL11.glTranslatef(f4 * 0.6f, f4 * -0.5f, f4 * 0.0f);
        GL11.glRotatef(f4 * 90.0f, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(f4 * 10.0f, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(f4 * 30.0f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Performs transformations prior to the rendering of a held item in first person.
     *
     * @param equipProgress The progress of the animation to equip (raise from out of frame) while switching held items.
     * @param swingProgress The progress of the arm swing animation.
     */
    private void transformFirstPersonItem(float equipProgress, float swingProgress) {
        Animations animations = Client.INSTANCE.getModuleManager().getModule(Animations.class);

        double size = .40 + (animations.scaleValue.getValue() * .01);

        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(size, size, size);
    }

    private void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer) {
        GL11.glRotatef(-18.0f, 0.0f, 0.0f, 1.0f);
        GL11.glRotatef(-12.0f, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(-8.0f, 1.0f, 0.0f, 0.0f);
        GL11.glTranslatef(-0.9f, 0.2f, 0.0f);
        float f = (float)this.itemToRender.getMaxItemUseDuration() - ((float)clientPlayer.getItemInUseCount() - partialTicks + 1.0f);
        float f2 = f / 20.0f;
        f2 = (f2 * f2 + f2 * 2.0f) / 3.0f;
        if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        if (f2 > 0.1f) {
            float f3 = MathHelper.sin((f - 0.1f) * 1.3f);
            float f4 = f2 - 0.1f;
            float f5 = f3 * f4;
            GL11.glTranslatef(f5 * 0.0f, f5 * 0.01f, f5 * 0.0f);
        }
        GL11.glTranslatef(f2 * 0.0f, f2 * 0.0f, f2 * 0.1f);
        GL11.glScalef(1.0f, 1.0f, 1.0f + f2 * 0.2f);
    }

    private void doBlockTransformations() {
        GlStateManager.translate(-0.5f, 0.2f, 0.0f);
        GlStateManager.rotate(30.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-80.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(60.0f, 0.0f, 1.0f, 0.0f);
    }

    private void transformFirstPersonItemLunar(float swingProgress) {
        GlStateManager.translate(0.07F, -0.14F, -0.11F);
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    /**
     * Renders the active item in the player's hand when in first person mode. Args: partialTickTime
     *
     * @param partialTicks The amount of time passed during the current tick, ranging from 0 to 1.
     */
    public void renderItemInFirstPerson(float partialTicks) {
        if (!Config.isShaders() || !Shaders.isSkipRenderHand()) {
            KillAura aura = Client.INSTANCE.getModuleManager().getModule(KillAura.class);
            Animations animations = Client.INSTANCE.getModuleManager().getModule(Animations.class);

            float equipProgress = 1.0f - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
            EntityPlayerSP abstractclientplayer = this.mc.thePlayer;
            float swingProgress = abstractclientplayer.getSwingProgress(partialTicks);
            float pitch = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
            float yaw = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
            float var9 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * MathHelper.PI);

            this.rotateArroundXAndY(pitch, yaw);
            this.setLightMapFromPlayer(abstractclientplayer);
            this.rotateWithPlayerRotations(abstractclientplayer, partialTicks);
            GlStateManager.enableRescaleNormal();
            GL11.glPushMatrix();

            if (mc.thePlayer.getHeldItem() != null)
                GL11.glTranslated(animations.itemXValue.getValue(), animations.itemYValue.getValue(), animations.itemZValue.getValue());

            float var15 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
            if (this.itemToRender != null) {
                if (this.itemToRender.getItem() instanceof ItemMap) {
                    this.renderItemMap(abstractclientplayer, pitch, equipProgress, swingProgress);
                } else if (abstractclientplayer.getItemInUseCount() > 0 || (aura.isHoldingSword() &&(aura.forceDisplayBlocking.get() && aura.renderBlocking || !aura.forceDisplayBlocking.get() && aura.isBlocking))) {
                    EnumAction enumaction = this.itemToRender.getItemUseAction();
                    switch (enumaction) {
                        case NONE:
                            this.transformFirstPersonItem(equipProgress, swingProgress);
                            break;
                        case EAT:
                        case DRINK:
                            this.performDrinking(abstractclientplayer, partialTicks);
                            this.transformFirstPersonItem(equipProgress, swingProgress);
                            break;
                        case BLOCK:
                            if (mc.thePlayer.getHeldItem() != null)
                                GL11.glTranslated(animations.blockXValue.getValue(), animations.blockYValue.getValue(), animations.blockZValue.getValue());

                            float var151 = MathHelper.sin((float) (MathHelper.sqrt_float(swingProgress) * Math.PI));
                            switch (animations.type.getValue()) {
                                case "Lunar":
                                    this.transformFirstPersonItemLunar(swingProgress);
                                    this.doBlockTransformations();
                                    GlStateManager.translate(-0.5, 0.2, 0.0);
                                    break;
                                case "Jigsaw":
                                    this.transformFirstPersonItem(0.1f, swingProgress);
                                    this.doBlockTransformations();
                                    GlStateManager.translate(-0.5, 0, 0);
                                    break;
                                case "Debug":
                                    this.transformFirstPersonItem(0.2f, swingProgress);
                                    this.doBlockTransformations();
                                    GlStateManager.translate(-0.5, 0.2, 0.0);
                                    break;
                                case "Push":
                                    this.transformFirstPersonItem(equippedProgress, 0.0F);
                                    this.doBlockTransformations();
                                    GlStateManager.rotate(-MathHelper.sin((float) (MathHelper.sqrt_float(swingProgress) * Math.PI)) * 35.0F, -8.0F, -0.0F, 9.0F);
                                    GlStateManager.rotate(-MathHelper.sin((float) (MathHelper.sqrt_float(swingProgress) * Math.PI)) * 10.0F, 1.0F, -0.4F, -0.5F);
                                    break;
                                case "1.7":
                                    this.transformFirstPersonItem(equipProgress, swingProgress);
                                    this.doBlockTransformations();
                                    GlStateManager.scale(0.83F, 0.88F, 0.85F);
                                    GlStateManager.translate(-0.3F, 0.1F, 0.0F);
                                    break;
                                case "Swing":
                                    GL11.glTranslated(0.015f, 0.12f, 0.0);
                                    GL11.glTranslated(0.015f, (double) var15 * 0.055, 0.015f);
                                    this.transformFirstPersonItem(equipProgress, swingProgress);
                                    GlStateManager.rotate(var15 * 14.0f, -6.0f, -var15 / 20.0f, 0.0f);
                                    GlStateManager.rotate(var15 * 21.0f, 2.0f, -var15 / 4.0f, 0.0f);
                                    GL11.glTranslated(0.015f, (double) var15 * 0.05, 0.015f);
                                    this.doBlockTransformations();
                                    break;
                                case "Swong":
                                    GL11.glTranslated(-0.03, var151 * 0.062f, var151 * 0);
                                    GL11.glTranslated(0.025D, 0.09615D, 0.0D);
                                    transformFirstPersonItem(equipProgress / 3, 0.0F);
                                    GlStateManager.rotate(-var151 * 9f, -var151 / 20F, -var151 / 20F, 1);
                                    GlStateManager.rotate(-var151 * 55F, 1.2F, var151 / 4F, 0.36F);
                                    if (mc.thePlayer.isSneaking()) {
                                        GlStateManager.translate(-0.05, -0.05, 0);
                                    }
                                    this.doBlockTransformations();
                                    break;
                                case "Swonk":
                                    float f7 = MathHelper.sin((float) ((double) MathHelper.sqrt_float(swingProgress) * 3.1));
                                    GL11.glTranslated(0.0, 0.12, 0.0);
                                    GL11.glTranslated(0.0, (double) f7 * 0.05, 0.0);
                                    this.transformFirstPersonItem(equipProgress / 2.0f, 0.0f);
                                    GlStateManager.rotate(-f7 * 51.0f / 10.0f, -1.0f, 0.0f, 0.0f);
                                    GlStateManager.rotate(-f7 * 51.0f / 5.0f, f7 / 5.0f, -2.0f, 0.0f);
                                    GlStateManager.rotate(-f7 * 51.0f / 3.0f, 1.0f, f7 / 2.0f, -1.0f);
                                    GlStateManager.rotate(-f7 * 51.0f / 30.0f, 0.0f, -1.0f, 0.0f);
                                    this.doBlockTransformations();
                                    if (!this.mc.thePlayer.isSneaking()) break;
                                    GlStateManager.translate(0.0, 0.15, 0.0);
                                    break;
                                case "Swung":
                                    float smooth = (swingProgress * 0.8f - (swingProgress * swingProgress) * 0.8f);
                                    GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
                                    GlStateManager.translate(0.0F, equipProgress * -0.15F, 0.0F);
                                    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                                    GlStateManager.rotate(smooth * -90.0F, 0.0F, 1.0F, 0.0F);
                                    GlStateManager.scale(0.37F, 0.37F, 0.37F);
                                    this.doBlockTransformations();
                                    break;
                                case "Swang":
                                    float var16 = MathHelper.sin((float) ((double) MathHelper.sqrt_float(swingProgress) * Math.PI));
                                    GL11.glTranslated(0.015f, 0.12f, 0.0);
                                    GL11.glTranslated(0.015f, (double) var16 * 0.0666, 0.015f);
                                    this.transformFirstPersonItem(equipProgress / 2.0f, swingProgress);
                                    GlStateManager.rotate(var16 * 14.0f, 2.0f, -var16 / 10.0f, 6.0f);
                                    GlStateManager.rotate(var16 * 35.0f, 0.98f, -var16 / 1.5f, 0.0f);
                                    this.doBlockTransformations();
                                    GL11.glTranslated(0.015f, (double) var16 * 0.05, 0.015f);
                                    break;
                                case "Swank":
                                    GL11.glTranslated(-0.1f, 0.15f, 0.0);
                                    this.transformFirstPersonItem(equipProgress / 2.0f, swingProgress);
                                    float var14 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                                    GlStateManager.rotate(var14 * 30.0f, -var14, -0.0f, 9.0f);
                                    GlStateManager.rotate(var14 * 40.0f, 1.0f, -var14, -0.0f);
                                    this.doBlockTransformations();
                                    break;
                                case "Swack":
                                    float sw0ng = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927f);
                                    transformFirstPersonItem(equipProgress, 0.0f);
                                    GL11.glScaled(0.7d, 0.7d, 0.7d);
                                    GL11.glTranslated(-0.9d, 0.7d, 0.1d);
                                    GL11.glRotatef((-sw0ng) * 29.0f, 7.0f, -0.6f, -0.0f);
                                    GL11.glRotatef(((-sw0ng) * 10.0f) / 2.0f, -12.0f, 0.0f, 9.0f);
                                    GL11.glRotatef((-sw0ng) * 17.0f, -1.0f, -0.6f, -0.7f);
                                    GL11.glTranslatef(sw0ng / 2.5f, 0.1f, sw0ng / 2.5f);
                                    doBlockTransformations();
                                    break;
                                case "Leaked":
                                    float swingscale = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI) * 0.05f;
                                    this.transformFirstPersonItem(equipProgress, 0.0f);
                                    this.doBlockTransformations();
                                    GlStateManager.scale(1.0f + swingscale, 1.0f + swingscale, 1.0f + swingscale);
                                    GlStateManager.rotate(-MathHelper.sin((float) ((double) MathHelper.sqrt_float(swingProgress) * Math.PI)) * 20.0f, 0.0f, 1.2f, -0.8f);
                                    GlStateManager.rotate(-MathHelper.sin((float) ((double) MathHelper.sqrt_float(swingProgress) * Math.PI)) * 30.0f, 1.0f, 0.0f, 0.0f);
                                    break;
                                case "Jello":
                                    GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
                                    GlStateManager.translate(0.0F, 0 * -0.6F, 0.0F);
                                    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                                    float var3 = MathHelper.sin((float) (0.0F * 0.0F * Math.PI));
                                    float var4 = MathHelper.sin((float) (MathHelper.sqrt_float(0.0F) * Math.PI));
                                    GlStateManager.rotate(var3 * -20.0F, 0.0F, 1.0F, 0.0F);
                                    GlStateManager.rotate(var4 * -20.0F, 0.0F, 0.0F, 1.0F);
                                    GlStateManager.rotate(var4 * -80.0F, 1.0F, 0.0F, 0.0F);
                                    GlStateManager.scale(0.4F, 0.4F, 0.4F);

                                    GlStateManager.translate(-0.5F, 0.2F, 0.0F);
                                    GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
                                    GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
                                    GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
                                    int alpha = (int) Math.min(255,
                                            ((System.currentTimeMillis() % 255) > 255 / 2
                                                    ? (Math.abs(Math.abs(System.currentTimeMillis()) % 255 - 255))
                                                    : System.currentTimeMillis() % 255) * 2);
                                    GlStateManager.translate(0.3f, -0.0f, 0.40f);
                                    GlStateManager.rotate(0.0f, 0.0f, 0.0f, 1.0f);
                                    GlStateManager.translate(0, 0.5f, 0);

                                    GlStateManager.rotate(90, 1.0f, 0.0f, -1.0f);
                                    GlStateManager.translate(0.6f, 0.5f, 0);
                                    GlStateManager.rotate(-90, 1.0f, 0.0f, -1.0f);

                                    GlStateManager.rotate(-10, 1.0f, 0.0f, -1.0f);
                                    GlStateManager.rotate(mc.thePlayer.isSwingInProgress ? -alpha / 5f : 1, 1.0f, -0.0f, 1.0f);
                                    break;
                                case "Smooth":
                                    transformFirstPersonItem(equipProgress / 1.5F, 0.0f);
                                    doBlockTransformations();
                                    GlStateManager.translate(-0.05f, 0.3f, 0.3f);
                                    GlStateManager.rotate(-var9 * 140.0f, 8.0f, 0.0f, 8.0f);
                                    GlStateManager.rotate(var9 * 90.0f, 8.0f, 0.0f, 8.0f);
                                    break;
                                case "Ethereal":
                                    transformFirstPersonItem(equipProgress, 0.0f);
                                    doBlockTransformations();
                                    GlStateManager.translate(-0.05f, 0.2f, 0.2f);
                                    GlStateManager.rotate(-var9 * 70.0f / 2.0f, -8.0f, -0.0f, 9.0f);
                                    GlStateManager.rotate(-var9 * 70.0f, 1.0f, -0.4f, -0.0f);
                                    break;
                                case "E":
                                    float f8 = MathHelper.sin((float) ((double) MathHelper.sqrt_float(swingProgress) * Math.PI));
                                    GL11.glTranslated(0.05, f8 * 0.062f, f8 * 0.0f);
                                    GL11.glTranslated(0.025, 0.1115, 0.0);
                                    this.transformFirstPersonItem(equipProgress / 2, 0);
                                    GlStateManager.rotate(-f8 * -2.0f, -f8 / 20.0f, -f8 / -20.0f, 1.0f);
                                    GlStateManager.rotate(-f8 * 25.0f, 10.0f, f8 / 10.0f, 0.15f);
                                    if (this.mc.thePlayer.isSneaking()) {
                                        GlStateManager.translate(0.0, 0.15, 0.0);
                                    }
                                    this.doBlockTransformations();
                                    break;
                                case "1.8 (Loser)":
                                    GL11.glTranslated(0.0, 0.12, 0.0);
                                    this.transformFirstPersonItem(equipProgress, 0.0f);
                                    this.doBlockTransformations();
                                    break;
                            }
                            break;
                        case BOW:
                            this.transformFirstPersonItem(equipProgress, swingProgress);
                            this.doBowTransformations(partialTicks, abstractclientplayer);
                    }
                } else {
                    this.doItemUsedTransformations(swingProgress);
                    this.transformFirstPersonItem(equipProgress, swingProgress);
                }
                this.renderItem(abstractclientplayer, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
            } else if (!abstractclientplayer.isInvisible()) {
                this.renderPlayerArm(abstractclientplayer, equipProgress, swingProgress);
            }
            GL11.glPopMatrix();
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
        }
    }

    /**
     * Rotate and translate render to show item consumption
     *
     * @param swingProgress The swing movement progress
     */
    private void doItemUsedTransformations(float swingProgress) {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI * 2.0F);
        float f2 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
        GlStateManager.translate(f, f1, f2);
    }

    /**
     * Renders all the overlays that are in first person mode. Args: partialTickTime
     */
    public void renderOverlays(float partialTicks) {
        GlStateManager.disableAlpha();

        if (this.mc.thePlayer.isEntityInsideOpaqueBlock()) {
            IBlockState iblockstate = this.mc.theWorld.getBlockState(new BlockPos(this.mc.thePlayer));
            BlockPos blockpos = new BlockPos(this.mc.thePlayer);
            EntityPlayer entityplayer = this.mc.thePlayer;

            for (int i = 0; i < 8; ++i) {
                double d0 = entityplayer.posX + (double) (((float) ((i) % 2) - 0.5F) * entityplayer.width * 0.8F);
                double d1 = entityplayer.posY + (double) (((float) ((i >> 1) % 2) - 0.5F) * 0.1F);
                double d2 = entityplayer.posZ + (double) (((float) ((i >> 2) % 2) - 0.5F) * entityplayer.width * 0.8F);
                BlockPos blockpos1 = new BlockPos(d0, d1 + (double) entityplayer.getEyeHeight(), d2);
                IBlockState iblockstate1 = this.mc.theWorld.getBlockState(blockpos1);

                if (iblockstate1.getBlock().isVisuallyOpaque()) {
                    iblockstate = iblockstate1;
                    blockpos = blockpos1;
                }
            }

            if (iblockstate.getBlock().getRenderType() != -1) {
                Object object = Reflector.getFieldValue(Reflector.RenderBlockOverlayEvent_OverlayType_BLOCK);

                if (!Reflector.callBoolean(Reflector.ForgeEventFactory_renderBlockOverlay, this.mc.thePlayer, partialTicks, object, iblockstate, blockpos)) {
                    this.renderBlockInHand(this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(iblockstate));
                }
            }
        }

        if (!this.mc.thePlayer.isSpectator()) {
            if (this.mc.thePlayer.isInsideOfMaterial(Material.water) && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderWaterOverlay, this.mc.thePlayer, partialTicks)) {
                this.renderWaterOverlayTexture(partialTicks);
            }

            if (this.mc.thePlayer.isBurning() && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderFireOverlay, this.mc.thePlayer, partialTicks)) {
                this.renderFireInFirstPerson();
            }
        }

        GlStateManager.enableAlpha();
    }

    /**
     * Render the block in the player's hand
     *
     * @param atlas        The TextureAtlasSprite to render
     */
    private void renderBlockInHand(TextureAtlasSprite atlas) {
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.color(0.1F, 0.1F, 0.1F, 0.5F);
        GlStateManager.pushMatrix();

        float f6 = atlas.getMinU();
        float f7 = atlas.getMaxU();
        float f8 = atlas.getMinV();
        float f9 = atlas.getMaxV();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex(f7, f9).endVertex();
        worldrenderer.pos(1.0D, -1.0D, -0.5D).tex(f6, f9).endVertex();
        worldrenderer.pos(1.0D, 1.0D, -0.5D).tex(f6, f8).endVertex();
        worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex(f7, f8).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Renders a texture that warps around based on the direction the player is looking. Texture needs to be bound
     * before being called. Used for the water overlay. Args: parialTickTime
     */
    private void renderWaterOverlayTexture(float p_78448_1_) {
        if (!Config.isShaders() || Shaders.isUnderwaterOverlay()) {
            this.mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            float f = this.mc.thePlayer.getBrightness(p_78448_1_);
            GlStateManager.color(f, f, f, 0.5F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            float f7 = -this.mc.thePlayer.rotationYaw / 64.0F;
            float f8 = this.mc.thePlayer.rotationPitch / 64.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex(4.0F + f7, 4.0F + f8).endVertex();
            worldrenderer.pos(1.0D, -1.0D, -0.5D).tex(0.0F + f7, 4.0F + f8).endVertex();
            worldrenderer.pos(1.0D, 1.0D, -0.5D).tex(0.0F + f7, 0.0F + f8).endVertex();
            worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex(4.0F + f7, 0.0F + f8).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
        }
    }

    /**
     * Renders the fire on the screen for first person mode. Arg: partialTickTime
     */
    private void renderFireInFirstPerson() {
        Camera camera = Client.INSTANCE.getModuleManager().getModule(Camera.class);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
        GlStateManager.depthFunc(519);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        float f = 1.0F;

        for (int i = 0; i < 2; ++i) {
            GlStateManager.pushMatrix();
            TextureAtlasSprite textureatlassprite = this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
            this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            float f1 = textureatlassprite.getMinU();
            float f2 = textureatlassprite.getMaxU();
            float f3 = textureatlassprite.getMinV();
            float f4 = textureatlassprite.getMaxV();
            float f5 = (0.0F - f) / 2.0F; // X
            float f6 = f5 + f;
            float f7 = camera.isEnabled() && camera.noFire.get() ? camera.noFireValue.getValue() : (float) (0.0 - f / 2.0F); // Y
            float f8 = f7 + f;
            float f9 = -0.5F;
            GlStateManager.translate((float) (-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            GlStateManager.rotate((float) (i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.setSprite(textureatlassprite);
            worldrenderer.pos(f5, f7, f9).tex(f2, f4).endVertex();
            worldrenderer.pos(f6, f7, f9).tex(f1, f4).endVertex();
            worldrenderer.pos(f6, f8, f9).tex(f1, f3).endVertex();
            worldrenderer.pos(f5, f8, f9).tex(f2, f3).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
    }

    public void updateEquippedItem() {
        this.prevEquippedProgress = this.equippedProgress;
        ItemStack itemstack = SpoofSlotComponent.getSpoofedStack();
        boolean flag = false;

        if (this.itemToRender != null && itemstack != null) {
            if (!this.itemToRender.getIsItemStackEqual(itemstack)) {
                if (Reflector.ForgeItem_shouldCauseReequipAnimation.exists()) {
                    boolean flag1 = Reflector.callBoolean(this.itemToRender.getItem(),
                            Reflector.ForgeItem_shouldCauseReequipAnimation, this.itemToRender, itemstack, this.equippedItemSlot != SpoofSlotComponent.getSpoofedSlot());

                    if (!flag1) {
                        this.itemToRender = itemstack;
                        this.equippedItemSlot = SpoofSlotComponent.getSpoofedSlot();
                        return;
                    }
                }

                flag = true;
            }
        } else flag = this.itemToRender != null || itemstack != null;

        float f2 = 0.4F;
        float f = flag ? 0.0F : 1.0F;
        float f1 = MathHelper.clamp_float(f - this.equippedProgress, -f2, f2);
        this.equippedProgress += f1;

        if (this.equippedProgress < 0.1F) {
            this.itemToRender = itemstack;
            this.equippedItemSlot = SpoofSlotComponent.getSpoofedSlot();

            if (Config.isShaders()) {
                Shaders.setItemToRenderMain(itemstack);
            }
        }
    }

    /**
     * Resets equippedProgress
     */
    public void resetEquippedProgress() {
        this.equippedProgress = 0.0F;
    }

    /**
     * Resets equippedProgress
     */
    public void resetEquippedProgress2() {
        this.equippedProgress = 0.0F;
    }
}