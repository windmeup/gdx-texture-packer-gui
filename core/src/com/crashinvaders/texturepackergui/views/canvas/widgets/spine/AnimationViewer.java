package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.crashinvaders.common.scene2d.ScrollFocusCaptureInputListener;
import com.crashinvaders.texturepackergui.views.canvas.widgets.OuterFade;
import com.esotericsoftware.spine.SkeletonData;

public class AnimationViewer extends WidgetGroup {

  private static final int[] ZOOM_LEVELS = {100, 120, 150, 200, 300, 400, 600, 800, 1000};

  private final AnimationPanel animationPanel;

  private final Listener listener;

  private int zoomIndex;

  public AnimationViewer(com.badlogic.gdx.scenes.scene2d.ui.Skin skin, Listener listener) {
    animationPanel = new AnimationPanel(skin);
    addActor(animationPanel);
    this.listener = listener;
    OuterFade outerFade = new OuterFade(skin);
    outerFade.setCenter(animationPanel);
    addActor(outerFade);
    addListener(new AnimationZoomListener());
    addListener(new ScrollFocusCaptureInputListener());
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

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible && getStage() != null) {
      getStage().setScrollFocus(this);
    }
  }

  @Override
  protected void sizeChanged() {
    animationPanel.layout();
  }

  public interface Listener {
    void onZoomChanged(int percentage);
  }

  private class AnimationZoomListener extends InputListener {

    private final Vector2 lastPos = new Vector2();

    private boolean dragging = false;

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
      getStage().setScrollFocus(AnimationViewer.this);
      if (button != 0) {
        return false;
      }
      dragging = true;
      lastPos.set(x, y);
      return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      if (dragging) {
        animationPanel.setPosition(
            animationPanel.getX() - lastPos.x + x,
            animationPanel.getY() - lastPos.y + y);
        lastPos.set(x, y);
      }
    }

    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
      if (button != 0) {
        return;
      }
      dragging = false;
    }

    @Override
    public boolean scrolled(InputEvent event, float x, float y, int amount) {
      zoomIndex = Math.max(0, Math.min(ZOOM_LEVELS.length - 1, zoomIndex - amount));
      float scale = (float) ZOOM_LEVELS[zoomIndex] / 100f;
      animationPanel.setScale(scale);
      listener.onZoomChanged(ZOOM_LEVELS[zoomIndex]);
      return true;
    }
  }
}
