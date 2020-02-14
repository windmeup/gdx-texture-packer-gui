package com.badlogic.gdx.tools.spine.data;

import com.badlogic.gdx.utils.Array;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SpineData {

  private Skeleton skeleton;

  private Array<Bone> bones;

  private Array<Slot> slots;

  private Array<Skin> skins;

  private Map<String, Animation> animations;
}
