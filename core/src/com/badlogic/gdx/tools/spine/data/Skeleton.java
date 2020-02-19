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
@Setter
public class Skeleton {

  private String spine;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private float x;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private float y;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private float width;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private float height;

  private String images;
}
