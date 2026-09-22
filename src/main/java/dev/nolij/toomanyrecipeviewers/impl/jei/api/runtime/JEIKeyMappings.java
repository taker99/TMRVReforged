package dev.nolij.toomanyrecipeviewers.impl.jei.api.runtime;

import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.input.EmiBind;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod.LOGGER;

public class JEIKeyMappings implements IInternalKeyMappings {
	
	private record EMIDelegatedJEIKeyMapping(Supplier<EmiBind> delegateGetter) implements IJeiKeyMappingInternal {
		
		@Override
		public boolean isActiveAndMatches(InputConstants.@NotNull Key key) {
			return switch (key.getType()) {
				case KEYSYM -> delegateGetter.get().matchesKey(key.getValue(), 0);
				case SCANCODE -> delegateGetter.get().matchesKey(0, key.getValue());
				case MOUSE -> delegateGetter.get().matchesMouse(key.getValue());
			};
		}
		
		@Override
		public boolean isUnbound() {
			return !delegateGetter.get().isBound();
		}
		
		@Override
		public @NotNull Component getTranslatedKeyMessage() {
			return delegateGetter.get().getBindText();
		}
		
		@Override
		public boolean isDown() {
			return delegateGetter.get().isHeld();
		}
		
		@Override
		public IJeiKeyMappingInternal register(Consumer<KeyMapping> consumer) {
			LOGGER.error(new UnsupportedOperationException());
			return this;
		}
		
	}
	
	private record MinecraftDelegatedJEIKeyMapping(KeyMapping delegate) implements IJeiKeyMappingInternal {
		
		@Override
		public boolean isActiveAndMatches(InputConstants.@NotNull Key key) {
			return delegate.isActiveAndMatches(key);
		}
		
		@Override
		public boolean isUnbound() {
			return delegate.isUnbound();
		}
		
		@Override
		public @NotNull Component getTranslatedKeyMessage() {
			return delegate.getTranslatedKeyMessage();
		}
		
		@Override
		public boolean isDown() {
			return delegate.isDown();
		}
		
		@Override
		public IJeiKeyMappingInternal register(Consumer<KeyMapping> consumer) {
			consumer.accept(delegate);
			return this;
		}
		
	}
	
	private static class DummyJEIKeyMapping implements IJeiKeyMappingInternal {
		
		@Override
		public boolean isActiveAndMatches(InputConstants.@NotNull Key key) {
			return false;
		}
		
		@Override
		public boolean isUnbound() {
			return true;
		}
		
		@Override
		public @NotNull Component getTranslatedKeyMessage() {
			return Component.translatable("key.keyboard.unknown");
		}
		
		@Override
		public boolean isDown() {
			return false;
		}
		
		@Override
		public IJeiKeyMappingInternal register(Consumer<KeyMapping> consumer) {
			return this;
		}
		
	}

	private record UnboundJEIKeyMapping(String translationKey) implements IJeiKeyMappingInternal {

		@Override
		public boolean isActiveAndMatches(InputConstants.@NotNull Key key) {
			return false;
		}

		@Override
		public boolean isUnbound() {
			return true;
		}

		@Override
		public @NotNull Component getTranslatedKeyMessage() {
			return Component.translatable(translationKey);
		}

		@Override
		public boolean isDown() {
			return false;
		}

		@Override
		public IJeiKeyMappingInternal register(Consumer<KeyMapping> consumer) {
			return this;
		}

	}

	private record FixedJEIKeyMapping(List<InputConstants.Key> keys) implements IJeiKeyMappingInternal {

		FixedJEIKeyMapping(InputConstants.Key... keys) {
			this(List.of(keys));
		}

		@Override
		public boolean isActiveAndMatches(InputConstants.@NotNull Key key) {
			return keys.contains(key);
		}

		@Override
		public boolean isUnbound() {
			return false;
		}

		@Override
		public @NotNull Component getTranslatedKeyMessage() {
			return keys.get(0).getDisplayName();
		}

		@Override
		public boolean isDown() {
			final var window = Minecraft.getInstance().getWindow().getWindow();
			return keys.stream().anyMatch(key -> switch (key.getType()) {
				case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
				case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
				case SCANCODE -> false;
			});
		}

