package com.badlogic.gdx.tools.spine.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Region implements Attachment {

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private int x;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private int y;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private int width;

  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private int height;
}
