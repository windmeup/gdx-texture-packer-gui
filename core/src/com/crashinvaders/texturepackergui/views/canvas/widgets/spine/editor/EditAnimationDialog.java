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

  private final VisScrollPane scrollPane;

  public EditAnimationDialog(AnimationActor actor, Polygon bounds) {
    super("edit - " + actor.getName());
    addCloseButton();
    closeOnEscape();
    centerWindow();
    AnimationEditPanel editPanel = new AnimationEditPanel(actor, bounds);
    VisTextButton saveButton = new VisTextButton("Save");
    VisTextButton zoomInButton = new VisTextButton("Zoom in");
    VisTextButton zoomOutButton = new VisTextButton("Zoom out");
    VisTable toolbarLeft = new VisTable();
    toolbarLeft.add(saveButton);
    VisTable toolbarRight = new VisTable();
    toolbarRight.add(zoomInButton);
    toolbarRight.add(zoomOutButton);
    scrollPane = new VisScrollPane(editPanel);
    Table contentTable = getContentTable();
    contentTable.add(toolbarLeft).left();
    contentTable.add(toolbarRight).right();
    contentTable.row();
    contentTable.add(scrollPane).colspan(2).expand();
    setSize(800f, 600f);
    zoomInButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            editPanel.zoomIn();
            scrollPane.invalidateHierarchy();
          }
        }
    );
    zoomOutButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
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
    }
  }
}