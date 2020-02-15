package com.badlogic.gdx.tools.spine;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.tools.texturepacker.PngPageFileWriter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.FileFilter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FrameSequence2Spine {

  private static final float FRAME_DURATION = 0.125f;

  public FrameSequence2Spine() {
  }

  public void toSpine(String imageDir, String outputDir, int centerX, int centerY) throws IOException {
    FileHandle outputDirFile = new FileHandle(outputDir);
    FileHandle imageDirFile = new FileHandle(imageDir);
    String atlasName = imageDirFile.name();
    FileFilter oldFileFilter = new RegexFileFilter("^" + atlasName + "(\\d*)?\\.([\\s,\\S]*)$");
    for (FileHandle old : outputDirFile.list(oldFileFilter)) {
      old.delete();
    }
    TexturePacker.Settings settings = new TexturePacker.Settings();
    settings.alias = false;
    settings.alphaThreshold = 0;
    settings.debug = false;
    settings.duplicatePadding = false;
    settings.edgePadding = true;
    settings.fast = false;
    settings.filterMag = Texture.TextureFilter.Linear;
    settings.filterMin = Texture.TextureFilter.Linear;
    settings.ignoreBlankImages = true;
    settings.maxHeight = 2048;
    settings.maxWidth = 2048;
    settings.minHeight = 16;
    settings.minWidth = 16;
    settings.paddingX = 1;
    settings.paddingY = 1;
    settings.pot = true;
    settings.multipleOfFour = false;
    settings.rotation = false;
    settings.shrinkSize = true;
    settings.evenSize = true;
    settings.stripWhitespaceX = true;
    settings.stripWhitespaceY = true;
    settings.wrapX = Texture.TextureWrap.ClampToEdge;
    settings.wrapY = Texture.TextureWrap.ClampToEdge;
    settings.premultiplyAlpha = false;
    settings.grid = false;
    settings.square = false;
    settings.bleed = true;
    settings.limitMemory = true;
    settings.useIndexes = false;
    TexturePacker texturePacker = new TexturePacker(settings, new PngPageFileWriter());
    FileFilter suffixFileFilter = new SuffixFileFilter(new String[]{".png", ".jpg", "jpeg"});
    addImage(texturePacker, imageDirFile, "", suffixFileFilter);
    texturePacker.pack(outputDirFile.file(), atlasName);
    toSpine(imageDir, outputDir, centerX, centerY, atlasName, texturePacker.getImageProcessor());
  }

  private void addImage(TexturePacker texturePacker, FileHandle file, String prefix, FileFilter suffixFileFilter) {
    FileHandle[] images = file.list(suffixFileFilter);
    Arrays.sort(images, Comparator.comparing(FileHandle::name));
    for (FileHandle image : images) {
      texturePacker.addImage(image.file(), prefix + image.nameWithoutExtension());
    }
    FileHandle[] subDirs = file.list((FileFilter) DirectoryFileFilter.DIRECTORY);
    Arrays.sort(subDirs, Comparator.comparing(FileHandle::name));
    for (FileHandle subDir : subDirs) {
      addImage(texturePacker, subDir, prefix + subDir.name() + "/", suffixFileFilter);
    }
  }

  private void toSpine(
      String imageDir, String outputDir, int centerX, int centerY,
      String atlasName, ImageProcessor imageProcessor) throws IOException {
    String atlasFile = outputDir + "/" + atlasName + ".atlas";
    String outputFile = outputDir + "/" + atlasName + ".json";
    TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(
        new FileHandle(atlasFile), new FileHandle(outputDir), false);
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
    Map<String, Bound> bodySkins = new TreeMap<>();
    for (TextureAtlas.TextureAtlasData.Region region : atlas.getRegions()) {
      bodySkins.put(region.name, getBound(region, imageProcessor, centerX, centerY, imageDir));
    }
    imageProcessor.clear();
    Map<String, Map<String, Bound>> attachments = new HashMap<>();
    attachments.put("body", bodySkins);
    Skin skin = new Skin();
    skin.setName("default");
    skin.setAttachments(attachments);
    Array<Skin> skins = Array.with(skin);
    // animation
    Map<String, List<String>> actions = new HashMap<>();
    for (TextureAtlas.TextureAtlasData.Region region : atlas.getRegions()) {
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
    Writer writer = new FileHandle(outputFile).writer(false, "UTF-8");
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
      int centerX, int centerY, String oriImageDir) {
    TexturePacker.Rect rect = imageProcessor.addImage(Paths.get(oriImageDir, region.name + ".png").toFile(), null);
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


  public static void main(String[] args) throws IOException {
    new FrameSequence2Spine().toSpine(
        "E:/falling resource/character/role_0", "E:/falling resource/character/packed",
        245, 108);
  }
}
