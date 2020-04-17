package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.math.Rectangle;

public class GeoUtils {

  public static float[] copyVertices(Rectangle rectangle) {
    float[] vertices = new float[8]; // clockwise vertices
    vertices[0] = rectangle.x;
    vertices[1] = rectangle.y;
    vertices[2] = rectangle.x;
    vertices[3] = rectangle.y + rectangle.height;
    vertices[4] = rectangle.x + rectangle.width;
    vertices[5] = vertices[3];
    vertices[6] = vertices[4];
    vertices[7] = rectangle.y;
    return vertices;
  }

  public static float[] copyVertices(float[] vertices) {
    float[] copyVertices = new float[vertices.length];
    System.arraycopy(vertices, 0, copyVertices, 0, vertices.length);
    return copyVertices;
  }
}
