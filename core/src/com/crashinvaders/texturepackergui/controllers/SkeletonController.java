package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.tools.spine.Point;
import com.badlogic.gdx.tools.spine.SkeletonSettings;
import com.crashinvaders.texturepackergui.controllers.main.MainController;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.AnimationActor;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.AnimationPanel;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.editor.EditAnimationDialog;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;

@Component
public class SkeletonController {

  private SkeletonData skeletonData;

  private SkeletonSettings skeletonSettings;

  @Inject
  MainController mainController;

  public void set(SkeletonData skeletonData, SkeletonSettings skeletonSettings) {
    this.skeletonData = skeletonData;
    this.skeletonSettings = skeletonSettings;
  }

  public void clear() {
    skeletonData = null;
    skeletonSettings = null;
  }

  public void setSkeletonX(int skeletonX) {
    if (skeletonData != null && skeletonData.getX() != skeletonX) {
      skeletonData.setX(skeletonX);
      modified();
    }
  }

  public void setSkeletonY(int skeletonY) {
    if (skeletonData != null && skeletonData.getY() != skeletonY) {
      skeletonData.setY(skeletonY);
      modified();
    }
  }

  public void setSkeletonWidth(int skeletonWidth) {
    if (skeletonData != null && skeletonData.getWidth() != skeletonWidth) {
      skeletonData.setWidth(skeletonWidth);
      modified();
    }
  }

  public void setSkeletonHeight(int skeletonHeight) {
    if (skeletonData != null && skeletonData.getHeight() != skeletonHeight) {
      skeletonData.setHeight(skeletonHeight);
      modified();
    }
  }

  public void moveSelected(int offsetX, int offsetY) {
    if (skeletonData != null) {
      AnimationPanel animationPanel = getAnimationPanel();
      AnimationActor animationActor = animationPanel.getSelected();
      if (animationActor != null) {
        Animation.AttachmentTimeline attachmentTimeline;
        int slotIndex;
        Attachment attachment;
        RegionAttachment regionAttachment;
        String animationName = animationActor.getName();
        for (Animation.Timeline timeline : skeletonData.findAnimation(animationName).getTimelines()) {
          if (timeline instanceof Animation.AttachmentTimeline) {
            attachmentTimeline = (Animation.AttachmentTimeline) timeline;
            slotIndex = attachmentTimeline.getSlotIndex();
            for (String name : attachmentTimeline.getAttachmentNames()) {
              attachment = skeletonData.getDefaultSkin().getAttachment(slotIndex, name);
              if (attachment instanceof RegionAttachment) {
                regionAttachment = (RegionAttachment) attachment;
                regionAttachment.setX(regionAttachment.getX() + offsetX);
                regionAttachment.setY(regionAttachment.getY() + offsetY);
                regionAttachment.updateOffset();
              }
            }
          }
        }
        Point point = skeletonSettings.getAnimationOffsets().computeIfAbsent(
            animationName, k -> new Point(0, 0)
        );
        point.translate(offsetX, offsetY);
        animationPanel.relayout();
      }
    }
  }

  public void editSelected() {
    if (skeletonData != null) {
      AnimationPanel animationPanel = getAnimationPanel();
      AnimationActor animationActor = animationPanel.getSelected();
      if (animationActor != null) {
        String name = animationActor.getName();
        EditAnimationDialog dialog =
            new EditAnimationDialog(animationActor, skeletonSettings.getAnimationBounds().get(name), boundsVertices -> {
              // TODO
            });
        mainController.getStage().addActor(dialog);
      }
    }
  }

  private void modified() {
    getAnimationPanel().relayout();
  }

  private AnimationPanel getAnimationPanel() {
    return mainController.getCanvas().getAnimationViewer().getAnimationPanel();
  }
}
