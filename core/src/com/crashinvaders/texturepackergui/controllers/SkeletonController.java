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
      data.getSkeleton().setX(skeletonX);
      modified();
    }
  }

  public void setSkeletonY(int skeletonY) throws IOException {
    if (skeletonData != null) {
      skeletonData.setY(skeletonY);
      data.getSkeleton().setY(skeletonY);
      modified();
    }
  }

  public void setSkeletonWidth(int skeletonWidth) throws IOException {
    if (skeletonData != null) {
      skeletonData.setWidth(skeletonWidth);
      data.getSkeleton().setWidth(skeletonWidth);
      modified();
    }
  }

  public void setSkeletonHeight(int skeletonHeight) throws IOException {
    if (skeletonData != null) {
      skeletonData.setHeight(skeletonHeight);
      data.getSkeleton().setHeight(skeletonHeight);
      modified();
    }
  }

  private void modified() throws IOException {
    mainController.getCanvas().getAnimationViewer().getAnimationPanel().layout();
    JacksonUtils.writeValue(skeletonFile, data);
  }
}
