package dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.ingredient;

import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.TMRVTooltipBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.library.gui.recipes.layout.builder.LegacyTooltipCallbackAdapter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

import java.util.List;

@SuppressWarnings("NonExtendableApiUsage")
public interface ITMRVRecipeSlotDrawable extends IRecipeSlotDrawable {

	List<ClientTooltipComponent> tmrv$getTooltipComponents();

	void addTooltipCallbacks(List<IRecipeSlotRichTooltipCallback> tooltipCallbacks);
	
	@SuppressWarnings("removal")
	@Override
	void draw(GuiGraphics guiGraphics);
	
	@SuppressWarnings("removal")
	@Override
	default void drawHoverOverlays(GuiGraphics guiGraphics) {
		drawHighlight(guiGraphics, 0x80FFFFFF);
	}
	
	@SuppressWarnings("removal")
	@Override
	default List<Component> getTooltip() {
		return new TMRVTooltipBuilder(tmrv$getTooltipComponents()).toLegacyToComponents();
	}
	
	@SuppressWarnings("removal")
	@Override
	default void getTooltip(ITooltipBuilder tooltipBuilder) {
		new TMRVTooltipBuilder(tmrv$getTooltipComponents()).getLines().forEach(line ->
			line.ifLeft(tooltipBuilder::add).ifRight(tooltipBuilder::add));
	}
	
	@Override
	default void drawTooltip(GuiGraphics guiGraphics, int i, int i1) {
		final var tooltip = new JeiTooltip();
		getTooltip(tooltip);
		tooltip.draw(guiGraphics, i, i1);
	}
	
	@SuppressWarnings("removal")
	@Override
	default boolean isMouseOver(double x, double y) {
		return getRect().contains((int) x, (int) y);
	}
	
	@SuppressWarnings("removal")
	@Override
	default void addTooltipCallback(mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback tooltipCallback) {
		addTooltipCallbacks(List.of(new LegacyTooltipCallbackAdapter(tooltipCallback)));
	}
	
	@SuppressWarnings("removal")
	@Override
	default Rect2i getAreaIncludingBackground() {
		return getRect();
	}
	
	@Override
	default void drawHighlight(GuiGraphics guiGraphics, int colour) {
		final var rect = getRect();
		guiGraphics.fillGradient(RenderType.guiOverlay(), rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), colour, colour, 0);
	}
	
}
