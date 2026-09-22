package dev.nolij.toomanyrecipeviewers.compat.plugin;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Optional;

final class TMRVDragDropHandler implements EmiDragDropHandler<Screen> {

	private final IScreenHelper screenHelper;

	TMRVDragDropHandler(IScreenHelper screenHelper) {
		this.screenHelper = screenHelper;
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public boolean dropStack(Screen screen, EmiIngredient stack, int x, int y) {
		try {
			return drop(screen, (Optional<ITypedIngredient<Object>>) (Optional) JemiUtil.getTyped(stack.getEmiStacks().getFirst()), x, y);
		} catch (Exception ignored) {
			return false;
		}
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void render(Screen screen, EmiIngredient dragged, GuiGraphics draw, int mouseX, int mouseY, float delta) {
		try {
			render(screen, EmiDrawContext.wrap(draw), (Optional<ITypedIngredient<Object>>) (Optional) JemiUtil.getTyped(dragged.getEmiStacks().getFirst()));
		} catch (Exception ignored) {
		}
	}

	private <I> boolean drop(Screen screen, Optional<ITypedIngredient<I>> ingredient, int x, int y) {
		if (ingredient.isEmpty())
			return false;

		for (final var target : getTargets(screen, ingredient.get())) {
			if (target.getArea().contains(x, y)) {
				target.accept(ingredient.get().getIngredient());
				return true;
			}
		}
		return false;
	}

	private <I> void render(Screen screen, EmiDrawContext context, Optional<ITypedIngredient<I>> ingredient) {
		if (ingredient.isEmpty())
			return;

		for (final var target : getTargets(screen, ingredient.get())) {
			final var area = target.getArea();
			context.fill(area.getX(), area.getY(), area.getWidth(), area.getHeight(), 0x8822BB33);
		}
	}

	@SuppressWarnings("unchecked")
	private <I> List<IGhostIngredientHandler.Target<I>> getTargets(Screen screen, ITypedIngredient<I> ingredient) {
		return screenHelper.<Screen>getGhostIngredientHandlers(screen).stream()
			.flatMap(handler -> handler.getTargetsTyped(screen, ingredient, false).stream())
			.toList();
	}

}
