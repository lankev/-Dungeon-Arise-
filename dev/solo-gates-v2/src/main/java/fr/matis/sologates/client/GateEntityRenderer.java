package fr.matis.sologates.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fr.matis.sologates.SoloGates;
import fr.matis.sologates.entity.GateEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class GateEntityRenderer<T extends GateEntity> extends EntityRenderer<T> {

    private static final ResourceLocation VORTEX_TEXTURE =
        new ResourceLocation(SoloGates.MOD_ID, "textures/block/gate_vortex.png");
    private static final RenderType RENDER_TYPE =
        RenderType.entityTranslucent(VORTEX_TEXTURE);

    // [scale, rotation_speed_deg_per_tick, alpha]
    private static final float[][] LAYERS = {
        {1.00f,  0.40f, 0.80f},
        {0.80f, -0.70f, 0.90f},
        {0.55f,  1.20f, 1.00f},
    };

    public GateEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return VORTEX_TEXTURE;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        float[] c = entity.getRank().renderColor();
        long gameTime = entity.level().getGameTime();
        float time = gameTime + partialTick;

        poseStack.pushPose();
        poseStack.translate(0, 1.5, 0);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation());
        poseStack.scale(3.0f, 3.0f, 3.0f);

        VertexConsumer vc = bufferSource.getBuffer(RENDER_TYPE);
        for (float[] layer : LAYERS) {
            poseStack.pushPose();
            poseStack.scale(layer[0], layer[0], layer[0]);
            poseStack.mulPose(Axis.ZP.rotationDegrees(time * layer[1]));
            drawQuad(poseStack, vc, c[0], c[1], c[2], layer[2]);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void drawQuad(PoseStack ps, VertexConsumer vc,
                                 float r, float g, float b, float a) {
        Matrix4f mat = ps.last().pose();
        Matrix3f nor = ps.last().normal();
        int ri = (int)(r*255), gi = (int)(g*255), bi = (int)(b*255), ai = (int)(a*255);
        int light = LightTexture.FULL_BRIGHT;
        vc.vertex(mat,-0.5f,-0.5f,0).color(ri,gi,bi,ai).uv(0,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nor,0,0,1).endVertex();
        vc.vertex(mat, 0.5f,-0.5f,0).color(ri,gi,bi,ai).uv(1,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nor,0,0,1).endVertex();
        vc.vertex(mat, 0.5f, 0.5f,0).color(ri,gi,bi,ai).uv(1,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nor,0,0,1).endVertex();
        vc.vertex(mat,-0.5f, 0.5f,0).color(ri,gi,bi,ai).uv(0,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nor,0,0,1).endVertex();
    }
}
