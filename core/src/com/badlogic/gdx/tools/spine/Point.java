package com.badlogic.gdx.tools.spine;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Point {

  private int x;

  private int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Point(Point point) {
    set(point);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Point point = (Point) o;
    return x == point.x &&
        y == point.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  public void set(Point point) {
    x = point.x;
    y = point.y;
  }

  public void translate(int deltaX, int deltaY) {
    x += deltaX;
    y += deltaY;
  }

  public boolean same(int x, int y) {
    return this.x == x && this.y == y;
  }
}
