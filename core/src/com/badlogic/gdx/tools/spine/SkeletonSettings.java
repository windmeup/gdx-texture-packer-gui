package com.badlogic.gdx.tools.spine;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
public class SkeletonSettings {

  private String slotName = "";

  private int x;

  private int y;

  private int width;

  private int height;

  private int anchorX;

  private int anchorY;

  private String anchorFilesDir = "";

  private float duration = 0.125f;

  private Map<String, Point> animationOffsets;

  public void set(SkeletonSettings settings) {
    slotName = settings.slotName;
    x = settings.x;
    y = settings.y;
    width = settings.width;
    height = settings.height;
    anchorX = settings.anchorX;
    anchorY = settings.anchorY;
    duration = settings.duration;
    anchorFilesDir = settings.anchorFilesDir;
    animationOffsets = new TreeMap<>();
    for (Map.Entry<String, Point> entry : settings.animationOffsets.entrySet()) {
      animationOffsets.put(entry.getKey(), new Point(entry.getValue()));
    }
  }
}
