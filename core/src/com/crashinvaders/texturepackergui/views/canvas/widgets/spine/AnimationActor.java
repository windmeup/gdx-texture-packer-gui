package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Rectangle;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.utils.SkeletonActor;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class AnimationActor extends SkeletonActor {

  private static final float PAD = 2f;

  private static final Color AXES = new Color(0x00ff0066);

  private static final Color AABB = new Color(0xffff0066);

  private static final Rectangle tempAABB = new Rectangle();

  private final Rectangle bound;

  private final NinePatch border;

  AnimationActor(
      SkeletonRenderer renderer, Skeleton skeleton, AnimationState state,
      Rectangle bound, NinePatch border) {
    super(renderer, skeleton, state);
    this.bound = bound;
    this.border = border;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    batch.setColor(Color.BLACK);
    border.draw(batch, getX() - PAD + bound.getX(), getY() - PAD + bound.getY(),
        bound.getWidth() + PAD * 2f, bound.getHeight() + PAD * 2f);
  }

  public void drawCoords(
      ShapeDrawer shapeDrawer, Rectangle aabb,
      float parentX, float parentY, float scaleX, float scaleY) {
    float x = parentX + getX() * scaleX;
    float y = parentY + getY() * scaleY;
    float left = x + bound.getX() * scaleX;
    float bottom = y + bound.getY() * scaleY;
    shapeDrawer.setColor(AXES);
    shapeDrawer.line(left, y, left + bound.getWidth() * scaleX, y);
    shapeDrawer.line(x, bottom, x, bottom + bound.getHeight() * scaleY);
    shapeDrawer.setColor(AABB);
    intersect(aabb);
    shapeDrawer.filledRectangle(
        x + tempAABB.x * scaleX, y + tempAABB.y * scaleY, tempAABB.width * scaleX, tempAABB.height * scaleY);
  }

  private void intersect(Rectangle aabb) {
    float left = Math.max(aabb.x, bound.x);
    float bottom = Math.max(aabb.y, bound.y);
    float right = Math.min(aabb.x + aabb.width, bound.x + bound.width);
    float top = Math.min(aabb.y + aabb.height, bound.y + bound.height);
    tempAABB.set(left, bottom, right - left, top - bottom);
  }
}
