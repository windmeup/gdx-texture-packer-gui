package com.crashinvaders.texturepackergui.views.canvas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.controllers.SkeletonController;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.views.canvas.model.AtlasModel;
import com.crashinvaders.texturepackergui.views.canvas.widgets.BackgroundWidget;
import com.crashinvaders.texturepackergui.views.canvas.widgets.preview.InfoPanel;
import com.crashinvaders.texturepackergui.views.canvas.widgets.preview.PreviewHolder;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.AnimationInfoPanel;
import com.crashinvaders.texturepackergui.views.canvas.widgets.spine.AnimationViewer;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.AbstractNonParentalActorLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;
import com.kotcrab.vis.ui.widget.VisImageTextButton;
import com.kotcrab.vis.ui.widget.VisTable;
import lombok.Getter;

import java.io.IOException;

public class PagePreviewCanvas extends Stack {

	private final PreviewHolder previewHolder;

	private final InfoPanel infoPanel;
	private final VisImageTextButton btnNextPage;
	private final VisImageTextButton btnPrevPage;

	private final Rectangle widgetAreaBounds = new Rectangle();
	private final Rectangle scissorBounds = new Rectangle();

	private Callback callback;

	private AtlasModel atlas;
	private int pageIndex = 0;

	@Getter
	private boolean showAnimations;

	@Getter
	private AnimationViewer animationViewer;

	private AnimationInfoPanel animationInfoPanel;

	public PagePreviewCanvas(Skin skin) {
		// Layout
		{
			// Background
			{
				BackgroundWidget backgroundWidget = new BackgroundWidget(skin);
				addActor(backgroundWidget);
			}

			// Page preview
			{
				previewHolder = new PreviewHolder(skin);
				previewHolder.setListener(new PreviewHolder.Listener() {
					@Override
					public void onZoomChanged(int percentage) {
						infoPanel.setZoomLevel(percentage);
					}
				});
				addActor(previewHolder);
			}

			animationViewer = new AnimationViewer(skin, percentage ->
				animationInfoPanel.setZoomLevel(percentage));
			addActor(animationViewer);

			// Page buttons
			{
				String text = App.inst().getI18n().get("atlasPreviewNextPage");
				btnNextPage = new VisImageTextButton(text, "default");
				{
					btnNextPage.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            showNextPage();
                        }
                    });
					btnNextPage.setFocusBorderEnabled(false);

					VisImageTextButton.VisImageTextButtonStyle style = btnNextPage.getStyle();
					style.imageUp = skin.getDrawable("custom/page-button-next");
					btnNextPage.setStyle(style);
					btnNextPage.getImage().setColor(new Color(0xffffffa0));
					btnNextPage.align(Align.left);
					btnNextPage.padBottom(2f).padRight(8f);
					btnNextPage.getImageCell().padLeft(6f).padRight(4f);
					btnNextPage.getLabelCell().padBottom(2f);
				}

				text = App.inst().getI18n().get("atlasPreviewPrevPage");
				btnPrevPage = new VisImageTextButton(text, "default");
				{
					btnPrevPage.addListener(new ChangeListener() {
						@Override
						public void changed(ChangeEvent event, Actor actor) {
							showPrevPage();
                        }
                    });
					btnPrevPage.setFocusBorderEnabled(false);

					VisImageTextButton.VisImageTextButtonStyle style = btnPrevPage.getStyle();
					style.imageUp = skin.getDrawable("custom/page-button-prev");
					btnPrevPage.setStyle(style);
					btnPrevPage.getImage().setColor(new Color(0xffffff80));
					btnPrevPage.align(Align.left);
					btnPrevPage.padBottom(2f).padRight(8f);
					btnPrevPage.getImageCell().padLeft(6f).padRight(4f);
					btnPrevPage.getLabelCell().padBottom(2f);
				}

				VisTable table = new VisTable();
				table.defaults().right().fillX();
				table.add(btnNextPage);
				table.row().padTop(6f);
				table.add(btnPrevPage);

