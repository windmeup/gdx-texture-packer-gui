package com.crashinvaders.texturepackergui.views.canvas.widgets.spine.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
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

  private static final float GAP = 20f;

  private static final float GAP_DOUBLE = GAP * 2f;

  private ShapeDrawer shapeDrawer;

  private final AnimationActor actor;

  AnimationEditPanel(AnimationActor actor) {
    Skeleton skeleton = new Skeleton(actor.getSkeleton());
    SkeletonData data = skeleton.getData();
    Rectangle bound = actor.getBound();
    this.actor = new AnimationActor(
        actor.getRenderer(), skeleton, new AnimationState(new AnimationStateData(data)),
        bound, actor.getBorder());
    this.actor.getAnimationState().setAnimation(0, actor.getName(), true); // actor name is the animation name
    this.actor.setPosition(GAP - bound.x, GAP - bound.y);
    addActor(this.actor);
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
