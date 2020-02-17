package com.badlogic.gdx.tools.spine;

import lombok.Getter;
import lombok.Setter;

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

  private float duration = 0.125f;

  public void set(SkeletonSettings settings) {
    this.slotName = settings.slotName;
    this.x = settings.x;
    this.y = settings.y;
    this.width = settings.width;
    this.height = settings.height;
    this.anchorX = settings.anchorX;
    this.anchorY = settings.anchorY;
    this.duration = settings.duration;
  }
}