				Container<Actor> container = new Container<>(table);
				container.align(Align.topRight);
				container.padTop(30f);
				addActor(container);
			}

			// Info pane
			{
				infoPanel = new InfoPanel(App.inst().getInterfaceService().getParser());
				addActor(infoPanel);
				animationInfoPanel = new AnimationInfoPanel(App.inst().getInterfaceService().getParser());
				addActor(animationInfoPanel);
			}
			setShowAnimations(false);
		}
	}

	public void setShowAnimations(boolean showAnimations) {
		this.showAnimations = showAnimations;
		boolean reverse = !showAnimations;
		previewHolder.setVisible(reverse);
		btnNextPage.setVisible(reverse);
		btnPrevPage.setVisible(reverse);
		infoPanel.setVisible(reverse);
		animationViewer.setVisible(showAnimations);
		animationInfoPanel.setVisible(showAnimations);
	}

	// Apply scissors
	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.flush();
		getStage().calculateScissors(widgetAreaBounds.set(getX(), getY(), getWidth(), getHeight()), scissorBounds);
		if (ScissorStack.pushScissors(scissorBounds)) {
			super.draw(batch, parentAlpha);
			batch.flush();
			ScissorStack.popScissors();
		}
	}

	public void reloadPack(PackModel pack, SkeletonController skeletonController) {
		skeletonController.clear();
		animationViewer.getAnimationPanel().clear();
		String atlasPath = null;
		if (pack != null) {
			atlasPath = pack.getAtlasPath();
		}

//		// Check if atlas the same
//		if (atlas != null && atlas.getAtlasPath().equals(atlasPath)) return;

		pageIndex = 0;
		previewHolder.reset();
//		infoPanel.setPagesAmount(0);
		if (atlas != null) {
			atlas.dispose();
			atlas = null;
		}

		if (atlasPath != null) {
			FileHandle packFile = Gdx.files.absolute(atlasPath);
			if (packFile != null && packFile.exists() && !packFile.isDirectory()) {
				try {
					atlas = new AtlasModel(packFile);

					previewHolder.setPage(atlas, pageIndex);
					infoPanel.setAtlasPage(atlas.getPages().get(pageIndex));
//					infoPanel.setCurrentPage(pageIndex + 1);
//					infoPanel.setPagesAmount(atlas.getPages().size);
//					infoPanel.updatePageInfo();

				} catch (GdxRuntimeException ex) {
					if (atlas != null) {
						atlas.dispose();
						atlas = null;
					}
					callback.atlasLoadError(pack);
				}
				if (atlas != null) {
					String skeletonPath = pack.getSkeletonPath();
					if (skeletonPath != null) {
						FileHandle skeletonHandle = new FileHandle(skeletonPath);
						if (skeletonHandle.exists() && !skeletonHandle.isDirectory()) {
							boolean success = true;
							try {
								skeletonController.setSkeletonPath(skeletonPath);
							} catch (IOException ex) {
								success = false;
							}
							if (success) {
								SkeletonJson skeletonJson = new SkeletonJson(new TextureAtlas(atlas.getAtlasData()));
								SkeletonData skeletonData = skeletonJson.readSkeletonData(skeletonHandle);
								skeletonController.setSkeletonData(skeletonData);
								animationViewer.getAnimationPanel().setSkeletonData(skeletonData);
							}
						}
					}
				}
			}
		}
		updatePageButtonsVisibility();

		infoPanel.setVisible(atlas != null);
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	private void showNextPage() {
		if (atlas == null || atlas.getPages().size == 0) return;

		pageIndex = pageIndex +1 >= atlas.getPages().size ? 0 : pageIndex+1;

		previewHolder.setPage(atlas, pageIndex);
//		infoPanel.setCurrentPage(pageIndex +1);
		infoPanel.setAtlasPage(atlas.getPages().get(pageIndex));
		updatePageButtonsVisibility();
	}

	private void showPrevPage() {
		if (atlas == null || atlas.getPages().size == 0) return;

		pageIndex = pageIndex -1 < 0 ? atlas.getPages().size-1 : pageIndex-1;

		previewHolder.setPage(atlas, pageIndex);
//		infoPanel.setCurrentPage(pageIndex +1);
		infoPanel.setAtlasPage(atlas.getPages().get(pageIndex));
		updatePageButtonsVisibility();
	}

	private void updatePageButtonsVisibility() {
		if (showAnimations) {
			return;
		}
		if (atlas == null) {
			btnNextPage.setVisible(false);
			btnPrevPage.setVisible(false);
			return;
		}

		int pagesAmount = atlas.getPages().size;

		btnNextPage.setVisible(pagesAmount > 1);
		btnPrevPage.setVisible(pagesAmount > 1);
//		btnNextPage.setDisabled(!(pageIndex < pagesAmount-1));
//		btnPrevPage.setDisabled(!(pageIndex > 0));
	}

	public interface Callback {
		void atlasLoadError(PackModel pack);
	}

	public static class CanvasLmlTagProvider implements LmlTagProvider {
		@Override
		public LmlTag create(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
			return new CanvasLmlTag(parser, parentTag, rawTagData);
		}
	}

	public static class CanvasLmlTag extends AbstractNonParentalActorLmlTag {
		public CanvasLmlTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
			super(parser, parentTag, rawTagData);
		}

		@Override
		protected Actor getNewInstanceOfActor(LmlActorBuilder builder) {
			return new PagePreviewCanvas(getSkin(builder));
		}
	}
}
