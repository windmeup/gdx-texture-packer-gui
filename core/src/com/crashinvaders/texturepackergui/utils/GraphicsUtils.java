package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GraphicsUtils {

  public static TextureRegion onePix = onePixTextureRegion();

  public static void dispose() {
    onePix.getTexture().dispose();
  }

  private static TextureRegion onePixTextureRegion() {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.drawPixel(0, 0);
    Texture texture = new Texture(pixmap); //remember to dispose of later
    pixmap.dispose();
    return new TextureRegion(texture, 0, 0, 1, 1);
  }
}
