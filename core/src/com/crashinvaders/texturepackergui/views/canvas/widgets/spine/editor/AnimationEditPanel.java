package com.crashinvaders.texturepackergui.views.canvas.widgets.spine.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.crashinvaders.texturepackergui.utils.GeoUtils;
import com.crashinvaders.texturepackergui.utils.GraphicsUtils;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.AnimationActor;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.List;

public class AnimationEditPanel extends Group {

  private static final Color BOUNDS_COLOR = Color.BLUE;

  private static final Color VERTEX_COLOR = Color.GREEN;

  private static final Color SELECTED_COLOR = Color.RED;

  private static final float GAP = 20f;

  private static final float GAP_DOUBLE = GAP * 2f;

  private static final int ACTION_NONE = -1;

  private static final int ACTION_MOVE_VERTEX = 0;

  private static final int ACTION_MOVE_BOUNDS = 1;

  private ShapeDrawer shapeDrawer;

  private final Vector2 actorLocation = new Vector2();

  private final AnimationActor actor;

  private final Polygon bounds;

  private final Vector2 mouse = new Vector2();

  private final Vector2 mouseDelta = new Vector2();

  private final Vector2 vertexBackup = new Vector2();

  private int action;

  private int selectedVertex = -1;

  AnimationEditPanel(AnimationActor actor, Polygon bounds) {
    Skeleton skeleton = new Skeleton(actor.getSkeleton());
    SkeletonData data = skeleton.getData();
    Rectangle actorBound = actor.getBound();
    actorLocation.set(GAP - actorBound.x, GAP - actorBound.y);
    this.actor = new AnimationActor(
        actor.getRenderer(), skeleton, new AnimationState(new AnimationStateData(data)),
        actorBound, actor.getBorder());
    this.actor.getAnimationState().setAnimation(0, actor.getName(), true); // actor name is the animation name
    this.actor.setPosition(actorLocation.x, actorLocation.y);
    addActor(this.actor);
    float[] verticesCopy;
    if (bounds == null) {
      verticesCopy = GeoUtils.copyVertices(actorBound); // copy actor's bound
    } else {
      verticesCopy = GeoUtils.copyVertices(bounds.getVertices());
    }
    this.bounds = new Polygon(verticesCopy);
    this.bounds.setPosition(actorLocation.x, actorLocation.y);
    setScale(4f);
    addListener(
        new InputListener() {

          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
              setMouse(x, y);
              selectedVertex = selectVertex();
              if (selectedVertex >= 0) {
                event.stop();
                action = ACTION_MOVE_VERTEX;
                return true;
              } else if (UIUtils.ctrl() && inBounds()) {
                event.stop();
                action = ACTION_MOVE_BOUNDS;
                return true;
              } else {
                action = ACTION_NONE;
              }
            }
            return false;
          }

          @Override
          public void touchDragged(InputEvent event, float x, float y, int pointer) {
            switch (action) {
              case ACTION_MOVE_VERTEX:
                setMouse(x, y);
                moveVertex();
                break;
              case ACTION_MOVE_BOUNDS:
                moveBounds();
                break;
              default:
                break;
            }
          }

          @Override
          public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
              if (action == ACTION_MOVE_VERTEX) {
                vertexMoved();
              }
              action = ACTION_NONE;
            }
          }

          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            switch (keycode) {
              case Input.Keys.DEL:
              case Input.Keys.FORWARD_DEL:
                removeVertex();
                return true;
            }
            return false;
          }
        }
    );
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    if (shapeDrawer == null) {
      shapeDrawer = new ShapeDrawer(batch, GraphicsUtils.onePix);
    }
    shapeDrawer.setColor(Color.WHITE);
    shapeDrawer.filledRectangle(0f, 0f, getWidth(), getHeight());
    applyTransform(batch, computeTransform());
    actor.draw(batch, parentAlpha); // culling is not correct, just draw actor
    shapeDrawer.setColor(BOUNDS_COLOR);
    shapeDrawer.polygon(bounds);
    float[] vertices = bounds.getVertices();
    float boundsX = bounds.getX();
    float boundsY = bounds.getY();
    shapeDrawer.setColor(VERTEX_COLOR);
    for (int i = 0; i < vertices.length; i += 2) {
      if (i == selectedVertex) {
        continue;
      }
      shapeDrawer.circle(vertices[i] + boundsX, vertices[i + 1] + boundsY, 2f);
    }
    if (selectedVertex >= 0) {
      shapeDrawer.setColor(SELECTED_COLOR);
      shapeDrawer.circle(vertices[selectedVertex] + boundsX, vertices[selectedVertex + 1] + boundsY, 2f);
    }
    resetTransform(batch);
  }

  @Override
  public void setScale(float scaleXY) {
    super.setScale(scaleXY);
    Rectangle bound = actor.getBound();
    setSize((bound.width + GAP_DOUBLE) * scaleXY,
        (bound.height + GAP_DOUBLE) * scaleXY);
  }

  /**
   * @return null: if animation's bounding box is default value
   */
  public float[] getBoundsVertices() {
    float[] vertices = bounds.getVertices();
    float[] newVertices = removeCollinearVertices(vertices);
    if (isDefaultBounds(newVertices)) {
      return null;
    }
    return vertices == newVertices ? GeoUtils.copyVertices(vertices) : newVertices;
  }

  public void zoomIn() {
    float scaleXY = getScaleX();
    if (scaleXY < 10f) {
      scaleXY = Math.min(10f, scaleXY + 1f);
      setScale(scaleXY);
    }
  }

  public void zoomOut() {
    float scaleXY = getScaleX();
    if (scaleXY > 1f) {
      scaleXY = Math.max(1f, scaleXY - 1f);
      setScale(scaleXY);
    }
  }

  public void resetBounds() {
    selectedVertex = -1;
    bounds.setVertices(GeoUtils.copyVertices(actor.getBound()));
  }

  /**
   * remove collinear vertex
   */
  public void optimize() {
    float[] vertices = bounds.getVertices();
    float[] newVertices = removeCollinearVertices(vertices);
    if (newVertices != vertices) {
      bounds.setVertices(newVertices);
      bounds.dirty();
    }
  }

  private void setMouse(float screenX, float screenY) {
    mouse.set(screenX, screenY).sub(actorLocation);
  }

  private int selectVertex() {
    float[] vertices = bounds.getVertices();
    int length = vertices.length;
    for (int i = 0; i < length; i += 2) { // in vertex
      if (mouse.dst2(vertices[i], vertices[i + 1]) <= 4f) {
        vertexBackup.set(vertices[i], vertices[i + 1]);
        return i;
      }
    }
    float startX = vertices[length - 2];
    float startY = vertices[length - 1];
    float endX;
    float endY;
    for (int i = 0; i < length; i += 2) {
      endX = vertices[i];
      endY = vertices[i + 1];
      if (Intersector.distanceSegmentPoint(startX, startY, endX, endY, mouse.x, mouse.y) <= 2f) {
        // in edge, add new vertex
        Intersector.nearestSegmentPoint(startX, startY, endX, endY, mouse.x, mouse.y, vertexBackup);
        float[] newVertices = new float[length + 2];
        System.arraycopy(vertices, 0, newVertices, 0, i);
        newVertices[i] = vertexBackup.x;
        newVertices[i + 1] = vertexBackup.y;
        System.arraycopy(vertices, i, newVertices, i + 2, length - i);
        bounds.setVertices(newVertices);
        return i;
      }
      startX = endX;
      startY = endY;
    }
    return -1;
  }

  private void removeVertex() {
    if (selectedVertex < 0) {
      return;
    }
    float[] vertices = bounds.getVertices();
    int length = vertices.length;
    if (length <= 6) {
      return;
    }
    length -= 2;
    float[] newVertices = new float[length];
    System.arraycopy(vertices, 0, newVertices, 0, selectedVertex);
    System.arraycopy(vertices, selectedVertex + 2, newVertices, selectedVertex, length - selectedVertex);
    bounds.setVertices(newVertices);
    bounds.dirty();
    if (selectedVertex == length) {
      selectedVertex = 0;
    }
  }

  private float[] removeCollinearVertices(float[] vertices) {
    int length = vertices.length;
    if (length == 6) {
      return vertices;
    }
    float startX = vertices[length - 2];
    float startY = vertices[length - 1];
    float x = vertices[0];
    float y = vertices[1];
    float endX;
    float endY;
    List<Integer> removes = null;
    for (int i = 0; i < length; i += 2) {
      endX = vertices[(i + 2) % length];
      endY = vertices[(i + 3) % length];
      if (Intersector.distanceSegmentPoint(startX, startY, endX, endY, x, y) < 0.0001f) {
        if (removes == null) {
          removes = new ArrayList<>();
        }
        removes.add(i);
      } else {
        startX = x;
        startY = y;
      }
      x = endX;
      y = endY;
    }
    if (removes == null) {
      return vertices;
    }
    int newLength = length - removes.size() * 2;
    if (newLength < 6) {
      return vertices;
    }
    float[] newVertices = new float[newLength];
    int index = 0;
    int selected = selectedVertex;
    for (int i = 0; i < length; i += 2) {
      if (removes.contains(i)) {
        if (i < selected) {
          selectedVertex -= 2;
        }
        continue;
      }
      newVertices[index++] = vertices[i];
      newVertices[index++] = vertices[i + 1];
    }
    return newVertices;
  }

  private boolean inBounds() {
    float[] vertices = bounds.getVertices();
    if (Intersector.isPointInPolygon(vertices, 0, vertices.length, mouse.x, mouse.y)) {
      mouseDelta.setZero();
      return true;
    }
    return false;
  }

  private void moveVertex() {
    float[] vertices = bounds.getVertices();
    vertices[selectedVertex] = mouse.x;
    vertices[selectedVertex + 1] = mouse.y;
    bounds.dirty();
  }

  private void moveBounds() {
    float scaleXY = getScaleX();
    float x = Gdx.input.getDeltaX() / scaleXY;
    float y = -Gdx.input.getDeltaY() / scaleXY;
    mouseDelta.add(x, y);
    moveBounds(x, y);
  }

  private void moveBounds(float x, float y) {
    float[] vertices = bounds.getVertices();
    for (int i = 0; i < vertices.length; i += 2) {
      vertices[i] += x;
      vertices[i + 1] += y;
    }
    bounds.dirty();
  }

  private void vertexMoved() {
    float[] vertices = bounds.getVertices();
    float x = vertices[selectedVertex];
    float y = vertices[selectedVertex + 1];
    for (int i = 0; i < vertices.length; i += 2) {
      if (i == selectedVertex) {
        continue;
      }
      if (Vector2.dst2(x, y, vertices[i], vertices[i + 1]) <= 4f) { // too near, revert
        vertices[selectedVertex] = vertexBackup.x;
        vertices[selectedVertex + 1] = vertexBackup.y;
        bounds.dirty();
        return;
      }
    }
  }

  private boolean isDefaultBounds(float[] vertices) {
    float[] defaultVertices = GeoUtils.copyVertices(actor.getBound());
    if (vertices.length == 8) {
      _out:
      for (int i = 0; i < 8; i += 2) {
        for (int j = 0; j < 8; ++j) {
          if (vertices[(i + j) % 8] != defaultVertices[j]) {
            continue _out;
          }
        }
        return true;
      }
    }
    return false;
  }
}
