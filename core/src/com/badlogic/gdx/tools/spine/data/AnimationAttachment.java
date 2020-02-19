package com.badlogic.gdx.tools.spine.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimationAttachment {

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private float time;

  private String name;
}
