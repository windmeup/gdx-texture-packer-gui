package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
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

public class AnimationViewer extends WidgetGroup {

  private static final SkeletonRenderer skeletonRenderer = new SkeletonRenderer();

  private static final float GAP = 10f;

  public void setSkeletonData(SkeletonData skeletonData) {
    clear();
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
    SkeletonActor skeletonActor;
    Rectangle bound;
    float frameX = GAP;
    float frameWidth;
    float lineHeight = GAP;
    float width = getWidth();
    float height = GAP;
    List<SkeletonActor> frames = new ArrayList<>();
    for (Animation animation : skeletonData.getAnimations()) {
      bound = new Rectangle(-5f, -5f, 10f, 10f); // origin always in bound
      for (Animation.Timeline timeline : animation.getTimelines()) {
        if (timeline instanceof Animation.AttachmentTimeline) {
          for (String name : ((Animation.AttachmentTimeline) timeline).getAttachmentNames()) {
            if (frameBounds.containsKey(name)) {
              bound.merge(frameBounds.get(name));
            }
          }
        }
      }
      lineHeight = Math.max(lineHeight, bound.getHeight() + GAP);
      frameWidth = bound.getWidth();
      if (frameX + frameWidth + GAP > width) {
        frameX = GAP;
        for (SkeletonActor frame : frames) {
          frame.setY(frame.getY() + lineHeight);
        }
        height += lineHeight;
        lineHeight = bound.getHeight() + GAP;
      }
      skeletonActor = new SkeletonActor(skeletonRenderer, new Skeleton(skeletonData),
          new AnimationState(new AnimationStateData(skeletonData)));
      skeletonActor.getAnimationState().setAnimation(0, animation.getName(), true);
      skeletonActor.setPosition(frameX - bound.getX(), GAP - bound.getY());
      frames.add(skeletonActor);
      frameX += bound.getWidth() + GAP;
    }
    height += lineHeight;
    float yOffset = (getHeight() - height) / 2f;
    for (SkeletonActor frame : frames) {
      frame.setY(frame.getY() + yOffset);
      addActor(frame);
    }
  }

  /**
   * @see com.crashinvaders.common.scene2d.lml.AnimatedImage
   * Small patch to support non-continuous rendering mode
   */
  @Override
  public void act(float delta) {
    super.act(delta);
    if (isVisible()) {
      Gdx.graphics.requestRendering();
    }
  }

  private Rectangle getBound(RegionAttachment attachment) {
    float x = attachment.getX();
    float y = attachment.getY();
    float width = attachment.getWidth();
    float height = attachment.getHeight();
    return new Rectangle(x - width / 2f, y - height / 2f, width, height);
  }
}
