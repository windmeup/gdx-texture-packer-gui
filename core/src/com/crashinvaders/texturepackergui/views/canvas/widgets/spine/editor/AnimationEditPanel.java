package com.crashinvaders.texturepackergui.views.canvas.widgets.spine.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.crashinvaders.texturepackergui.utils.GraphicsUtils;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.AnimationActor;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class AnimationEditPanel extends Group {

  private static final Color BOUNDS_COLOR = Color.RED;

  private static final float GAP = 20f;

  private static final float GAP_DOUBLE = GAP * 2f;

  private ShapeDrawer shapeDrawer;

  private final AnimationActor actor;

  private final Polygon bounds;

  AnimationEditPanel(AnimationActor actor, Polygon bounds) {
    Skeleton skeleton = new Skeleton(actor.getSkeleton());
    SkeletonData data = skeleton.getData();
    Rectangle bound = actor.getBound();
    this.actor = new AnimationActor(
        actor.getRenderer(), skeleton, new AnimationState(new AnimationStateData(data)),
        bound, actor.getBorder());
    this.actor.getAnimationState().setAnimation(0, actor.getName(), true); // actor name is the animation name
    this.actor.setPosition(GAP - bound.x, GAP - bound.y);
    addActor(this.actor);
    float[] verticesCopy;
    if (bounds == null) {
      verticesCopy = new float[8]; // copy actor's bound counter clockwise
      verticesCopy[0] = bound.x;
      verticesCopy[1] = bound.y;
      verticesCopy[2] = bound.x;
      verticesCopy[3] = bound.y + bound.height;
      verticesCopy[4] = bound.x + bound.width;
      verticesCopy[5] = verticesCopy[3];
      verticesCopy[6] = verticesCopy[4];
      verticesCopy[7] = bound.y;
    } else {
      float[] vertices = bounds.getVertices();
      verticesCopy = new float[vertices.length];
      System.arraycopy(vertices, 0, verticesCopy, 0, vertices.length);
    }
    this.bounds = new Polygon(verticesCopy);
    this.bounds.setPosition(GAP - bound.x, GAP - bound.y);
    setSize(bound.width + GAP_DOUBLE, bound.height + GAP_DOUBLE);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    if (shapeDrawer == null) {
      shapeDrawer = new ShapeDrawer(batch, GraphicsUtils.onePix);
    }
    shapeDrawer.setColor(Color.WHITE);
    shapeDrawer.filledRectangle(0f, 0f, getWidth(), getHeight());
    applyTransform(batch, computeTransform());
    actor.draw(batch, parentAlpha); // culling is not correct, just draw actor
    shapeDrawer.setColor(BOUNDS_COLOR);
    shapeDrawer.polygon(bounds);
    resetTransform(batch);
  }

  @Override
  public void setScale(float scaleXY) {
    super.setScale(scaleXY);
    Rectangle bound = actor.getBound();
    setSize((bound.width + GAP_DOUBLE) * scaleXY,
        (bound.height + GAP_DOUBLE) * scaleXY);
  }

  public void zoomIn() {
    float scaleXY = getScaleX();
    if (scaleXY < 10f) {
      scaleXY = Math.min(10f, scaleXY + 1f);
      setScale(scaleXY);
    }
  }

  public void zoomOut() {
    float scaleXY = getScaleX();
    if (scaleXY > 1f) {
      scaleXY = Math.max(1f, scaleXY - 1f);
      setScale(scaleXY);
    }
  }
}
