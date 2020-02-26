package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.crashinvaders.common.scene2d.ScrollFocusCaptureInputListener;
import com.crashinvaders.texturepackergui.views.canvas.widgets.OuterFade;
import com.esotericsoftware.spine.SkeletonData;
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

  private void translate(float deltaX, float deltaY) {
    float panelHeight = animationPanel.getHeight() * animationPanel.getScaleY();
    float height = getHeight() - AnimationPanel.INFO_PANEL_HEIGHT;
    float toY = animationPanel.getY() + deltaY;
    if (panelHeight < height) {
      toY = Math.min(Math.max(toY, AnimationPanel.GAP)
          , height - panelHeight - AnimationPanel.GAP);
    } else {
      toY = Math.max(Math.min(toY, AnimationPanel.GAP)
          , height - panelHeight - AnimationPanel.GAP);
    }
    animationPanel.setPosition(
        Math.max(Math.min(animationPanel.getX() + deltaX, AnimationPanel.GAP)
            , getWidth() - animationPanel.getWidth() * animationPanel.getScaleX() - AnimationPanel.GAP),
        toY);
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
        translate(x - lastPos.x, y - lastPos.y);
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
      translate(0f, amount * 200f * getScaleY());
      return true;
    }
  }
}
