package com.crashinvaders.texturepackergui.controllers.packing.processors.spine;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.tools.spine.Point;
import com.badlogic.gdx.tools.spine.SkeletonSettings;
import com.badlogic.gdx.tools.spine.data.*;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.packing.processors.PackingProcessor;
import com.crashinvaders.texturepackergui.utils.GeoUtils;
import com.crashinvaders.texturepackergui.utils.JacksonUtils;
import com.crashinvaders.texturepackergui.utils.SpineUtils;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExportSpineProcessor extends SpineProcessor {

  private static final String ANIMATION_BOUNDING = "aniBounding";

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
    Slot slot = new Slot();
    String slotName = skeletonSettings.getSlotName();
    slot.setName(slotName);
    slot.setBone("root");
    slot.setAttachment(slotName);
    Slot aniBoundingSlot = new Slot();
    aniBoundingSlot.setName(ANIMATION_BOUNDING);
    aniBoundingSlot.setBone("root");
    aniBoundingSlot.setAttachment(ANIMATION_BOUNDING);
    List<Slot> slots = new ArrayList<>();
    slots.add(slot);
    slots.add(aniBoundingSlot);
    // skin
    Map<String, Region> slotAttachments = new TreeMap<>();
    Map<String, Map<String, ? extends Attachment>> attachments = new HashMap<>();
    attachments.put(slotName, slotAttachments);
    Map<String, BoundingBox> aniBoundingSlotAttachments = new TreeMap<>();
    attachments.put(ANIMATION_BOUNDING, aniBoundingSlotAttachments);
    Skin skin = new Skin();
    skin.setName("default");
    skin.setAttachments(attachments);
    List<Skin> skins = Collections.singletonList(skin);
    // animation
    Map<String, List<TextureAtlas.TextureAtlasData.Region>> animationRegions = new HashMap<>();
    for (TextureAtlas.TextureAtlasData.Region region : atlasData.getRegions()) {
      addRegion(animationRegions, region);
    }
    Map<String, Animation> animations = new TreeMap<>();
    float frameDuration = skeletonSettings.getDuration();
    String animationName;
    List<TextureAtlas.TextureAtlasData.Region> regions;
    for (Map.Entry<String, List<TextureAtlas.TextureAtlasData.Region>> entry : animationRegions.entrySet()) {
      animationName = entry.getKey();
      regions = entry.getValue();
      animations.put(animationName, toAnimation(animationName, regions, slotName, frameDuration));
      for (TextureAtlas.TextureAtlasData.Region region : regions) {
        slotAttachments.put(region.name, getRegionAttachment(animationName, region, skeletonSettings, rects));
      }
      aniBoundingSlotAttachments.put(animationName,
          getBoundingBoxAttachment(animationName, regions, slotAttachments, skeletonSettings));
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

  private Region getRegionAttachment(
      String animationName,
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
    int anchorX = settings.getAnchorX();
    int anchorY = settings.getAnchorY();
    Point animationOffset = settings.getAnimationOffsets().get(animationName);
    if (animationOffset != null) {
      anchorX -= animationOffset.getX();
      anchorY -= animationOffset.getY();
    }
    if (!Strings.isEmpty(dir)) {
      String[] splits = region.name.split("/");
      FileHandle handle = new FileHandle(dir + "/" + splits[splits.length - 1] + ".txt");
      if (handle.exists()) {
        BufferedReader reader = handle.reader(8);
        anchorX -= Integer.parseInt(reader.readLine());
        anchorY += rect.getOriginalHeight() - 1 + Integer.parseInt(reader.readLine());
      }
    }
    Region regionAttachment = new Region();
    regionAttachment.setX(rect.getOffsetX() - anchorX + regWidth / 2); // x,y is location of bound's center
    regionAttachment.setY(offsetY - anchorY + regHeight / 2);
    regionAttachment.setWidth(regWidth);
    regionAttachment.setHeight(regHeight);
    return regionAttachment;
  }

  private BoundingBox getBoundingBoxAttachment(
      String animationName,
      List<TextureAtlas.TextureAtlasData.Region> regions,
      Map<String, Region> slotAttachments,
      SkeletonSettings skeletonSettings) {
    BoundingBox boundingBox = new BoundingBox();
    Polygon bounding = skeletonSettings.getAnimationBounds().get(animationName);
    float[] vertices;
    if (bounding == null) {
      Rectangle boundingRect = new Rectangle(-5f, -5f, 10f, 10f); //  // origin always in bound
      for (TextureAtlas.TextureAtlasData.Region atlasRegion : regions) {
        boundingRect.merge(SpineUtils.getBound(slotAttachments.get(atlasRegion.name)));
      }
      boundingRect.merge(new Rectangle(skeletonSettings.getX(), skeletonSettings.getY(),
          skeletonSettings.getWidth(), skeletonSettings.getHeight()));
      vertices = GeoUtils.copyVertices(boundingRect);
    } else {
      vertices = bounding.getVertices();
    }
    boundingBox.setVertexCount(vertices.length / 2);
    boundingBox.setVertices(vertices);
    return boundingBox;
  }

  private int toEven(int v) {
    return v % 2 == 0 ? v : v + 1;
  }

  private void addRegion(Map<String, List<TextureAtlas.TextureAtlasData.Region>> animationRegions, TextureAtlas.TextureAtlasData.Region region) {
    String name = region.name;
    String[] split = name.split("/");
    if (split.length < 2) {
      return;
    }
    StringBuilder builder = new StringBuilder(split[0]);
    for (int i = 1; i < split.length - 1; ++i) {
      builder.append("_");
      builder.append(split[i]);
    }
    String animationName = builder.toString();
    List<TextureAtlas.TextureAtlasData.Region> regions = animationRegions.computeIfAbsent(animationName, k -> new ArrayList<>());
    regions.add(region);
  }

  private Animation toAnimation(String animationName, List<TextureAtlas.TextureAtlasData.Region> regions, String slotName, float frameDuration) {
    // regions
    regions.sort(Comparator.comparing(r -> r.name));
    List<AnimationAttachment> attachment = new ArrayList<>();
    float duration = 0f;
    AnimationAttachment animationAttachment;
    for (TextureAtlas.TextureAtlasData.Region region : regions) {
      animationAttachment = new AnimationAttachment();
      animationAttachment.setTime(duration);
      animationAttachment.setName(region.name);
      attachment.add(animationAttachment);
      duration += frameDuration;
    }
    animationAttachment = new AnimationAttachment(); // end attachment
    animationAttachment.setTime(duration);
    animationAttachment.setName("");
    attachment.add(animationAttachment);
    AnimationSlot slot = new AnimationSlot();
    slot.setAttachment(attachment);
    Map<String, AnimationSlot> slots = new HashMap<>();
    slots.put(slotName, slot);
    // bounding boxes
    attachment = new ArrayList<>();
    animationAttachment = new AnimationAttachment();
    animationAttachment.setName(animationName);
    attachment.add(animationAttachment);
    animationAttachment = new AnimationAttachment(); // end attachment
    animationAttachment.setTime(duration);
    animationAttachment.setName("");
    attachment.add(animationAttachment);
    slot = new AnimationSlot();
    slot.setAttachment(attachment);
    slots.put(ANIMATION_BOUNDING, slot);
    Animation animation = new Animation();
    animation.setSlots(slots);
    return animation;
  }
}
