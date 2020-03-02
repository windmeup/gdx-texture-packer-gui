package com.crashinvaders.texturepackergui.controllers.main;

import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;

public class SkeletonSettingsActors {

  @LmlActor("edtSlotName")
  VisTextField edtSlotName;

  @LmlActor("skbSkeletonX")
  VisValidatableTextField skbSkeletonX;

  @LmlActor("skbSkeletonY")
  VisValidatableTextField skbSkeletonY;

  @LmlActor("skbSkeletonWidth")
  VisValidatableTextField skbSkeletonWidth;

  @LmlActor("skbSkeletonHeight")
  VisValidatableTextField skbSkeletonHeight;

  @LmlActor("skbAnchorX")
  VisValidatableTextField skbAnchorX;

  @LmlActor("skbAnchorY")
  VisValidatableTextField skbAnchorY;

  @LmlActor("edtAnchorFilesDir")
  VisTextField edtAnchorFilesDir;

  @LmlActor("skbDuration")
  VisValidatableTextField skbDuration;
}
