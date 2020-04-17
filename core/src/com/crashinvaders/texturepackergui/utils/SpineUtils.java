package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.tools.spine.data.Region;
import com.esotericsoftware.spine.attachments.RegionAttachment;

public class SpineUtils {

  public static Rectangle getBound(Region region) {
    return getBound(region.getX(), region.getY(), region.getWidth(), region.getHeight());
  }

  public static Rectangle getBound(RegionAttachment region) {
    return getBound(region.getX(), region.getY(), region.getWidth(), region.getHeight());
  }

  private static Rectangle getBound(float x, float y, float width, float height) {
    return new Rectangle(x - width / 2f, y - height / 2f, width, height);
  }
}
