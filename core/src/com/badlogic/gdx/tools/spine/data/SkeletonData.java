package com.badlogic.gdx.tools.spine.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SkeletonData {

  private Skeleton skeleton;

  private List<Bone> bones;

  private List<Slot> slots;

  private List<Skin> skins;

  private Map<String, Animation> animations;
}
