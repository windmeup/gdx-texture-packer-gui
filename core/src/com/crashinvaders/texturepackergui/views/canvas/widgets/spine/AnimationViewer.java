package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.crashinvaders.texturepackergui.views.canvas.widgets.OuterFade;
import com.esotericsoftware.spine.SkeletonData;

public class AnimationViewer extends WidgetGroup {

  private final AnimationPanel animationPanel;

  public AnimationViewer(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
    animationPanel = new AnimationPanel(skin);
    addActor(animationPanel);
    OuterFade outerFade = new OuterFade(skin);
    outerFade.setCenter(animationPanel);
    addActor(outerFade);
  }

  public void setSkeletonData(SkeletonData skeletonData) {
    animationPanel.setSkeletonData(skeletonData);
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
}
