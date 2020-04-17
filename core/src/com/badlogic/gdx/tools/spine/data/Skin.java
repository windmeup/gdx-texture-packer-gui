package com.badlogic.gdx.tools.spine.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class Skin {

  private String name;

  private Map<String, Map<String, ? extends Attachment>> attachments;
}
