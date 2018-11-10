package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogicgames.libimagequant.LiqAttribute;
import com.badlogicgames.libimagequant.LiqImage;
import com.badlogicgames.libimagequant.LiqPalette;
import com.badlogicgames.libimagequant.LiqResult;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

public class ImageQuantProcessor implements PackProcessor {
    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

//        if (project.getFileType().getClass() != PngFileTypeModel.class) return;

//        PngFileTypeModel fileType = (PngFileTypeModel) project.getFileType();

//        if (fileType.getCompression() == null || fileType.getCompression().getType() != PngCompressionType.PNG8) return;

        System.out.println("Pngquant compression started");

        new SharedLibraryLoader().load("imagequant-java");

        TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
                Gdx.files.absolute(pack.getOutputDir()).child(pack.getCanonicalFilename()),
                Gdx.files.absolute(pack.getOutputDir()), false);
        for (TextureAtlas.TextureAtlasData.Page page : atlasData.getPages()) {
            Pixmap pm = null;
            try {
                long preCompressedSize = page.textureFile.length();
                compressImage(page.textureFile);
                pm = new Pixmap(page.textureFile);
            } finally {
                if (pm != null) pm.dispose();
            }
        }

        System.out.println("Pngquant compression finished");
    }

    private static void compressImage(FileHandle fileHandle) throws IOException {
        // Read the input image.
        BufferedImage input = ImageIO.read(fileHandle.read());
        byte[] pixels = ((DataBufferByte)input.getRaster().getDataBuffer()).getData();

        // ABGR -> RGBA.
        for (int i = 0; i < pixels.length; i += 4) {
            byte a = pixels[i];
            byte b = pixels[i + 1];
            byte g = pixels[i + 2];
            byte r = pixels[i + 3];
            pixels[i] = r;
            pixels[i + 1] = g;
            pixels[i + 2] = b;
            pixels[i + 3] = a;
        }

        // Setup libimagequant and quantize the image.
        LiqAttribute attribute = new LiqAttribute();
        LiqImage image = new LiqImage(attribute, pixels, input.getWidth(), input.getHeight(), 0);
        LiqResult result = image.quantize();

        // Based on the quantization result, generate an 8-bit indexed image and retrieve its palette.
        byte[] quantizedPixels = new byte[input.getWidth() * input.getHeight()];
        image.remap(result, quantizedPixels);
        LiqPalette palette = result.getPalette();

        // The resulting 8-bit indexed image and palette could be written out to an indexed PNG or GIF, but instead we convert it
        // back to 32-bit RGBA.
        BufferedImage convertedImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        byte[] convertedPixels = ((DataBufferByte)convertedImage.getRaster().getDataBuffer()).getData();
        int size = input.getWidth() * input.getHeight();
        for (int i = 0, j = 0; i < size; i++, j += 4) {
            int index = quantizedPixels[i] & 0xff; // Java's byte is signed
            int color = palette.getColor(index);
            convertedPixels[j] = LiqPalette.getA(color);
            convertedPixels[j + 1] = LiqPalette.getB(color);
            convertedPixels[j + 2] = LiqPalette.getG(color);
            convertedPixels[j + 3] = LiqPalette.getR(color);
        }

        ImageIO.write(convertedImage, "png", fileHandle.write(false));
    }
}
