package dev.nolij.toomanyrecipeviewers.impl.widget;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
//? if >=21.1
import mezz.jei.common.util.PlaceableUtil;
import net.minecraft.client.gui.GuiGraphics;

public class DrawableWidget extends Widget implements IPlaceable<DrawableWidget> {
	
	private final IDrawable drawable;
	private int x;
	private int y;
	
	public DrawableWidget(IDrawable drawable, int x, int y) {
		this.drawable = drawable;
		this.x = x;
		this.y = y;
	}
	
	public DrawableWidget(IDrawable drawable) {
		this(drawable, 0, 0);
	}
	
	@Override
	public DrawableWidget setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

	//? if >=21.1 {
	@Override
	public DrawableWidget setPosition(int x, int y, int width, int height, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		return PlaceableUtil.setPosition(this, x, y, width, height, horizontalAlignment, verticalAlignment);
	}
	//?}
	
	@Override
	public int getWidth() {
		return drawable.getWidth();
	}
	
	@Override
	public int getHeight() {
		return drawable.getHeight();
	}
	
	@Override
	public Bounds getBounds() {
		return new Bounds(x, y, getWidth(), getHeight());
	}
	
	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		drawable.draw(draw, x, y);
	}
	
}
