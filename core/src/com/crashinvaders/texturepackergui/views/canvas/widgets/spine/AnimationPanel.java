package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.crashinvaders.texturepackergui.utils.GraphicsUtils;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import lombok.Getter;
import lombok.Setter;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationPanel extends Group {

  public static final float GAP = 10f;

  public static final float INFO_PANEL_HEIGHT = 18f;

  private static final SkeletonRenderer skeletonRenderer = new SkeletonRenderer();

  private ShapeDrawer shapeDrawer;

  private final NinePatchDrawable borderFrame;

  private final NinePatch actorBorder;

  private final Rectangle aabb = new Rectangle();

  private SkeletonData skeletonData;

  @Getter
  @Setter
  private boolean showCoords;

  AnimationPanel(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
    setTouchable(Touchable.disabled);
    borderFrame = new NinePatchDrawable(skin.getPatch("custom/white_frame")).tint(Color.BLACK);
    actorBorder = skin.getPatch("custom/white_frame");
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    batch.setColor(Color.BLACK);
    float x = getX();
    float y = getY();
    float scaleX = getScaleX();
    float scaleY = getScaleY();
    borderFrame.draw(batch, x, y, getWidth() * scaleX, getHeight() * scaleY);
    super.draw(batch, parentAlpha);
    if (showCoords) {
      if (shapeDrawer == null) {
        shapeDrawer = new ShapeDrawer(batch, GraphicsUtils.onePix);
      }
      shapeDrawer.setDefaultLineWidth(2f);
      for (Actor actor : getChildren()) {
        if (actor instanceof AnimationActor) {
          ((AnimationActor) actor).drawCoords(shapeDrawer, aabb, x, y, scaleX, scaleY);
        }
      }
    }
  }

  @Override
  public void setScale(float scaleXY) {
    super.setScale(scaleXY);
    layout();
  }

  public void setSkeletonData(SkeletonData skeletonData) {
    this.skeletonData = skeletonData;
    layout();
  }

  public void layout() {
    clear();
    if (skeletonData == null) {
      return;
    }
    Skin skin = skeletonData.getDefaultSkin();
    if (skin == null) {
      return;
    }
    aabb.set(skeletonData.getX(), skeletonData.getY(), skeletonData.getWidth(), skeletonData.getHeight());
    Map<String, Rectangle> actorBounds = new HashMap<>();
    Attachment attachment;
    for (Skin.SkinEntry skinEntry : skin.getAttachments()) {
      attachment = skinEntry.getAttachment();
      if (attachment instanceof RegionAttachment) {
        actorBounds.put(attachment.getName(), getBound((RegionAttachment) attachment));
      }
    }
    Rectangle actorBound;
    float maxActorWidth = 0f;
    for (Animation animation : skeletonData.getAnimations()) {
      actorBound = getBound(animation, actorBounds);
      maxActorWidth = Math.max(maxActorWidth, actorBound.getWidth());
    }
    AnimationActor animationActor;
    float actorX = GAP;
    float actorWidth;
    float actorHeight;
    float rowHeight = GAP;
    float scaleX = getScaleX();
    Group parent = getParent();
    float parentWidth = parent.getWidth();
    float width = Math.max((parentWidth - GAP * 2f) / scaleX, maxActorWidth + GAP * 2f);
    float height = GAP;
    float rowIncrease;
    List<AnimationActor> actors = new ArrayList<>();
    for (Animation animation : skeletonData.getAnimations()) {
      actorBound = getBound(animation, actorBounds);
      actorHeight = actorBound.getHeight() + GAP;
      actorWidth = actorBound.getWidth();
      if (actorX + actorWidth + GAP > width) {
        actorX = GAP;
        for (AnimationActor actor : actors) {
          actor.setY(actor.getY() + actorHeight);
        }
        height += rowHeight;
        rowHeight = actorHeight;
      } else {
        rowIncrease = actorHeight - rowHeight;
        if (rowIncrease > 0f) {
          for (AnimationActor actor : actors) {
            actor.setY(actor.getY() + rowIncrease);
          }
          rowHeight = actorHeight;
        }
      }
      animationActor = new AnimationActor(skeletonRenderer, new Skeleton(skeletonData),
          new AnimationState(new AnimationStateData(skeletonData)), actorBound, actorBorder);
      animationActor.getAnimationState().setAnimation(0, animation.getName(), true);
      animationActor.setPosition(actorX - actorBound.getX(), GAP - actorBound.getY());
      actors.add(animationActor);
      actorX += actorBound.getWidth() + GAP;
    }
    height += rowHeight;
    for (AnimationActor actor : actors) {
      addActor(actor);
    }
    setSize(width, height);
    float parentHeight = parent.getHeight() - INFO_PANEL_HEIGHT;
    height *= getScaleY();
    float x = Math.round((parentWidth - width * scaleX) / 2f);
    if (parentHeight > height) {
      setPosition(x,
          Math.round((parentHeight - height) / 2f));
    } else {
      setPosition(x,
          Math.round(parentHeight - height - GAP));
    }
  }

  private Rectangle getBound(RegionAttachment attachment) {
    float x = attachment.getX();
    float y = attachment.getY();
    float width = attachment.getWidth();
    float height = attachment.getHeight();
    return new Rectangle(x - width / 2f, y - height / 2f, width, height);
  }

  private Rectangle getBound(Animation animation, Map<String, Rectangle> actorBounds) {
    Rectangle bound = new Rectangle(-5f, -5f, 10f, 10f); // origin always in bound
    for (Animation.Timeline timeline : animation.getTimelines()) {
      if (timeline instanceof Animation.AttachmentTimeline) {
        for (String name : ((Animation.AttachmentTimeline) timeline).getAttachmentNames()) {
          if (actorBounds.containsKey(name)) {
            bound.merge(actorBounds.get(name));
          }
        }
      }
    }
    return bound;
  }
}
