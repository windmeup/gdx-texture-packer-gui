package com.crashinvaders.texturepackergui.controllers.packing.processors.spine;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.PageFileWriter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.packing.processors.PackingProcessor;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.FileFilter;
import java.util.Comparator;

public class SpinePackingProcessor extends SpineProcessor {

  @Override
  public void processPackage(PackProcessingNode node) {
    PageFileWriter pageFileWriter = node.getPageFileWriter();
    if (pageFileWriter == null) {
      throw new IllegalStateException("PageFileWriter is not set. Looks like something is wrong with file type processor setup.");
    }
    PackModel pack = node.getPack();
    String fileName = PackingProcessor.obtainFilename(pack);
    FileFilter oldFileFilter =
        new RegexFileFilter("^" + fileName.replaceAll("\\.", "\\.") + "(\\d*)?(\\.([a-z0-9]*))?$", IOCase.INSENSITIVE);
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
    TexturePacker.Settings settings = toSpineSettings(pack.getSettings());
    TexturePacker texturePacker = new TexturePacker(settings, pageFileWriter);
    for (PackingProcessor.ImageEntry entry : imageEntries) {
      if (!entry.ninePatch) {
        texturePacker.addImage(entry.fileHandle.file(), entry.name);
      }
    }
    texturePacker.pack(outputDirFile.file(), fileName);
  }
}
