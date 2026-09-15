package dev.nolij.toomanyrecipeviewers.impl.recipe;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.builder.RecipeLayoutBuilder;
import dev.nolij.toomanyrecipeviewers.impl.jei.api.gui.widgets.ScrollGridWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.DeferredPlaceableWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.DrawableWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.FillingFlameWidget;
import dev.nolij.toomanyrecipeviewers.impl.widget.TextWidget;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
//? if >=21.1 {
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
//?}
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
//? if >=21.1
import mezz.jei.api.gui.widgets.IDrawableWidget;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.gui.widgets.ITextWidget;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.UserInput;
import mezz.jei.library.gui.widgets.ScrollBoxRecipeWidget;
//? if >=21.1 {
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableAnimated;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.elements.DrawableCombined;
import mezz.jei.common.gui.elements.DrawableRecipeWidget;
//?}
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

abstract class TMRVRecipeWidget extends Widget implements IRecipeExtrasBuilder {
	
	protected static class RecipeSlotsView implements IRecipeSlotsView {
		protected final List<IRecipeSlotView> slots = new ArrayList<>();
		
		@Override
		public @Unmodifiable List<IRecipeSlotView> getSlotViews() {
			return slots;
		}
	}
	
	protected static class RecipeSlotDrawablesView implements IRecipeSlotDrawablesView {
		protected final List<IRecipeSlotDrawable> slots = new ArrayList<>();
		
		@Override
		public @Unmodifiable List<IRecipeSlotDrawable> getSlots() {
			return slots;
		}
	}
	
	private final WidgetHolder widgets;
	protected final RecipeSlotsView slotsView;
	protected final RecipeSlotDrawablesView slotDrawablesView;
	protected final Bounds bounds;
	
	private final ArrayList<IRecipeWidget> recipeWidgets = new ArrayList<>();
	private final ArrayList<IJeiInputHandler> inputHandlers = new ArrayList<>();
	private final ArrayList<IJeiGuiEventListener> guiEventListeners = new ArrayList<>();
	
	TMRVRecipeWidget(WidgetHolder widgets, int width, int height) {
		this.widgets = widgets;
		this.slotsView = new RecipeSlotsView();
		this.slotDrawablesView = new RecipeSlotDrawablesView();
		this.bounds = new Bounds(0, 0, width, height);
	}
	
	void addSlotWidgets(RecipeLayoutBuilder builder, EmiRecipe emiRecipe) {
		for (final var slot : builder.getSlots()) {
			if (!slot.isVisible())
				continue;
			
			final var widget = widgets.add(slot.build());
			slotsView.slots.add((IRecipeSlotView) widget);
			slotDrawablesView.slots.add((IRecipeSlotDrawable) widget);
			
			if (slot.role == RecipeIngredientRole.OUTPUT)
				widget.recipeContext(emiRecipe);
		}
	}
	
	@Override
	public Bounds getBounds() {
		return bounds;
	}
	
	protected abstract void renderSetup(EmiDrawContext context, int mouseX, int mouseY, float delta);
	
	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		final var context = EmiDrawContext.wrap(draw);
		
		renderSetup(context, mouseX, mouseY, delta);
		
