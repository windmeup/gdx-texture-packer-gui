
package com.crashinvaders.texturepackergui.controllers.main;

import com.crashinvaders.texturepackergui.controllers.SkeletonController;
import com.crashinvaders.texturepackergui.events.PackAtlasUpdatedEvent;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.views.canvas.PagePreviewCanvas;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

/** Controls the Canvas instance inside MainController */
@Component
public class CanvasController {

	@Inject LocaleService localeService;
	@Inject EventDispatcher eventDispatcher;
	@Inject ModelService modelService;
	@Inject SkeletonController skeletonController;

	private PagePreviewCanvas canvas;
    private PackModel currentPack;

	public void initialize(PagePreviewCanvas canvas) {
        this.canvas = canvas;

        canvas.setCallback(new PagePreviewCanvas.Callback() {
            @Override
            public void atlasLoadError(PackModel pack) {
                //TODO supply with details
                eventDispatcher.postEvent(new ShowToastEvent().message(getString("toastPackLoadError", pack.getName())));
            }
        });

        currentPack = getSelectedPack();
        canvas.reloadPack(currentPack, skeletonController);
    }

    @OnEvent(ProjectInitializedEvent.class) void onEvent(ProjectInitializedEvent event) {
        if (canvas == null) return;

        currentPack = event.getProject().getSelectedPack();
        canvas.reloadPack(currentPack, skeletonController);
    }

    @OnEvent(ProjectPropertyChangedEvent.class) void onEvent(ProjectPropertyChangedEvent event) {
        if (canvas == null) return;

        switch (event.getProperty()) {
            case SELECTED_PACK:
                currentPack = event.getProject().getSelectedPack();
                canvas.reloadPack(currentPack, skeletonController);
                break;
        }
    }

    @OnEvent(PackAtlasUpdatedEvent.class) void onEvent(PackAtlasUpdatedEvent event) {
        if (canvas == null) return;

        if (event.getPack() == currentPack) {
            canvas.reloadPack(currentPack, skeletonController);
        }
    }

    /** @return localized string */
    private String getString(String key) {
        return localeService.getI18nBundle().get(key);
    }

    /** @return localized string */
    private String getString(String key, Object... args) {
        return localeService.getI18nBundle().format(key, args);
    }

    private PackModel getSelectedPack() {
        return getProject().getSelectedPack();
    }

    private ProjectModel getProject() {
        return modelService.getProject();
    }
}
