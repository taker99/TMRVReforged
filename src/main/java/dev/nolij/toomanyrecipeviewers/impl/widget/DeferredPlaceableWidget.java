package dev.nolij.toomanyrecipeviewers.impl.widget;

import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
//? if >=21.1
import mezz.jei.common.util.PlaceableUtil;

public class DeferredPlaceableWidget implements IPlaceable<DeferredPlaceableWidget> {
	
	@FunctionalInterface
	public interface PlaceableCallback {
		void setPosition(int x, int y);
	}
	
	private final PlaceableCallback callback;
	private final int width, height;
	
	private boolean placed = false;
	
	public DeferredPlaceableWidget(PlaceableCallback callback, int width, int height) {
		this.callback = callback;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public DeferredPlaceableWidget setPosition(int x, int y) {
		if (placed)
			throw new IllegalStateException();
		placed = true;
		
		callback.setPosition(x, y);
		return this;
	}

	//? if >=21.1 {
	@Override
	public DeferredPlaceableWidget setPosition(int x, int y, int width, int height, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		return PlaceableUtil.setPosition(this, x, y, width, height, horizontalAlignment, verticalAlignment);
	}
	//?}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
}
