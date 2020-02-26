package com.crashinvaders.texturepackergui.controllers;

import com.crashinvaders.texturepackergui.controllers.main.MainController;
import com.crashinvaders.texturepackergui.utils.JacksonUtils;
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
  }

  public void setSkeletonPath(String skeletonPath) throws IOException {
    skeletonFile = Paths.get(skeletonPath).toFile();
    data = JacksonUtils.readValue(
        skeletonFile, com.badlogic.gdx.tools.spine.data.SkeletonData.class
    );
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

  private void modified(boolean dataChanged) throws IOException {
    mainController.getCanvas().getAnimationViewer().getAnimationPanel().layout();
    if (dataChanged) {
      JacksonUtils.writeValue(skeletonFile, data);
    }
  }
}
