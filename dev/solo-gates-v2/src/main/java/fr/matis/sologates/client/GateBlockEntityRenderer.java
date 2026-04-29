package fr.matis.sologates.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import fr.matis.sologates.GateRank;
import fr.matis.sologates.SoloGates;
import fr.matis.sologates.block.GateBlock;
import fr.matis.sologates.block.GateBlockEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class GateBlockEntityRenderer implements BlockEntityRenderer<GateBlockEntity> {

    private static final ResourceLocation VORTEX_TEXTURE =
        new ResourceLocation(SoloGates.MOD_ID, "textures/block/gate_vortex.png");
    private static final RenderType RENDER_TYPE =
        RenderType.entityTranslucentCull(VORTEX_TEXTURE);

    // Portal display size: 3x3 blocks
    private static final float SIZE = 3.0f;

    // 3 layers: [scale, rotation_speed_deg_per_tick, alpha]
    private static final float[][] LAYERS = {
        {1.10f,  0.50f, 0.55f},
        {0.85f, -0.80f, 0.80f},
        {0.55f,  1.30f, 0.95f},
    };

    public GateBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(GateBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;

        GateRank rank = ((GateBlock) be.getBlockState().getBlock()).getRank();
        float[] c = rank.renderColor();

        long gameTime = be.getLevel().getGameTime();
        float time = gameTime + partialTick;

        poseStack.pushPose();
        // Center on block
        poseStack.translate(0.5, 0.5, 0.5);
        // Billboard: always face camera
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation());
        // Scale to 3x3 blocks
        poseStack.scale(SIZE, SIZE, SIZE);

        VertexConsumer vc = buffers.getBuffer(RENDER_TYPE);
        for (float[] layer : LAYERS) {
            float scale = layer[0];
            float angle = time * layer[1];
            float alpha = layer[2];

            poseStack.pushPose();
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
            drawQuad(poseStack, vc, c[0], c[1], c[2], alpha);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void drawQuad(PoseStack ps, VertexConsumer vc,
                                 float r, float g, float b, float a) {
        Matrix4f mat = ps.last().pose();
        Matrix3f nor = ps.last().normal();
        int ri = (int)(r * 255), gi = (int)(g * 255), bi = (int)(b * 255), ai = (int)(a * 255);
        int light = LightTexture.FULL_BRIGHT;

        vc.vertex(mat, -0.5f, -0.5f, 0).color(ri, gi, bi, ai).uv(0, 1)
          .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nor, 0, 0, 1).endVertex();
        vc.vertex(mat,  0.5f, -0.5f, 0).color(ri, gi, bi, ai).uv(1, 1)
          .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nor, 0, 0, 1).endVertex();
        vc.vertex(mat,  0.5f,  0.5f, 0).color(ri, gi, bi, ai).uv(1, 0)
          .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nor, 0, 0, 1).endVertex();
        vc.vertex(mat, -0.5f,  0.5f, 0).color(ri, gi, bi, ai).uv(0, 0)
          .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nor, 0, 0, 1).endVertex();
    }

    @Override
    public int getViewDistance() { return 256; }
}
