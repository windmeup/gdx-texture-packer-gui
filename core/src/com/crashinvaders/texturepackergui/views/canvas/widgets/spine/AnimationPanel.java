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
import com.badlogic.gdx.tools.spine.Point;
import com.badlogic.gdx.tools.spine.SkeletonSettings;
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

  private static final float GAP = 30f;

  private static final float INFO_PANEL_HEIGHT = 18f;

  private static final SkeletonRenderer skeletonRenderer = new SkeletonRenderer();

  private ShapeDrawer shapeDrawer;

  private final NinePatchDrawable borderFrame;

  private final NinePatch actorBorder;

  private final Rectangle aabb = new Rectangle();

  private SkeletonData skeletonData;

  private SkeletonSettings skeletonSettings;

  private final Hovered hovered;

  private final Selected selected;

  @Getter
  @Setter
  private boolean showCoords;

  AnimationPanel(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
    setTouchable(Touchable.disabled);
    borderFrame = new NinePatchDrawable(skin.getPatch("custom/white_frame")).tint(Color.BLACK);
    actorBorder = skin.getPatch("custom/white_frame");
    hovered = new Hovered(skin);
    selected = new Selected(skin);
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
    } else if (selected.animationActor != null) {
      if (shapeDrawer == null) {
        shapeDrawer = new ShapeDrawer(batch, GraphicsUtils.onePix);
      }
      shapeDrawer.setDefaultLineWidth(2f);
      selected.animationActor.drawCoords(shapeDrawer, aabb, x, y, scaleX, scaleY);
    }
  }

  @Override
  public void setScale(float scaleXY) {
    super.setScale(scaleXY);
    relayout();
  }

  @Override
  public void clear() {
    super.clear();
    selected.clearAnimationActor();
    hovered.clearAnimationActor();
    skeletonData = null;
    skeletonSettings = null;
    setSize(0f, 0f);
  }

  public void set(SkeletonData skeletonData, SkeletonSettings skeletonSettings) {
    this.skeletonData = skeletonData;
    this.skeletonSettings = skeletonSettings;
    layout();
  }

  public void relayout() {
    super.clear();
    selected.clearAnimationActor();
    hovered.clearAnimationActor();
    layout();
  }

  public void translate(float deltaX, float deltaY, float parentWidth, float parentHeight) {
    float panelHeight = getHeight() * getScaleY();
    float height = parentHeight - AnimationPanel.INFO_PANEL_HEIGHT;
    float toY = getY() + deltaY;
    if (panelHeight < height) {
      toY = Math.min(Math.max(toY, AnimationPanel.GAP)
          , height - panelHeight - AnimationPanel.GAP);
    } else {
      toY = Math.max(Math.min(toY, AnimationPanel.GAP)
          , height - panelHeight - AnimationPanel.GAP);
    }
    setPosition(
        Math.max(Math.min(getX() + deltaX, AnimationPanel.GAP)
            , parentWidth - getWidth() * getScaleX() - AnimationPanel.GAP),
        toY);
  }

  public void select() {
    if (hovered.animationActor != null) {
      selected.setAnimationActor(hovered.animationActor);
    }
  }

  public AnimationActor getSelected() {
    return selected.animationActor;
  }

  private void layout() {
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
      if (selected.animationName != null && actor.getName().equals(selected.animationName)) {
        selected.setAnimationActor(actor);
      }
    }
    addActor(selected);
    addActor(hovered);
    setSize(width, height);
    float parentHeight = parent.getHeight();
    if (selected.animationActor == null) {
      parentHeight -= INFO_PANEL_HEIGHT;
      height *= getScaleY();
      float x = Math.round((parentWidth - width * scaleX) / 2f);
      if (parentHeight > height) {
        setPosition(x,
            Math.round((parentHeight - height) / 2f));
      } else {
        setPosition(x,
            Math.round(parentHeight - height - GAP));
      }
    } else { // selected to center
      actorBound = selected.animationActor.getBound();
      float x = getX() + (actorBound.x + selected.animationActor.getX() + actorBound.width / 2f) * getScaleX();
      float y = getY() + (actorBound.y + selected.animationActor.getY() + actorBound.height / 2f) * getScaleY();
      translate(parentWidth / 2f - x, parentHeight / 2f - y, parentWidth, parentHeight);
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

  private class Selected extends Spotlight {
    private String animationName;

    private Selected(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
      super(skin, "blue", "blue");
    }

    @Override
    void setAnimationActor(AnimationActor animationActor) {
      super.setAnimationActor(animationActor);
      if (animationActor != null) {
        this.animationName = animationActor.getName();
      }
    }

    @Override
    void clearAnimationActor() {
      super.clearAnimationActor();
    }
  }

  private class Hovered extends Spotlight {
    private boolean active;

    private Hovered(com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
      super(skin, "orange", "white");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      if (animationActor == null || animationActor == selected.animationActor) {
        return;
      }
      super.draw(batch, parentAlpha);
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
        clearAnimationActor();
      }
      if (withinPage) {
        AnimationActor hit = hit(pointerPos);
        if (hit != null) {
          setAnimationActor(hit);
        }
        if (hit == null && active) {
          clearAnimationActor();
        }
      }
    }

    @Override
    void setAnimationActor(AnimationActor animationActor) {
      super.setAnimationActor(animationActor);
      active = true;
    }

    void clearAnimationActor() {
      super.clearAnimationActor();
      active = false;
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

  private class Spotlight extends Actor {
    private final NinePatch border;
    private final Color borderColor;
    private final Color textFrameColor;
    private final Color textColor;
    private final BitmapFont font;
    private final GlyphLayout glText;
    private final TextureRegion whiteTex;
    AnimationActor animationActor;

    private Spotlight(com.badlogic.gdx.scenes.scene2d.ui.Skin skin, String color, String textColor) {
      border = skin.getPatch("custom/white_frame");
      borderColor = skin.getColor(color);
      textFrameColor = new Color(0x333333aa);
      this.textColor = skin.getColor(textColor);
      font = skin.getFont("default-font");
      glText = new GlyphLayout();
      whiteTex = skin.getRegion("white");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      if (animationActor == null) return;
      Rectangle bound = animationActor.getBound();
      float framePad = 1f;
      float x = animationActor.getX() + bound.getX() - framePad;
      float y = animationActor.getY() + bound.getY() - framePad;
      float width = bound.getWidth() + framePad * 2f;
      float height = bound.getHeight() + framePad * 2f;
      batch.setColor(borderColor);
      border.draw(batch, x, y, width, height);
      drawText(batch, x, y, width);
    }

    void setAnimationActor(AnimationActor animationActor) {
      if (this.animationActor == animationActor) return;
      this.animationActor = animationActor;
      font.getData().setScale(1f);
      String animationName = animationActor.getName();
      Point point = skeletonSettings.getAnimationOffsets().get(animationName);
      int x = point == null ? 0 : point.getX();
      int y = point == null ? 0 : point.getY();
      glText.setText(font, animationName + ": " + x + "," + y, textColor, 0f, Align.left, false);
    }

    void clearAnimationActor() {
      animationActor = null;
    }

    void drawText(Batch batch, float x, float y, float width) {
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
      batch.setColor(textFrameColor);
      batch.draw(whiteTex, textX - 10f, textY - 6f, glText.width + 20f, glText.height + 10f);
      batch.setColor(Color.WHITE);
      font.getData().setScale(1f);
      font.draw(batch, glText, textX, textY + glText.height);
    }
  }
}
