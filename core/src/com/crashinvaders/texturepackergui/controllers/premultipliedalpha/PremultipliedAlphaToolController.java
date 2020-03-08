package com.crashinvaders.texturepackergui.controllers.premultipliedalpha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.utils.AppIconProvider;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileFilter;
import java.io.IOException;

@ViewDialog(
    id = "dialog_premultiplied_alpha",
    value = "lml/premultipliedalpha/dialogPremultipliedAlphaTool.lml")
public class PremultipliedAlphaToolController implements ActionContainer {

  @Inject
  InterfaceService interfaceService;
  @Inject
  ErrorDialogController errorDialogController;

  @LmlActor("edtInputDir")
  VisTextField edtInputDir;

  @LmlActor("edtOutputDir")
  VisTextField edtOutputDir;

  @ViewStage
  Stage stage;

  @LmlAction("pickPMAInputDir")
  void pickPMAInputDir() {
    chooseDir(edtInputDir);
  }

  @LmlAction("pickPMAOutputDir")
  void pickPMAOutputDir() {
    chooseDir(edtOutputDir);
  }

  @LmlAction("launchPMAProcess")
  void launchPMAProcess() {
    try {
      FileHandle outputDir = FileUtils.obtainIfExists(edtOutputDir.getText());
      if (outputDir == null)
        throw new IllegalStateException("Output directory does not exist: " + edtOutputDir.getText());
      FileHandle inputDir = FileUtils.obtainIfExists(edtInputDir.getText());
      if (inputDir == null) throw new IllegalStateException("Image directory does not exist: " + edtInputDir.getText());
      String outputPath = outputDir.file().getAbsolutePath();
      String inputPath = inputDir.file().getAbsolutePath();
      if (outputPath.startsWith(inputPath) || inputPath.startsWith(outputPath)) {
        throw new IllegalStateException("Directories same or someone contains the other one.");
      }
      FileFilter imageFilter = new SuffixFileFilter(new String[]{".png"}, IOCase.INSENSITIVE);
      process(inputDir, imageFilter, outputDir, inputPath.length());
      showSuccessfulDialog(outputDir);
    } catch (Exception e) {
      showErrorDialog(e);
    }
  }

  private void chooseDir(VisTextField textField) {
    FileHandle dir = FileUtils.obtainIfExists(textField.getText());
    FileChooser fileChooser = new FileChooser(dir, FileChooser.Mode.OPEN);
    fileChooser.setIconProvider(new AppIconProvider(fileChooser));
    fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
    fileChooser.setListener(new FileChooserAdapter() {
      @Override
      public void selected(Array<FileHandle> file) {
        FileHandle chosenFile = file.first();
        textField.setText(chosenFile.path());
      }
    });
    stage.addActor(fileChooser.fadeIn());
  }

  private void process(
      FileHandle sourceDir, FileFilter imageFilter,
      FileHandle outputDir, int relativePathIndex) throws IOException {
    BufferedImage src;
    int width;
    int height;
    BufferedImage dst;
    FileHandle outputImage;
    for (FileHandle image : sourceDir.list(imageFilter)) {
      src = ImageIO.read(image.file());
      width = src.getWidth();
      height = src.getHeight();
      dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      TexturePacker.copy(src, 0, 0, width, height, dst, 0, 0, false);
      dst.getColorModel().coerceData(dst.getRaster(), true);
      outputImage = outputDir.child(image.file().getAbsolutePath().substring(relativePathIndex));
      outputImage.mkdirs();
      ImageIO.write(dst, "png", outputImage.file());
    }
    for (FileHandle subDir : sourceDir.list((FileFilter) DirectoryFileFilter.DIRECTORY)) {
      process(subDir, imageFilter, outputDir, relativePathIndex);
    }
  }

  private void showSuccessfulDialog(final FileHandle outputDir) {
    VisDialog dialog = (VisDialog) interfaceService.getParser().parseTemplate(Gdx.files.internal("lml/textureunpacker/dialogSuccess.lml")).first();
    dialog.findActor("btnOpenOutputDir").addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        try {
          Desktop.getDesktop().open(outputDir.file());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    dialog.show(stage);
    stage.setScrollFocus(dialog);
  }

  private void showErrorDialog(Exception e) {
    errorDialogController.setError(e);
    interfaceService.showDialog(errorDialogController.getClass());
  }
}
