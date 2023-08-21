package bioast.mods.gt6m.scanner.item.gui;

import bioast.mods.gt6m.scanner.PacketScanner;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

public class MapTexture extends AbstractTexture {
    public final PacketScanner packet;
    private String selected = "All";
    public int width = -1;
    public int height = -1;
    public boolean invert = false;

    public MapTexture(PacketScanner packet) {
        this.packet = packet;
    }

    private BufferedImage getImage() {
        final int backgroundColor = invert ? Color.GRAY.getRGB() : Color.WHITE.getRGB();
        final int wh = (packet.size * 2 + 1) * 16;

        BufferedImage image = new BufferedImage(wh, wh, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = image.getRaster();

        int playerI = packet.posX - (packet.chunkX - packet.size) * 16 - 1; // Correct player offset
        int playerJ = packet.posZ - (packet.chunkZ - packet.size) * 16 - 1;
        for (int i = 0; i < wh; i++) {
            for (int j = 0; j < wh; j++) {
                image.setRGB(i, j, backgroundColor);
                if (packet.MAP_MAT[i][j] != null) {
                    for (short meta : packet.MAP_MAT[i][j].values()) {
                        final String name = packet.META_TO_NAME.get(meta);
                        if (!selected.equals("All") && !selected.equals(name)) continue;

                        image.setRGB(i, j, packet.ORES_TO_RGB.getOrDefault(name, Color.BLACK.getRGB()) | 0XFF000000);
                        break;
                    }
//                    } else if (packet.ptype == 3) {
//                        final short meta = packet.map[i][j].get((byte) 1);
//                        image.setRGB(i, j, ((meta & 0xFF) << 16) + ((meta & 0xFF) << 8) + ((meta & 0xFF)) | 0XFF000000);
//                    }
                }
                // draw player pos
                if (i == playerI || j == playerJ) {
                    raster.setSample(i, j, 0, (raster.getSample(i, j, 0) + 255) / 2);
                    raster.setSample(i, j, 1, raster.getSample(i, j, 1) / 2);
                    raster.setSample(i, j, 2, raster.getSample(i, j, 2) / 2);
                }
                // draw grid
                if ((i) % 16 == 0 || (j) % 16 == 0) {
                    raster.setSample(i, j, 0, raster.getSample(i, j, 0) / 2);
                    raster.setSample(i, j, 1, raster.getSample(i, j, 1) / 2);
                    raster.setSample(i, j, 2, raster.getSample(i, j, 2) / 2);
                }

            }
        }

        return image;
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) {
        this.deleteGlTexture();
        if (packet != null) {
            int tId = getGlTextureId();
            if (tId < 0) return;
            TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), getImage(), false, false);
            width = packet.getSize();
            height = packet.getSize();
        }
    }
    public void loadTexture(IResourceManager resourceManager, boolean invert) {
        this.invert = invert;
        loadTexture(resourceManager);
    }

    public void loadTexture(IResourceManager resourceManager, String selected, boolean invert) {
        this.selected = selected;
        loadTexture(resourceManager, invert);
    }

    public int glBindTexture() {
        if (this.glTextureId < 0) return this.glTextureId;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getGlTextureId());
        return this.glTextureId;
    }

    public void draw(int x, int y) {
        float f = 1F / (float) width;
        float f1 = 1F / (float) height;
        int u = 0, v = 0;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(
                (double) (x),
                (double) (y + height),
                0,
                (double) ((float) (u) * f),
                (double) ((float) (v + height) * f1));
        tessellator.addVertexWithUV(
                (double) (x + width),
                (double) (y + height),
                0,
                (double) ((float) (u + width) * f),
                (double) ((float) (v + height) * f1));
        tessellator.addVertexWithUV(
                (double) (x + width),
                (double) (y),
                0,
                (double) ((float) (u + width) * f),
                (double) ((float) (v) * f1));
        tessellator.addVertexWithUV(
                (double) (x),
                (double) (y),
                0,
                (double) ((float) (u) * f),
                (double) ((float) (v) * f1));
        tessellator.draw();
    }
}
