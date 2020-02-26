package com.badlogic.gdx.tools.spine.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * "hash": "MOhkPvw8nLOrjuLm/U98aFMannI",
 * "spine": "3.8.55",
 * "x": -221.27,
 * "y": -8.57,
 * "width": 470.72,
 * "height": 731.57,
 * "images": "./images/",
 * "audio": ""
 */
@Getter
public class Skeleton {

  @Setter
  private String spine;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private float x;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private float y;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private float width;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private float height;

  @Setter
  private String images;

  public boolean setX(float x) {
    if (this.x != x) {
      this.x = x;
      return true;
    }
    return false;
  }

  public boolean setY(float y) {
    if (this.y != y) {
      this.y = y;
      return true;
    }
    return false;
  }

  public boolean setWidth(float width) {
    if (this.width != width) {
      this.width = width;
      return true;
    }
    return false;
  }

  public boolean setHeight(float height) {
    if (this.height != height) {
      this.height = height;
      return true;
    }
    return false;
  }
}
