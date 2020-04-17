package com.crashinvaders.texturepackergui.views.canvas.widgets.spine.editor;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.AnimationActor;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class EditAnimationDialog extends VisDialog {

  private final AnimationEditPanel editPanel;

  private final VisScrollPane scrollPane;

  public EditAnimationDialog(AnimationActor actor, Polygon bounds, Listener listener) {
    super("edit - " + actor.getName());
    addCloseButton();
    closeOnEscape();
    centerWindow();
    setSize(800f, 600f);
    editPanel = new AnimationEditPanel(actor, bounds);
    scrollPane = new VisScrollPane(editPanel);
    VisTextButton saveButton = new VisTextButton("Save");
    VisTextButton optimizeButton = new VisTextButton("Optimize");
    VisTextButton resetButton = new VisTextButton("Reset");
    VisTextButton zoomInButton = new VisTextButton("Zoom in");
    VisTextButton zoomOutButton = new VisTextButton("Zoom out");
    VisTable toolbarLeft = new VisTable();
    toolbarLeft.add(saveButton);
    toolbarLeft.add(resetButton);
    toolbarLeft.add(optimizeButton);
    VisTable toolbarRight = new VisTable();
    toolbarRight.add(zoomInButton);
    toolbarRight.add(zoomOutButton);
    Table contentTable = getContentTable();
    contentTable.add(toolbarLeft).left();
    contentTable.add(toolbarRight).right();
    contentTable.row();
    contentTable.add(scrollPane).colspan(2).expand();
    saveButton.addListener(
        new ButtonListener() {
          @Override
          void changed() {
            listener.saved(editPanel.getBoundsVertices());
          }
        }
    );
    resetButton.addListener(
        new ButtonListener() {
          @Override
          public void changed() {
            editPanel.resetBounds();
          }
        }
    );
    optimizeButton.addListener(
        new ButtonListener() {
          @Override
          void changed() {
            editPanel.optimize();
          }
        }
    );
    zoomInButton.addListener(
        new ButtonListener() {
          @Override
          public void changed() {
            editPanel.zoomIn();
            scrollPane.invalidateHierarchy();
          }
        }
    );
    zoomOutButton.addListener(
        new ButtonListener() {
          @Override
          public void changed() {
            editPanel.zoomOut();
            scrollPane.invalidateHierarchy();
          }
        }
    );
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    if (stage != null) {
      stage.setScrollFocus(scrollPane);
      setKeyboardFocus();
    }
  }

  private void setKeyboardFocus() {
    getStage().setKeyboardFocus(editPanel);
  }

  private abstract class ButtonListener extends ChangeListener {

    @Override
    public void changed(ChangeEvent event, Actor actor) {
      changed();
      setKeyboardFocus();
    }

    abstract void changed();
  }

  public interface Listener {

    void saved(float[] boundsVertices);
  }
}
