package com.crashinvaders.texturepackergui.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JacksonUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static void writeValue(File file, Object o) throws IOException {
    objectMapper.writeValue(file, o);
  }

  public static <T> T readValue(File file, Class<T> type) throws IOException {
    return objectMapper.readValue(file, type);
  }
}
