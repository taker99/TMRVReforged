package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.WidgetGroup;
import dev.nolij.toomanyrecipeviewers.client.recipe.TMRVRecipeWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

@Mixin(RecipeScreen.class)
public abstract class RecipeScreenMixin extends Screen {
	@Shadow(remap = false) private List<WidgetGroup> currentPage;

	protected RecipeScreenMixin(Component title) {
		super(title);
	}

	@Unique
	private boolean tmrv$dispatchMouse(double mouseX, double mouseY, int button, double deltaX, double deltaY, boolean release) {
		for (final var group : currentPage) {
			for (final var widget : group.widgets) {
				if (widget instanceof TMRVRecipeWidget tmrvWidget) {
					try {
						final var handled = release
							? tmrvWidget.mouseReleased(mouseX - group.x(), mouseY - group.y(), button)
							: tmrvWidget.mouseDragged(mouseX - group.x(), mouseY - group.y(), button, deltaX, deltaY);
						if (handled)
							return true;
					} catch (Throwable t) {
						LOGGER.error("Error handling JEI widget input", t);
						group.error(t);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Inject(method = {"mouseReleased", "m_6348_"}, remap = false, at = @At("HEAD"), cancellable = true)
	private void tmrv$mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (tmrv$dispatchMouse(mouseX, mouseY, button, 0, 0, true))
			cir.setReturnValue(true);
	}

	@Inject(method = {"mouseDragged", "m_7979_"}, remap = false, at = @At("HEAD"), cancellable = true)
	private void tmrv$mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
		if (tmrv$dispatchMouse(mouseX, mouseY, button, deltaX, deltaY, false))
			cir.setReturnValue(true);
	}

	@Inject(method = {"mouseScrolled", "m_6050_"}, remap = false, at = @At("HEAD"), cancellable = true)
	private void tmrv$mouseScrolled(double mouseX, double mouseY, /*? if >=21.1 {*/double scrollX, /*?}*/double scrollY, CallbackInfoReturnable<Boolean> cir) {
		for (final var group : currentPage) {
			for (final var widget : group.widgets) {
				if (widget instanceof TMRVRecipeWidget tmrvWidget) {
					try {
						if (tmrvWidget.mouseScrolled(mouseX - group.x(), mouseY - group.y(), /*? if >=21.1 {*/scrollX/*?} else {*//*0*//*?}*/, scrollY)) {
							cir.setReturnValue(true);
							return;
						}
					} catch (Throwable t) {
						LOGGER.error("Error handling JEI widget scrolling", t);
						group.error(t);
						cir.setReturnValue(true);
						return;
					}
				}
			}
		}
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for (final var group : currentPage) {
			for (final var widget : group.widgets) {
				if (widget instanceof TMRVRecipeWidget tmrvWidget) {
					try {
						tmrvWidget.mouseMoved(mouseX - group.x(), mouseY - group.y());
					} catch (Throwable t) {
						LOGGER.error("Error handling JEI widget mouse movement", t);
						group.error(t);
						break;
					}
				}
			}
		}
		super.mouseMoved(mouseX, mouseY);
	}
}
