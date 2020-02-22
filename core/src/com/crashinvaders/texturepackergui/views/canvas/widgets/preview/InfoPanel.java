package com.crashinvaders.texturepackergui.views.canvas.widgets.preview;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.views.canvas.model.PageModel;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.DefaultLmlParser;

import java.util.Locale;

public class InfoPanel extends Container {

    private final I18NBundle i18nBundle = App.inst().getInterfaceService().getParser().getData().getDefaultI18nBundle();
    private final Label lblPages;
    private final Label lblZoom;
    private final Label lblFileSize;
    private final Label lblPageDimens;

    private PageModel atlasPage;

    private int pagesAmount, currentPage;

    public InfoPanel(LmlParser parser) {
        align(Align.top);
        fillX();

        // Workaround of parser's only single parsing operation limitation
        LmlParser localParser = new DefaultLmlParser(parser.getData());
        localParser.setSyntax(parser.getSyntax());
        Group root = (Group) (localParser.parseTemplate(Gdx.files.internal("lml/preview/canvasInfoPanel.lml")).first());
        setActor(root);

        lblPages = root.findActor("lblPages");
        lblZoom = root.findActor("lblZoom");
        lblPageDimens = root.findActor("lblPageDimens");
        lblFileSize = root.findActor("lblFileSize");

        updatePagesText();
        setZoomLevel(100f);
    }

    public void setZoomLevel(float zoom) {
        lblZoom.setText((String.format(Locale.US, "%.0f%%", zoom)));
    }

    public void setAtlasPage(PageModel atlasPage) {
        this.atlasPage = atlasPage;
        updatePageInfo();
    }

    private void updatePageInfo() {
        if (atlasPage == null) return;

        setPageDimens(atlasPage.getWidth(), atlasPage.getHeight());
        setCurrentPage(atlasPage.getPageIndex() + 1);
        setPagesAmount(atlasPage.getAtlasModel().getPages().size);
        setFileSize(atlasPage.getAtlasModel().getAtlasData().getPages().get(atlasPage.getPageIndex()).textureFile.length());
    }

    private void setFileSize(long bytes) {
        double megabytes = bytes / 1048576D;
        megabytes = Math.max(megabytes, 0.01); // To avoid cases when few KB displays as 0.00
        lblFileSize.setText(String.format(Locale.US, "%.2f", megabytes));
    }

    private void setPageDimens(int width, int height) {
        lblPageDimens.setText(width + " x " + height);
    }

    private void setPagesAmount(int pagesAmount) {
        this.pagesAmount = pagesAmount;
        updatePagesText();
    }

    private void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        updatePagesText();
    }

    private void updatePagesText() {
        if (pagesAmount <= 0) {
            lblPages.setText(i18nBundle.get("canvasInfoNoPageToShow"));
        } else {
            lblPages.setText(currentPage + "/" + pagesAmount);
        }
    }
}
