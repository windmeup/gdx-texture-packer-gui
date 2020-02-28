package com.crashinvaders.texturepackergui.views.canvas.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import lombok.Setter;

public class OuterFade extends Widget {
  private final Color COLOR_DIM = new Color(0x00000040);

  private final TextureRegion whiteTexture;

  @Setter
  private Actor center;

  public OuterFade(Skin skin) {
    whiteTexture = skin.getRegion("white");
    setFillParent(true);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
    if (center == null || center.getWidth() == 0f) return;

    Color col;
    float x = center.getX();
    float y = center.getY();
    float width = center.getWidth() * center.getScaleX();
    float height = center.getHeight() * center.getScaleY();

    // Fading all around page
    col = COLOR_DIM;
    batch.setColor(col.r, col.g, col.b, col.a * getColor().a * parentAlpha);
    batch.draw(whiteTexture,
        getX() + 0f,
        getY() + y + height,
        getWidth(),
        getHeight());
    batch.draw(whiteTexture,
        getX() + 0f,
        getY() + y - getHeight(),
        getWidth(),
        getHeight());
    batch.draw(whiteTexture,
        getX() + 0f,
        getY() + y,
        x,
        height);
    batch.draw(whiteTexture,
        getX() + x + width,
        getY() + y,
        getWidth(),
        height);
  }
}
