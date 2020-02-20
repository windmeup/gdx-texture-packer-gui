
package com.crashinvaders.texturepackergui.controllers.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.spine.SkeletonSettings;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.common.statehash.StateHashable;
import com.crashinvaders.texturepackergui.controllers.packing.processors.PackingProcessor;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent.Property;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.kiwi.util.common.Strings;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class PackModel implements StateHashable {
    private static final String TAG = PackModel.class.getSimpleName();

    private final Array<ScaleFactorModel> scaleFactors = new Array<>();
    private final Array<InputFile> inputFiles = new Array<>();
    private final Settings settings = new Settings();
    private final SkeletonSettings skeletonSettings = new SkeletonSettings();
    private String name = "";
    private String filename = "";
    private String outputDir = "";

    private EventDispatcher eventDispatcher;

    public PackModel() {
        scaleFactors.add(new ScaleFactorModel("", 1f, TexturePacker.Resampling.bicubic));
    }

    public PackModel(PackModel pack) {
        settings.set(pack.settings);
        skeletonSettings.set(pack.skeletonSettings);

        this.name = pack.name;
        this.filename = pack.filename;
        this.outputDir = pack.outputDir;

        scaleFactors.addAll(pack.scaleFactors);

        inputFiles.addAll(pack.inputFiles);
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;

        for (InputFile inputFile : inputFiles) {
            inputFile.setEventDispatcher(eventDispatcher);
        }
    }

    public void setName(String name) {
        if (Strings.equals(this.name, name)) return;

        this.name = name;
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.NAME));
        }
    }

    public void setFilename(String filename) {
        if (Strings.equals(this.filename, filename)) return;

        this.filename = filename;
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.FILENAME));
        }
    }

    public void setOutputDir(String outputDir) {
        if (Strings.equals(this.outputDir, outputDir)) return;

        this.outputDir = outputDir;
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.OUTPUT));
        }
    }

    public void setAnchorFilesDir(String dir) {
        if (Strings.equals(skeletonSettings.getAnchorFilesDir(), dir)) {
            return;
        }
        skeletonSettings.setAnchorFilesDir(dir);
        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.ANCHOR_FILES));
        }
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public Array<InputFile> getInputFiles() {
        return inputFiles;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings.set(settings);
    }

    public SkeletonSettings getSkeletonSettings() {
        return skeletonSettings;
    }

    public void setSkeletonSettings(SkeletonSettings settings) {
        skeletonSettings.set(settings);
    }

    public String getCanonicalName() {
        return name.trim().isEmpty() ? "unnamed" : name;
    }

    public String getCanonicalFilename() {
        if (!filename.trim().isEmpty()) {
            String name = filename;
            String extension = "";
            int dotIndex = filename.lastIndexOf(".");
            if (dotIndex != -1 && dotIndex != filename.length()-1) {
                name = filename.substring(0, dotIndex);
                extension = filename.substring(dotIndex, filename.length());
            }
            return name + scaleFactors.first().getSuffix() + extension;
        } else {
            return getCanonicalName() + scaleFactors.first().getSuffix() + settings.atlasExtension;
        }
    }

    /**
     * @return may be null
     */
    public String getAtlasPath() {
        String atlasPath = null;
        if (outputDir != null && !outputDir.trim().isEmpty()) {
            String filename = getCanonicalFilename();
            atlasPath = outputDir + File.separator + filename;
        }
        return atlasPath;
    }

    public String getSkeletonPath() {
        if (outputDir != null && !outputDir.trim().isEmpty()) {
            return outputDir + File.separator + PackingProcessor.obtainFilename(this) + ".json";
        }
        return null;
    }

    public Array<ScaleFactorModel> getScaleFactors() {
        return scaleFactors;
    }

    public void setScaleFactors(Array<ScaleFactorModel> scaleFactors) {
        this.scaleFactors.clear();
        this.scaleFactors.addAll(scaleFactors);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(this, Property.SCALE_FACTORS));
        }
    }

    public void addInputFile(FileHandle fileHandle, InputFile.Type type) {
        addInputFile(new InputFile(fileHandle, type));
    }
    public void addInputFile(InputFile inputFile) {
        if (inputFiles.contains(inputFile, false)) {
            Gdx.app.error(TAG, "File: " + inputFile + " is already added");
            return;
        }
        if (inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Ignore) {
            Gdx.app.error(TAG, "File: " + inputFile + " is a directory. Ignore files cannot be directories.");
            return;
        }
        inputFiles.add(inputFile);
        inputFile.setEventDispatcher(eventDispatcher);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(PackModel.this, Property.INPUT_FILE_ADDED)
                    .setInputFile(inputFile));
        }
    }

    public void removeInputFile(final FileHandle fileHandle, final InputFile.Type type) {
        removeInputFile(new InputFile(fileHandle, type));
    }
    public void removeInputFile(InputFile inputFile) {
        // Since we use equals and not == operator to compare InputFile values,
        // we have to find real value and don't use reference from parameter.
        int index = inputFiles.indexOf(inputFile, false);
        InputFile actualInputFile = index >= 0 ? inputFiles.get(index) : null;

        if (actualInputFile == null) {
            Gdx.app.error(TAG, "File: " + inputFile + " wasn't added");
            return;
        }
        inputFiles.removeValue(actualInputFile, false);
        actualInputFile.setEventDispatcher(null);

        if (eventDispatcher != null) {
            eventDispatcher.postEvent(new PackPropertyChangedEvent(PackModel.this, Property.INPUT_FILE_REMOVED)
                    .setInputFile(actualInputFile));
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int computeStateHash() {
        int settingsHash = computeSettingsStateHash();
        return StateHashUtils.computeHash(scaleFactors, inputFiles, settingsHash, name, filename, outputDir);
    }

    private int computeSettingsStateHash() {
        int result = Objects.hash(settings.pot, settings.multipleOfFour, settings.paddingX, settings.paddingY, settings.edgePadding, settings.duplicatePadding,
                settings.rotation, settings.minWidth, settings.minHeight, settings.maxWidth, settings.maxHeight,
                settings.square, settings.stripWhitespaceX, settings.stripWhitespaceY, settings.alphaThreshold,
                settings.filterMin, settings.filterMag, settings.wrapX, settings.wrapY, settings.format, settings.alias, settings.ignoreBlankImages,
                settings.fast, settings.debug, settings.silent, settings.combineSubdirectories, settings.ignore, settings.flattenPaths,
                settings.premultiplyAlpha, settings.useIndexes, settings.bleed, settings.bleedIterations, settings.limitMemory,
                settings.grid, settings.atlasExtension,
                skeletonSettings.getSlotName(), skeletonSettings.getX(), skeletonSettings.getY(),
                skeletonSettings.getWidth(), skeletonSettings.getHeight(),
                skeletonSettings.getAnchorX(), skeletonSettings.getAnchorY(), skeletonSettings.getDuration(),
                skeletonSettings.getAnchorFilesDir()
            );
        result = 31 * result + Arrays.hashCode(settings.scale);
        result = 31 * result + Arrays.hashCode(settings.scaleSuffix);
        result = 31 * result + Arrays.hashCode(settings.scaleResampling);
        return result;
    }
}