		@Override
		public IJeiKeyMappingInternal register(Consumer<KeyMapping> consumer) {
			return this;
		}

	}
	
	private final IJeiKeyMappingInternal dummy = new DummyJEIKeyMapping();
	
	private final IJeiKeyMappingInternal toggleVisibility = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.toggleVisibility);
	private final IJeiKeyMappingInternal focusSearch = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.focusSearch);
	private final IJeiKeyMappingInternal back = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.back);
	//? if >=21.1
	private final IJeiKeyMappingInternal forward = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.forward);
	private final IJeiKeyMappingInternal viewRecipes = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.viewRecipes);
	private final IJeiKeyMappingInternal viewUses = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.viewUses);
	private final IJeiKeyMappingInternal cheatOneToInventory = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.cheatOneToInventory);
	private final IJeiKeyMappingInternal cheatStackToInventory = new EMIDelegatedJEIKeyMapping(() -> EmiConfig.cheatStackToInventory);
	private final IJeiKeyMappingInternal escape = new FixedJEIKeyMapping(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_ESCAPE));
	private final IJeiKeyMappingInternal leftClick = new FixedJEIKeyMapping(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_LEFT));
	private final IJeiKeyMappingInternal rightClick = new FixedJEIKeyMapping(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT));
	private final IJeiKeyMappingInternal enter = new FixedJEIKeyMapping(
		InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_ENTER),
		InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_KP_ENTER)
	);
	private final IJeiKeyMappingInternal leftShift = new FixedJEIKeyMapping(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT));
	private final IJeiKeyMappingInternal controlOrCommand = new FixedJEIKeyMapping(InputConstants.Type.KEYSYM.getOrCreate(Minecraft.ON_OSX ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL));
	private final IJeiKeyMappingInternal shareToChat = new UnboundJEIKeyMapping("key.jei.shareToChat");
	
	@Override
	public IJeiKeyMapping getToggleOverlay() {
		return toggleVisibility;
	}
	
	@Override
	public IJeiKeyMapping getFocusSearch() {
		return focusSearch;
	}
	
	@Override
	public IJeiKeyMapping getToggleCheatMode() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getToggleEditMode() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getToggleCheatModeConfigButton() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getRecipeBack() {
		return back;
	}

	//? if >=21.1 {
	@Override
	public IJeiKeyMapping getRecipeForward() {
		return forward;
	}
	//?}
	
	@Override
	public IJeiKeyMapping getPreviousCategory() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getNextCategory() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getPreviousRecipePage() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getNextRecipePage() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getPreviousPage() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getNextPage() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getCloseRecipeGui() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getBookmark() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getToggleBookmarkOverlay() {
		return dummy;
	}
	
	@Override
	public @NotNull IJeiKeyMapping getShowRecipe() {
		return viewRecipes;
	}
	
	@Override
	public @NotNull IJeiKeyMapping getShowUses() {
		return viewUses;
	}
	
	@Override
	public IJeiKeyMapping getTransferRecipeBookmark() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getMaxTransferRecipeBookmark() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getQuickMove() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getCheatOneItem() {
		return cheatOneToInventory;
	}
	
	@Override
	public IJeiKeyMapping getCheatItemStack() {
		return cheatStackToInventory;
	}
	
	@Override
	public IJeiKeyMapping getToggleHideIngredient() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getToggleWildcardHideIngredient() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getHoveredClearSearchBar() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getPreviousSearch() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getNextSearch() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getCopyRecipeId() {
		return dummy;
	}
	
	@Override
	public IJeiKeyMapping getEscapeKey() {
		return escape;
	}
	
	@Override
	public IJeiKeyMapping getLeftClick() {
		return leftClick;
	}
	
	@Override
	public IJeiKeyMapping getRightClick() {
		return rightClick;
	}
	
	@Override
	public IJeiKeyMapping getEnterKey() {
		return enter;
	}
	
	@Override
	public IJeiKeyMapping getShareToChat() {
		return shareToChat;
	}
	
	@Override
	public IJeiKeyMappingInternal getShowBookmarkTooltipFeatures() {
		return controlOrCommand;
	}
	
	@Override
	public IJeiKeyMappingInternal getPauseRecipeCycling() {
		return leftShift;
	}
	
}
