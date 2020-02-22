package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.esotericsoftware.spine.utils.SkeletonActor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationPanel extends Group {

  public static final float GAP = 10f;

  public static final float INFO_PANEL_HEIGHT = 18f;

  private static final SkeletonRenderer skeletonRenderer = new SkeletonRenderer();

  private final NinePatchDrawable borderFrame;

  private SkeletonData skeletonData;

  AnimationPanel(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
    setTouchable(Touchable.disabled);
    borderFrame = new NinePatchDrawable(skin.getPatch("custom/white_frame")).tint(Color.BLACK);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    batch.setColor(Color.BLACK);
    borderFrame.draw(batch, getX(), getY(), getWidth() * getScaleX(), getHeight() * getScaleY());
    super.draw(batch, parentAlpha);
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
    Map<String, Rectangle> frameBounds = new HashMap<>();
    Attachment attachment;
    for (Skin.SkinEntry skinEntry : skin.getAttachments()) {
      attachment = skinEntry.getAttachment();
      if (attachment instanceof RegionAttachment) {
        frameBounds.put(attachment.getName(), getBound((RegionAttachment) attachment));
      }
    }
    Rectangle bound;
    float maxFrameWidth = 0f;
    for (Animation animation : skeletonData.getAnimations()) {
      bound = getBound(animation, frameBounds);
      maxFrameWidth = Math.max(maxFrameWidth, bound.getWidth());
    }
    SkeletonActor skeletonActor;
    float frameX = GAP;
    float frameWidth;
    float frameHeight;
    float lineHeight = GAP;
    float scaleX = getScaleX();
    Group parent = getParent();
    float parentWidth = parent.getWidth();
    float width = Math.max((parentWidth - GAP * 2f) / scaleX, maxFrameWidth + GAP * 2f);
    float height = GAP;
    float lineIncrease;
    List<SkeletonActor> frames = new ArrayList<>();
    for (Animation animation : skeletonData.getAnimations()) {
      bound = getBound(animation, frameBounds);
      frameHeight = bound.getHeight() + GAP;
      frameWidth = bound.getWidth();
      if (frameX + frameWidth + GAP > width) {
        frameX = GAP;
        for (SkeletonActor frame : frames) {
          frame.setY(frame.getY() + frameHeight);
        }
        height += lineHeight;
        lineHeight = frameHeight;
      } else {
        lineIncrease = frameHeight - lineHeight;
        if (lineIncrease > 0f) {
          for (SkeletonActor frame : frames) {
            frame.setY(frame.getY() + lineIncrease);
          }
          lineHeight = frameHeight;
        }
      }
      skeletonActor = new SkeletonActor(skeletonRenderer, new Skeleton(skeletonData),
          new AnimationState(new AnimationStateData(skeletonData)));
      skeletonActor.getAnimationState().setAnimation(0, animation.getName(), true);
      skeletonActor.setPosition(frameX - bound.getX(), GAP - bound.getY());
      frames.add(skeletonActor);
      frameX += bound.getWidth() + GAP;
    }
    height += lineHeight;
    for (SkeletonActor frame : frames) {
      addActor(frame);
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

  private Rectangle getBound(Animation animation, Map<String, Rectangle> frameBounds) {
    Rectangle bound = new Rectangle(-5f, -5f, 10f, 10f); // origin always in bound
    for (Animation.Timeline timeline : animation.getTimelines()) {
      if (timeline instanceof Animation.AttachmentTimeline) {
        for (String name : ((Animation.AttachmentTimeline) timeline).getAttachmentNames()) {
          if (frameBounds.containsKey(name)) {
            bound.merge(frameBounds.get(name));
          }
        }
      }
    }
    return bound;
  }
}
