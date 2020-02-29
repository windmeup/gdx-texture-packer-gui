package com.crashinvaders.texturepackergui.controllers.packing.processors.spine;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tools.spine.SkeletonSettings;
import com.badlogic.gdx.tools.spine.data.Animation;
import com.badlogic.gdx.tools.spine.data.AnimationAttachment;
import com.badlogic.gdx.tools.spine.data.AnimationSlot;
import com.badlogic.gdx.tools.spine.data.Bone;
import com.badlogic.gdx.tools.spine.data.Bound;
import com.badlogic.gdx.tools.spine.data.Skeleton;
import com.badlogic.gdx.tools.spine.data.SkeletonData;
import com.badlogic.gdx.tools.spine.data.Skin;
import com.badlogic.gdx.tools.spine.data.Slot;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.packing.processors.PackingProcessor;
import com.crashinvaders.texturepackergui.utils.JacksonUtils;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.esotericsoftware.spine.SkeletonJson;
import com.github.czyzby.kiwi.util.common.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExportSpineProcessor extends SpineProcessor {

  @Override
  public void processPackage(PackProcessingNode node) throws Exception {
    PackModel pack = node.getPack();
    String outputDir = pack.getOutputDir();
    String fileName = PackingProcessor.obtainFilename(pack);
    String jsonPath = outputDir + "/" + fileName + ".json";
    new FileHandle(jsonPath).delete();
    Map<String, Rect> rects = loadRects(pack);
    checkSettings(pack);
    String extension = pack.getSettings().atlasExtension;
    FileHandle outputDirFile = new FileHandle(outputDir);
    TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
        new FileHandle(outputDir + "/" + fileName + ((extension == null || extension.isEmpty()) ? "" : extension)),
        outputDirFile, false);
    JacksonUtils.writeValue(Paths.get(jsonPath).toFile(), toSkeletonData(pack, atlasData, rects));
  }

  public com.esotericsoftware.spine.SkeletonData getSkeletonPreview(PackModel pack, TextureAtlas.TextureAtlasData atlasData) throws IOException {
    Map<String, Rect> rects = loadRects(pack);
    checkSettings(pack);
    File tempFile = Files.createTempFile(pack.getName(), ".jsonTemp").toFile();
    JacksonUtils.writeValue(tempFile, toSkeletonData(pack, atlasData, rects));
    SkeletonJson skeletonJson = new SkeletonJson(new TextureAtlas(atlasData));
    com.esotericsoftware.spine.SkeletonData data = skeletonJson.readSkeletonData(new FileHandle(tempFile));
    if (!tempFile.delete()) {
      tempFile.deleteOnExit();
    }
    return data;
  }

  private Map<String, Rect> loadRects(PackModel pack) throws IOException {
    String fileName = PackingProcessor.obtainFilename(pack);
    String rtsPath = pack.getOutputDir() + "/" + fileName + ".rts";
    File rtsFile = Paths.get(rtsPath).toFile();
    if (!rtsFile.exists()) {
      throw new IllegalStateException("File is not exists: " + fileName + ".rts, repack the textures to create it.");
    }
    Map<String, Rect> rects = JacksonUtils.readValue(rtsFile, Rects.class).getRects();
    if (rects == null) {
      rects = Collections.emptyMap();
    }
    return rects;
  }

  private void checkSettings(PackModel pack) {
    SkeletonSettings skeletonSettings = pack.getSkeletonSettings();
    String slotName = skeletonSettings.getSlotName();
    if (slotName == null) {
      throw new IllegalStateException("Slot name is not set.");
    }
    slotName = slotName.trim();
    if (slotName.isEmpty()) {
      throw new IllegalStateException("Slot name is not set.");
    }
    float frameDuration = skeletonSettings.getDuration();
    if (frameDuration <= 0f) {
      throw new IllegalStateException("Duration must > 0");
    }
  }

  private SkeletonData toSkeletonData(
      PackModel pack, TextureAtlas.TextureAtlasData atlasData,
      Map<String, Rect> rects) throws IOException {
    SkeletonSettings skeletonSettings = pack.getSkeletonSettings();
    // skeleton
    Skeleton skeleton = new Skeleton();
    skeleton.setSpine("3.8.55");
    skeleton.setX(skeletonSettings.getX());
    skeleton.setY(skeletonSettings.getY());
    skeleton.setWidth(skeletonSettings.getWidth());
    skeleton.setHeight(skeletonSettings.getHeight());
    skeleton.setImages("./");
    // bone
    Bone root = new Bone();
    root.setName("root");
    List<Bone> bones = Collections.singletonList(root);
    // slot
    Slot body = new Slot();
    String slotName = skeletonSettings.getSlotName();
    body.setName(slotName);
    body.setBone("root");
    body.setAttachment(slotName);
    List<Slot> slots = Collections.singletonList(body);
    // skin
    Map<String, Bound> bodySkins = new TreeMap<>();
    for (TextureAtlas.TextureAtlasData.Region region : atlasData.getRegions()) {
      bodySkins.put(region.name,
          getBound(region, skeletonSettings, rects));
    }
    Map<String, Map<String, Bound>> attachments = new HashMap<>();
    attachments.put(slotName, bodySkins);
    Skin skin = new Skin();
    skin.setName("default");
    skin.setAttachments(attachments);
    List<Skin> skins = Collections.singletonList(skin);
    // animation
    Map<String, List<String>> actions = new HashMap<>();
    for (TextureAtlas.TextureAtlasData.Region region : atlasData.getRegions()) {
      addFrame(actions, region.name);
    }
    Map<String, Animation> animations = new TreeMap<>();
    float frameDuration = skeletonSettings.getDuration();
    for (Map.Entry<String, List<String>> entry : actions.entrySet()) {
      animations.put(entry.getKey(), toAnimation(entry.getValue(), slotName, frameDuration));
    }
    // data
    SkeletonData data = new SkeletonData();
    data.setSkeleton(skeleton);
    data.setBones(bones);
    data.setSlots(slots);
    data.setSkins(skins);
    data.setAnimations(animations);
    return data;
  }

  private Bound getBound(
      TextureAtlas.TextureAtlasData.Region region, SkeletonSettings settings,
      Map<String, Rect> rects) throws IOException {
    Rect rect = rects.get(region.name);
    if (rect == null) {
      throw new IOException("Region info not found, region name is " + region.name +
          ", repack the textures to fix it.");
    }
    int offsetY = rect.getOriginalHeight() - rect.getRegionHeight() - rect.getOffsetY(); // rect y coords down
    int regWidth = toEven(rect.getRegionWidth());
    int regHeight = toEven(rect.getRegionHeight());
    String dir = settings.getAnchorFilesDir();
    int anchorX;
    int anchorY;
    if (Strings.isEmpty(dir)) {
      anchorX = settings.getAnchorX();
      anchorY = settings.getAnchorY();
    } else {
      String[] splits = region.name.split("/");
      FileHandle handle = new FileHandle(dir + "/" + splits[splits.length - 1] + ".txt");
      if (handle.exists()) {
        BufferedReader reader = handle.reader(8);
        anchorX = Integer.parseInt(reader.readLine());
        anchorY = rect.getOriginalHeight() - 1 + Integer.parseInt(reader.readLine());
      } else {
        anchorX = settings.getAnchorX();
        anchorY = settings.getAnchorY();
      }
    }
    Bound bound = new Bound();
    bound.setX(rect.getOffsetX() - anchorX + regWidth / 2); // x,y is location of bound's center
    bound.setY(offsetY - anchorY + regHeight / 2);
    bound.setWidth(regWidth);
    bound.setHeight(regHeight);
    return bound;
  }

  private int toEven(int v) {
    return v % 2 == 0 ? v : v + 1;
  }

  private void addFrame(Map<String, List<String>> actions, String name) {
    String[] split = name.split("/");
    if (split.length < 2) {
      return;
    }
    StringBuilder builder = new StringBuilder(split[0]);
    for (int i = 1; i < split.length - 1; ++i) {
      builder.append("_");
      builder.append(split[i]);
    }
    String actionName = builder.toString();
    List<String> frames = actions.computeIfAbsent(actionName, k -> new ArrayList<>());
    frames.add(name);
  }

  private Animation toAnimation(List<String> frames, String slotName, float frameDuration) {
    frames.sort(String::compareTo);
    List<AnimationAttachment> attachment = new ArrayList<>();
    float duration = 0f;
    AnimationAttachment animationAttachment;
    for (String frame : frames) {
      animationAttachment = new AnimationAttachment();
      animationAttachment.setTime(duration);
      animationAttachment.setName(frame);
      attachment.add(animationAttachment);
      duration += frameDuration;
    }
    animationAttachment = new AnimationAttachment(); // end attachment
    animationAttachment.setTime(duration);
    animationAttachment.setName("");
    attachment.add(animationAttachment);
    AnimationSlot bodySlot = new AnimationSlot();
    bodySlot.setAttachment(attachment);
    Map<String, AnimationSlot> slots = new HashMap<>();
    slots.put(slotName, bodySlot);
    Animation animation = new Animation();
    animation.setSlots(slots);
    return animation;
  }
}
