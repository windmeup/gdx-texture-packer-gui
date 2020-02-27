package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.crashinvaders.common.scene2d.ScrollFocusCaptureInputListener;
import com.crashinvaders.texturepackergui.views.canvas.widgets.OuterFade;
import lombok.Getter;

public class AnimationViewer extends WidgetGroup {

  private static final int[] ZOOM_LEVELS = {100, 120, 150, 200, 300, 400, 600, 800};

  @Getter
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
    animationPanel.relayout();
  }

  public void zoomIn() {
    zoom(1);
  }

  public void zoomOut() {
    zoom(-1);
  }

  public interface Listener {
    void onZoomChanged(int percentage);
  }

  private void zoom(int amount) {
    if (!isVisible()) {
      return;
    }
    int zoomIndex = Math.max(0, Math.min(ZOOM_LEVELS.length - 1, this.zoomIndex + amount));
    if (this.zoomIndex == zoomIndex) {
      return;
    }
    this.zoomIndex = zoomIndex;
    float scale = (float) ZOOM_LEVELS[zoomIndex] / 100f;
    animationPanel.setScale(scale);
    listener.onZoomChanged(ZOOM_LEVELS[zoomIndex]);
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
      animationPanel.select();
      dragging = true;
      lastPos.set(x, y);
      return true;
    }

    @Override
    public void touchDragged(InputEvent event, float x, float y, int pointer) {
      if (dragging) {
        animationPanel.translate(x - lastPos.x, y - lastPos.y, getWidth(), getHeight());
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
      animationPanel.translate(0f, amount * 200f * getScaleY(), getWidth(), getHeight());
      return true;
    }
  }
}
