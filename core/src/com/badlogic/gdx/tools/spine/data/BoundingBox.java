package com.badlogic.gdx.tools.spine.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoundingBox implements Attachment {

  private String type = "boundingbox";

  private int vertexCount;

  private float[] vertices;
}
