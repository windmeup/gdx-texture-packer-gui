package com.crashinvaders.texturepackergui.controllers.packing.processors.spine;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public abstract class SpineProcessor implements PackProcessor {

  protected TexturePacker.Settings toSpineSettings(TexturePacker.Settings settings) {
    TexturePacker.Settings spineSettings = new TexturePacker.Settings(settings);
    spineSettings.stripWhitespaceX = true;
    spineSettings.stripWhitespaceY = true;
    spineSettings.shrinkSize = true;
    spineSettings.evenSize = true;
    spineSettings.edgePadding = true;
    spineSettings.useIndexes = false;
    spineSettings.scale = new float[]{1};
    spineSettings.paddingX = Math.max(settings.paddingX, 1);
    spineSettings.paddingY = Math.max(settings.paddingY, 1);
    return spineSettings;
  }

  @Getter
  @Setter
  protected static class Rect {

    private int originalHeight;

    private int regionHeight;

    private int offsetY;

    private int regionWidth;

    private int offsetX;
  }

  @Getter
  @Setter
  protected static class Rects {

    Map<String, Rect> rects;
  }
}
