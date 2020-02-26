package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Rectangle;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.utils.SkeletonActor;
import lombok.Getter;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class AnimationActor extends SkeletonActor {

  private static final float PAD = 2f;

  private static final Color AXES = new Color(0x00ff0066);

  private static final Color AABB = new Color(0xffff0066);

  @Getter
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
    float left = x + (bound.getX() - PAD) * scaleX;
    float bottom = y + (bound.getY() - PAD) * scaleY;
    shapeDrawer.setColor(AXES);
    shapeDrawer.line(left, y, left + (bound.getWidth() + PAD * 2f) * scaleX, y);
    shapeDrawer.line(x, bottom, x, bottom + (bound.getHeight() + PAD * 2f) * scaleY);
    shapeDrawer.setColor(AABB);
    shapeDrawer.filledRectangle(
        x + aabb.x * scaleX, y + aabb.y * scaleY, aabb.width * scaleX, aabb.height * scaleY);
  }
}
