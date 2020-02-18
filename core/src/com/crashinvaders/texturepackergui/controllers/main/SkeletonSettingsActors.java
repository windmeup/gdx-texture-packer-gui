package com.crashinvaders.texturepackergui.controllers.main;

import com.crashinvaders.texturepackergui.views.seekbar.SeekBar;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisTextField;

public class SkeletonSettingsActors {

  @LmlActor("edtSlotName")
  VisTextField edtSlotName;

  @LmlActor("skbSkeletonX")
  SeekBar skbSkeletonX;

  @LmlActor("skbSkeletonY")
  SeekBar skbSkeletonY;

  @LmlActor("skbSkeletonWidth")
  SeekBar skbSkeletonWidth;

  @LmlActor("skbSkeletonHeight")
  SeekBar skbSkeletonHeight;

  @LmlActor("skbAnchorX")
  SeekBar skbAnchorX;

  @LmlActor("skbAnchorY")
  SeekBar skbAnchorY;

  @LmlActor("edtAnchorFilesDir")
  VisTextField edtAnchorFilesDir;

  @LmlActor("skbDuration")
  SeekBar skbDuration;
}
