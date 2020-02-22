package com.crashinvaders.texturepackergui.views.canvas.widgets.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.DefaultLmlParser;

import java.util.Locale;

public class AnimationInfoPanel extends Container<Actor> {

  private final Label lblZoom;

  public AnimationInfoPanel(LmlParser parser) {
    align(Align.top);
    fillX();

    // Workaround of parser's only single parsing operation limitation
    LmlParser localParser = new DefaultLmlParser(parser.getData());
    localParser.setSyntax(parser.getSyntax());
    Group root = (Group) (localParser.parseTemplate(Gdx.files.internal("lml/preview/animationInfoPanel.lml")).first());
    setActor(root);

    lblZoom = root.findActor("lblZoom");

    setZoomLevel(100f);
  }

  public void setZoomLevel(float zoom) {
    lblZoom.setText((String.format(Locale.US, "%.0f%%", zoom)));
  }
}
