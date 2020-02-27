package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.crashinvaders.texturepackergui.utils.GraphicsUtils;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import lombok.Getter;
import lombok.Setter;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationPanel extends Group {

  public static final float GAP = 30f;

  public static final float INFO_PANEL_HEIGHT = 18f;

  private static final SkeletonRenderer skeletonRenderer = new SkeletonRenderer();

  private ShapeDrawer shapeDrawer;

  private final NinePatchDrawable borderFrame;

  private final NinePatch actorBorder;

  private final Rectangle aabb = new Rectangle();

  private SkeletonData skeletonData;

  private final Spotlight spotlight;

  @Getter
  @Setter
  private boolean showCoords;

  AnimationPanel(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
    setTouchable(Touchable.disabled);
    borderFrame = new NinePatchDrawable(skin.getPatch("custom/white_frame")).tint(Color.BLACK);
    actorBorder = skin.getPatch("custom/white_frame");
    spotlight = new Spotlight(skin);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    batch.setColor(Color.BLACK);
    float x = getX();
    float y = getY();
    float scaleX = getScaleX();
    float scaleY = getScaleY();
    borderFrame.draw(batch, x, y, getWidth() * scaleX, getHeight() * scaleY);
    super.draw(batch, parentAlpha);
    if (showCoords) {
      if (shapeDrawer == null) {
        shapeDrawer = new ShapeDrawer(batch, GraphicsUtils.onePix);
      }
      shapeDrawer.setDefaultLineWidth(2f);
      for (Actor actor : getChildren()) {
        if (actor instanceof AnimationActor) {
          ((AnimationActor) actor).drawCoords(shapeDrawer, aabb, x, y, scaleX, scaleY);
        }
      }
    }
  }

  @Override
  public void setScale(float scaleXY) {
    super.setScale(scaleXY);
    layout();
  }

  public void setSkeletonData(SkeletonData skeletonData) {
    this.skeletonData = skeletonData;
    layout();
  }

  public void layout() {
    clear();
    if (skeletonData == null) {
      return;
    }
    Skin skin = skeletonData.getDefaultSkin();
    if (skin == null) {
      return;
    }
    aabb.set(skeletonData.getX(), skeletonData.getY(), skeletonData.getWidth(), skeletonData.getHeight());
    Map<String, Rectangle> actorBounds = new HashMap<>();
    Attachment attachment;
    for (Skin.SkinEntry skinEntry : skin.getAttachments()) {
      attachment = skinEntry.getAttachment();
      if (attachment instanceof RegionAttachment) {
        actorBounds.put(attachment.getName(), getBound((RegionAttachment) attachment));
      }
    }
    Rectangle actorBound;
    float maxActorWidth = 0f;
    for (Animation animation : skeletonData.getAnimations()) {
      actorBound = getBound(animation, actorBounds);
      maxActorWidth = Math.max(maxActorWidth, actorBound.getWidth());
    }
    AnimationActor animationActor;
    float actorX = GAP;
    float actorWidth;
    float actorHeight;
    float rowHeight = GAP;
    float scaleX = getScaleX();
    Group parent = getParent();
    float parentWidth = parent.getWidth();
    float width = Math.max((parentWidth - GAP * 2f) / scaleX, maxActorWidth + GAP * 2f);
    float height = GAP;
    float rowIncrease;
    List<AnimationActor> actors = new ArrayList<>();
    List<AnimationActor> currentRow = new ArrayList<>();
    for (Animation animation : skeletonData.getAnimations()) {
      actorBound = getBound(animation, actorBounds);
      actorHeight = actorBound.getHeight() + GAP;
      actorWidth = actorBound.getWidth();
      if (actorX + actorWidth + GAP > width) {
        actorX = GAP;
        for (AnimationActor actor : actors) {
          actor.setY(actor.getY() + actorHeight);
        }
        height += rowHeight;
        rowHeight = actorHeight;
        currentRow.clear();
      } else {
        rowIncrease = actorHeight - rowHeight;
        if (rowIncrease > 0f) {
          for (AnimationActor actor : actors) {
            if (!currentRow.contains(actor)) {
              actor.setY(actor.getY() + rowIncrease);
            }
          }
          rowHeight = actorHeight;
        }
      }
      animationActor = new AnimationActor(skeletonRenderer, new Skeleton(skeletonData),
          new AnimationState(new AnimationStateData(skeletonData)), actorBound, actorBorder);
      animationActor.setName(animation.getName());
      animationActor.getAnimationState().setAnimation(0, animation.getName(), true);
      animationActor.setPosition(actorX - actorBound.getX(), GAP - actorBound.getY());
      actors.add(animationActor);
      currentRow.add(animationActor);
      actorX += actorBound.getWidth() + GAP;
    }
    height += rowHeight;
    for (AnimationActor actor : actors) {
      addActor(actor);
    }
    addActor(spotlight);
    setSize(width, height);
    float parentHeight = parent.getHeight() - INFO_PANEL_HEIGHT;
    height *= getScaleY();
    float x = Math.round((parentWidth - width * scaleX) / 2f);
    if (parentHeight > height) {
      setPosition(x,
          Math.round((parentHeight - height) / 2f));
    } else {
      setPosition(x,
          Math.round(parentHeight - height - GAP));
    }
  }

  private Rectangle getBound(RegionAttachment attachment) {
    float x = attachment.getX();
    float y = attachment.getY();
    float width = attachment.getWidth();
    float height = attachment.getHeight();
    return new Rectangle(x - width / 2f, y - height / 2f, width, height);
  }

  private Rectangle getBound(Animation animation, Map<String, Rectangle> actorBounds) {
    Rectangle bound = new Rectangle(-5f, -5f, 10f, 10f); // origin always in bound
    for (Animation.Timeline timeline : animation.getTimelines()) {
      if (timeline instanceof Animation.AttachmentTimeline) {
        for (String name : ((Animation.AttachmentTimeline) timeline).getAttachmentNames()) {
          if (actorBounds.containsKey(name)) {
            bound.merge(actorBounds.get(name));
          }
        }
      }
    }
    return bound.merge(aabb);
  }

  private static final Vector2 tmpCoords = new Vector2();
  private static final Rectangle tmpBounds = new Rectangle();

  private Vector2 screenToLocal(int screenX, int screenY) {
    getStage().screenToStageCoordinates(tmpCoords.set(screenX, screenY));
    stageToLocalCoordinates(tmpCoords);
    return tmpCoords;
  }

  private class Spotlight extends Actor {
    private final Color colorSpotlight;
    private final Color colorTextFrame;

    private final TextureRegion whiteTex;
    private final NinePatch spotlightBorder;
    private final BitmapFont font;
    private final GlyphLayout glText;

    private boolean active;
    private AnimationActor animationActor;

    public Spotlight(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
      whiteTex = skin.getRegion("white");
      spotlightBorder = skin.getPatch("custom/white_frame");
      font = skin.getFont("default-font");
      glText = new GlyphLayout();

      colorSpotlight = skin.getColor("orange");
      colorTextFrame = new Color(0x333333aa);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      if (animationActor == null) return;

      // Frame
      Rectangle bound = animationActor.getBound();
      float framePad = 1f;
      float x = animationActor.getX() + bound.getX() - framePad;
      float y = animationActor.getY() + bound.getY() - framePad;
      float width = bound.getWidth() + framePad * 2f;
      float height = bound.getHeight() + framePad * 2f;

      batch.setColor(colorSpotlight);
      spotlightBorder.draw(batch, x, y, width, height);

      // Text
      float textX;
      AnimationPanel parent = AnimationPanel.this;
      float parentWidth = parent.getWidth();
      if (glText.width > parentWidth - 20f) {
        textX = (parentWidth - glText.width) * 0.5f;
      } else {
        textX = x + width * 0.5f - glText.width * 0.5f;
        textX = Math.max(textX, 10f);
        textX = Math.min(textX, parentWidth - glText.width - 10f);
      }
      float textY = y - glText.height - 5f;
      batch.setColor(colorTextFrame);
      batch.draw(whiteTex, textX - 10f, textY - 6f, glText.width + 20f, glText.height + 10f);
      batch.setColor(Color.WHITE);
      font.getData().setScale(1f);
      font.draw(batch, glText, textX, textY + glText.height);
    }

    @Override
    public void act(float delta) {
      AnimationPanel animationPanel = AnimationPanel.this;
      if (!animationPanel.isVisible()) {
        return;
      }
      super.act(delta);
      Vector2 pointerPos = animationPanel.screenToLocal(Gdx.input.getX(), Gdx.input.getY());
      boolean withinPage = tmpBounds.set(0f, 0f, animationPanel.getWidth(), animationPanel.getHeight())
          .contains(pointerPos);
      if (!withinPage && active) {
        clearSpotlight();
      }
      if (withinPage) {
        AnimationActor hit = hit(pointerPos);
        if (hit != null) {
          spotlight(hit);
        }
        if (hit == null && active) {
          clearSpotlight();
        }
      }
    }

    private void clearSpotlight() {
      animationActor = null;
      active = false;
    }

    private void spotlight(AnimationActor animationActor) {
      if (this.animationActor == animationActor) return;
      this.animationActor = animationActor;
      active = true;
      font.getData().setScale(1f);
      glText.setText(font, animationActor.getName(), Color.WHITE, 0f, Align.left, false);
    }

    private AnimationActor hit(Vector2 position) {
      AnimationActor animationActor;
      for (Actor actor : AnimationPanel.this.getChildren()) {
        if (actor instanceof AnimationActor) {
          animationActor = (AnimationActor) actor;
          tmpBounds.set(animationActor.getBound());
          tmpBounds.x += animationActor.getX();
          tmpBounds.y += animationActor.getY();
          if (tmpBounds.contains(position)) {
            return animationActor;
          }
        }
      }
      return null;
    }
  }
}
