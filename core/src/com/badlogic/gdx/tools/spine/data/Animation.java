package com.badlogic.gdx.tools.spine.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Animation {

  private Map<String, AnimationSlot> slots;
}