		for (final var recipeWidget : recipeWidgets) {
			context.push();
			
			final var position = recipeWidget.getPosition();
			context.matrices().translate(position.x(), position.y(), 0F);
			
			recipeWidget.drawWidget(context.raw(), mouseX - position.x(), mouseY - position.y());
			
			context.resetColor();
			context.pop();
		}
	}
	
	protected abstract void buildTooltip(ITooltipBuilder builder, int mouseX, int mouseY);
	
	@Override
	public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
		final var tooltipBuilder = new JemiTooltipBuilder();
		buildTooltip(tooltipBuilder, mouseX, mouseY);
		for (final var recipeWidget : recipeWidgets) {
			final var position = recipeWidget.getPosition();
			recipeWidget.getTooltip(tooltipBuilder, mouseX - position.x(), mouseY - position.y());
		}
		
		return tooltipBuilder.tooltip;
	}
	
	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		return
			handleInput(mouseX, mouseY, InputConstants.Type.MOUSE.getOrCreate(button), 0, InputType.SIMULATE) &&
				handleInput(mouseX, mouseY, InputConstants.Type.MOUSE.getOrCreate(button), 0, InputType.EXECUTE);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return handleInput(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, InputConstants.getKey(keyCode, scanCode), modifiers, InputType.IMMEDIATE);
	}
	
	private static boolean containsPoint(ScreenRectangle area, int x, int y) {
		return
			area.top() <= y && y <= area.bottom() &&
				area.left() <= x && x <= area.right();
	}
	
	protected abstract boolean handleInput(int mouseX, int mouseY, InputConstants.Key key);
	
	private boolean handleInput(int mouseX, int mouseY, InputConstants.Key key, int modifiers, InputType inputType) {
		final var input = new UserInput(key, mouseX, mouseY, modifiers, inputType);
		
		for (final var inputHandler : inputHandlers) {
			final var area = inputHandler.getArea();
			if (containsPoint(area, mouseX, mouseY)) {
				final var position = area.position();
				if (inputHandler.handleInput(mouseX - position.x(), mouseY - position.y(), input))
					return true;
			}
		}
		for (final var guiEventListener : guiEventListeners) {
			final var area = guiEventListener.getArea();
			if (containsPoint(area, mouseX, mouseY)) {
				final var position = area.position();
				final var x = mouseX - position.x();
				final var y = mouseY - position.y();
				if (switch (key.getType()) {
					case MOUSE -> guiEventListener.mouseReleased(x, y, key.getValue());
					case KEYSYM -> guiEventListener.keyPressed(x, y, key.getValue(), 0, modifiers);
					default -> false;
				})
					return true;
			}
		}
		
		return handleInput(mouseX, mouseY, key);
	}
	
	@Override
	public IRecipeSlotDrawablesView getRecipeSlots() {
		return slotDrawablesView;
	}
	
	@Override
	public void addDrawable(IDrawable drawable, int x, int y) {
		widgets.add(new DrawableWidget(drawable, x, y));
	}
	
	@Override
	public IPlaceable<?> addDrawable(IDrawable drawable) {
		return widgets.add(new DrawableWidget(drawable));
	}

	//? if >=21.1 {
	@Override
	public IDrawableWidget addDrawableWidget(IDrawable drawable) {
		final var widget = new DrawableRecipeWidget(drawable);
		addWidget(widget);
		return widget;
	}

	@Override
	public IDrawableWidget addTooltipArea(int x, int y, int width, int height) {
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("Tooltip area dimensions must be non-negative");
		return addDrawableWidget(new DrawableBlank(width, height)).setPosition(x, y);
	}
	//?}
	
	@Override
	public void addWidget(IRecipeWidget recipeWidget) {
		recipeWidgets.add(recipeWidget);
	}
	
	@Override
	public void addSlottedWidget(ISlottedRecipeWidget slottedRecipeWidget, List<IRecipeSlotDrawable> slots) {
		addWidget(slottedRecipeWidget);
		if (slottedRecipeWidget instanceof IJeiInputHandler inputHandler)
			addInputHandler(inputHandler);
		if (slottedRecipeWidget instanceof IJeiGuiEventListener guiEventListener)
			addGuiEventListener(guiEventListener);
	}
	
	@Override
	public void addInputHandler(IJeiInputHandler inputHandler) {
		inputHandlers.add(inputHandler);
	}
	
	@Override
	public void addGuiEventListener(IJeiGuiEventListener listener) {
		guiEventListeners.add(listener);
	}
	
	@Override
	public IScrollBoxWidget addScrollBoxWidget(int width, int height, int x, int y) {
		final var widget = new ScrollBoxRecipeWidget(width, height, x, y);
		addWidget(widget);
		addInputHandler(widget);
		return widget;
	}
	
	@Override
	public IScrollGridWidget addScrollGridWidget(List<IRecipeSlotDrawable> slots, int columns, int visibleRows) {
		return new ScrollGridWidget(widgets, slots, columns, visibleRows);
	}
	
	private DeferredPlaceableWidget addTexture(EmiTexture texture) {
		return new DeferredPlaceableWidget((x, y) -> widgets.addTexture(texture, x, y), texture.width, texture.height);
	}
	
	@Override
	public IPlaceable<?> addRecipeArrow() {
		return addTexture(EmiTexture.EMPTY_ARROW);
	}

	//? if >=21.1 {
	@Override
	public IDrawableWidget addRecipeArrowWidget() {
		return addDrawableWidget(Internal.getTextures().getRecipeArrow());
	}
	//?}
	
	@Override
	public IPlaceable<?> addRecipePlusSign() {
		return addTexture(EmiTexture.PLUS);
	}

	//? if >=21.1 {
	@Override
	public IDrawableWidget addRecipePlusSignWidget() {
		return addDrawableWidget(Internal.getTextures().getRecipePlusSign());
	}
	//?}
	
	@Override
	public IPlaceable<?> addAnimatedRecipeArrow(int cookTime) {
		return new DeferredPlaceableWidget((x, y) -> widgets.addFillingArrow(x, y, cookTime * 50), EmiTexture.EMPTY_ARROW.width, EmiTexture.EMPTY_ARROW.height);
	}

	//? if >=21.1 {
	@Override
	public IDrawableWidget addAnimatedRecipeArrowWidget(int cookTime) {
		final var textures = Internal.getTextures();
		final IDrawableStatic filled = textures.getRecipeArrowFilled();
		final var animated = new DrawableAnimated(filled, cookTime, IDrawableAnimated.StartDirection.LEFT, false);
		return addDrawableWidget(new DrawableCombined(textures.getRecipeArrow(), animated));
	}
	//?}
	
	@Override
	public IPlaceable<?> addAnimatedRecipeFlame(int cookTime) {
		return new DeferredPlaceableWidget((x, y) -> widgets.add(new FillingFlameWidget(x, y, cookTime * 50)), EmiTexture.EMPTY_FLAME.width, EmiTexture.EMPTY_FLAME.height);
	}

	//? if >=21.1 {
	@Override
	public IDrawableWidget addAnimatedRecipeFlameWidget(int cookTime) {
		final var textures = Internal.getTextures();
		final IDrawableStatic flame = textures.getFlameIcon();
		final var animated = new DrawableAnimated(flame, cookTime, IDrawableAnimated.StartDirection.TOP, true);
		return addDrawableWidget(new DrawableCombined(textures.getFlameEmptyIcon(), animated));
	}
	//?}
	
	@Override
	public ITextWidget addText(List<FormattedText> lines, int maxWidth, int maxHeight) {
		//? if >=21.1 {
		final var widget = new mezz.jei.common.gui.elements.TextWidget(lines, 0, 0, maxWidth, maxHeight);
		addWidget(widget);
		return widget;
		//?} else
		//return widgets.add(new TextWidget(lines, maxWidth, maxHeight));
	}
	
}
