package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tools.spine.data.Animation;
import com.badlogic.gdx.tools.spine.data.AnimationAttachment;
import com.badlogic.gdx.tools.spine.data.AnimationSlot;
import com.badlogic.gdx.tools.spine.data.Bone;
import com.badlogic.gdx.tools.spine.data.Bound;
import com.badlogic.gdx.tools.spine.data.Skeleton;
import com.badlogic.gdx.tools.spine.data.Skin;
import com.badlogic.gdx.tools.spine.data.Slot;
import com.badlogic.gdx.tools.spine.data.SpineData;
import com.badlogic.gdx.tools.texturepacker.ImageProcessor;
import com.badlogic.gdx.tools.texturepacker.PageFileWriter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.FileFilter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Frames2SpineProcessor implements PackProcessor {

  private static final float FRAME_DURATION = 0.125f; // TODO config

  private static final int centerX = 245; // TODO config

  private static final int centerY = 108; // TODO config

  @Override
  public void processPackage(PackProcessingNode node) throws Exception {
    PageFileWriter pageFileWriter = node.getPageFileWriter();
    if (pageFileWriter == null) {
      throw new IllegalStateException("PageFileWriter is not set. Looks like something is wrong with file type processor setup.");
    }
    PackModel pack = node.getPack();
    String fileName = pack.getFilename().trim();
    if (fileName.isEmpty()) {
      fileName = pack.getName();
    }
    String atlasName;
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex < 0) {
      atlasName = fileName;
      fileName = atlasName + ".atlas";
    } else {
      atlasName = fileName.substring(0, dotIndex);
    }
    FileFilter oldFileFilter =
        new RegexFileFilter("^" + atlasName.replaceAll("\\.", "\\.") + "(\\d*)?\\.([\\s,\\S]*)$");
    String outputDir = pack.getOutputDir();
    FileHandle outputDirFile = new FileHandle(outputDir);
    for (FileHandle oldFile : outputDirFile.list(oldFileFilter)) {
      oldFile.delete();
    }
    Array<PackingProcessor.ImageEntry> imageEntries = PackingProcessor.collectImageFiles(pack);
    if (imageEntries.size == 0) {
      throw new IllegalStateException("No images to pack");
    }
    imageEntries.sort(Comparator.comparing(e -> e.regionName));
    TexturePacker.Settings settings = new TexturePacker.Settings(pack.getSettings());
    settings.stripWhitespaceX = true;
    settings.stripWhitespaceY = true;
    settings.shrinkSize = true;
    settings.evenSize = true;
    settings.edgePadding = true;
    settings.useIndexes = false;
    settings.scale = new float[]{1};
    settings.paddingX = Math.max(settings.paddingX, 1);
    settings.paddingY = Math.max(settings.paddingY, 1);
    TexturePacker texturePacker = new TexturePacker(settings, pageFileWriter);
    for (PackingProcessor.ImageEntry entry : imageEntries) {
      if (!entry.ninePatch) {
        texturePacker.addImage(entry.fileHandle.file(), entry.name);
      }
    }
    texturePacker.pack(outputDirFile.file(), fileName);
    TextureAtlas.TextureAtlasData atlasData = new TextureAtlas.TextureAtlasData(
        new FileHandle(outputDir + "/" + fileName), outputDirFile, false);
    // skeleton
    Skeleton skeleton = new Skeleton();
    skeleton.setSpine("3.8.55");
    skeleton.setWidth(500f); // TODO AABB
    skeleton.setHeight(400f);
    skeleton.setImages("./");
    // bone
    Bone root = new Bone();
    root.setName("root");
    Array<Bone> bones = Array.with(root);
    // slot
    Slot body = new Slot();
    body.setName("body");
    body.setBone("root");
    body.setAttachment("body");
    Array<Slot> slots = Array.with(body);
    // skin
    Map<String, PackingProcessor.ImageEntry> entryMap = new HashMap<>();
    for (PackingProcessor.ImageEntry entry : imageEntries) {
      entryMap.put(entry.regionName, entry);
    }
    ImageProcessor imageProcessor = texturePacker.getImageProcessor();
    Map<String, Bound> bodySkins = new TreeMap<>();
    for (TextureAtlas.TextureAtlasData.Region region : atlasData.getRegions()) {
      bodySkins.put(region.name, getBound(region, imageProcessor, entryMap));
    }
    Map<String, Map<String, Bound>> attachments = new HashMap<>();
    attachments.put("body", bodySkins);
    Skin skin = new Skin();
    skin.setName("default");
    skin.setAttachments(attachments);
    Array<Skin> skins = Array.with(skin);
    // animation
    Map<String, List<String>> actions = new HashMap<>();
    for (TextureAtlas.TextureAtlasData.Region region : atlasData.getRegions()) {
      addFrame(actions, region.name);
    }
    Map<String, Animation> animations = new HashMap<>();
    for (Map.Entry<String, List<String>> entry : actions.entrySet()) {
      animations.put(entry.getKey(), toAnimation(entry.getValue()));
    }
    // data
    SpineData data = new SpineData();
    data.setSkeleton(skeleton);
    data.setBones(bones);
    data.setSlots(slots);
    data.setSkins(skins);
    data.setAnimations(animations);
    Json json = new Json(JsonWriter.OutputType.json);
    String rawJson = json.prettyPrint(data);
    String[] lines = rawJson.split("\\n");
    Writer writer = new FileHandle(outputDir + "/" + atlasName + ".json").writer(false, "UTF-8");
    boolean first = true;
    for (String line : lines) {
      if (!line.contains("\"class\":")) { // remove class
        if (first) {
          first = false;
        } else {
          writer.append('\n');
        }
        writer.write(line);
      }
    }
    writer.flush();
    writer.close();
  }

  private Bound getBound(
      TextureAtlas.TextureAtlasData.Region region, ImageProcessor imageProcessor,
      Map<String, PackingProcessor.ImageEntry> imageEntries) {
    TexturePacker.Rect rect = imageProcessor.addImage(imageEntries.get(region.name).fileHandle.file(), null);
    imageProcessor.clear();
    int offsetY = (rect.originalHeight - rect.regionHeight - rect.offsetY); // rect y coords down
    int regWidth = toEven(rect.regionWidth);
    int regHeight = toEven(rect.regionHeight);
    Bound bound = new Bound();
    bound.setX(rect.offsetX - centerX + regWidth / 2); // x,y is location of bound's center
    bound.setY(offsetY - centerY + regHeight / 2);
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

  private Animation toAnimation(List<String> frames) {
    frames.sort(String::compareTo);
    Array<AnimationAttachment> attachment = new Array<>();
    float duration = 0f;
    AnimationAttachment animationAttachment;
    for (String frame : frames) {
      animationAttachment = new AnimationAttachment();
      animationAttachment.setTime(duration);
      animationAttachment.setName(frame);
      attachment.add(animationAttachment);
      duration += FRAME_DURATION;
    }
    animationAttachment = new AnimationAttachment(); // end attachment
    animationAttachment.setTime(duration);
    animationAttachment.setName("");
    attachment.add(animationAttachment);
    AnimationSlot bodySlot = new AnimationSlot();
    bodySlot.setAttachment(attachment);
    Map<String, AnimationSlot> slots = new HashMap<>();
    slots.put("body", bodySlot);
    Animation animation = new Animation();
    animation.setSlots(slots);
    return animation;
  }
}