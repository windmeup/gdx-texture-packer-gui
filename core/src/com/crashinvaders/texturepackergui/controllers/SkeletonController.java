package com.crashinvaders.texturepackergui.controllers;

import com.crashinvaders.texturepackergui.controllers.main.MainController;
import com.crashinvaders.texturepackergui.utils.JacksonUtils;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.AnimationPanel;
import com.esotericsoftware.spine.SkeletonData;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Component
public class SkeletonController {

  private File skeletonFile;

  @Setter
  private SkeletonData skeletonData;

  private com.badlogic.gdx.tools.spine.data.SkeletonData data;

  @Inject
  MainController mainController;

  public void clear() {
    skeletonFile = null;
    skeletonData = null;
    data = null;
  }

  public void setSkeletonPath(String skeletonPath) throws IOException {
    skeletonFile = Paths.get(skeletonPath).toFile();
    data = JacksonUtils.readValue(
        skeletonFile, com.badlogic.gdx.tools.spine.data.SkeletonData.class
    );
    if (data == null || data.getSkeleton() == null || data.getAnimations() == null || data.getBones() == null ||
        data.getSkins() == null || data.getSlots() == null) {
      clear();
      throw new IOException("not skeleton json");
    }
  }

  public void setSkeletonX(int skeletonX) throws IOException {
    if (skeletonData != null) {
      skeletonData.setX(skeletonX);
      modified(data.getSkeleton().setX(skeletonX));
    }
  }

  public void setSkeletonY(int skeletonY) throws IOException {
    if (skeletonData != null) {
      skeletonData.setY(skeletonY);
      modified(data.getSkeleton().setY(skeletonY));
    }
  }

  public void setSkeletonWidth(int skeletonWidth) throws IOException {
    if (skeletonData != null) {
      skeletonData.setWidth(skeletonWidth);
      modified(data.getSkeleton().setWidth(skeletonWidth));
    }
  }

  public void setSkeletonHeight(int skeletonHeight) throws IOException {
    if (skeletonData != null) {
      skeletonData.setHeight(skeletonHeight);
      modified(data.getSkeleton().setHeight(skeletonHeight));
    }
  }

  public void moveSelected(int offsetX, int offsetY) {
    if (skeletonData != null) {
      // TODO delete data
    }
  }

  private void modified(boolean dataChanged) throws IOException {
    if (dataChanged) {
      getAnimationPanel().relayout();
      JacksonUtils.writeValue(skeletonFile, data);
    }
  }

  private AnimationPanel getAnimationPanel() {
    return mainController.getCanvas().getAnimationViewer().getAnimationPanel();
  }
}
