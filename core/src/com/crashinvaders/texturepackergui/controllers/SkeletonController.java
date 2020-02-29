package com.crashinvaders.texturepackergui.controllers;

import com.crashinvaders.texturepackergui.controllers.main.MainController;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.AnimationPanel;
import com.esotericsoftware.spine.SkeletonData;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import lombok.Setter;

@Component
public class SkeletonController {

  @Setter
  private SkeletonData skeletonData;

  @Inject
  MainController mainController;

  public void clear() {
    skeletonData = null;
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
      // TODO
    }
  }

  private void modified() {
    getAnimationPanel().relayout();
  }

  private AnimationPanel getAnimationPanel() {
    return mainController.getCanvas().getAnimationViewer().getAnimationPanel();
  }
}
