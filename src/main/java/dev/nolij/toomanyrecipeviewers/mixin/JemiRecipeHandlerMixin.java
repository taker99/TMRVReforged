package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.jemi.JemiRecipeHandler;
import dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers;
import dev.nolij.toomanyrecipeviewers.impl.recipe.TMRVRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link JemiRecipeHandler}'s {@code getRawRecipe} resolves non-JEMI recipes via
 * {@code RecipeManager.byKey(recipe.getId())}. TMRV recipe IDs are synthesized as
 * {@code toomanyrecipeviewers:/<namespace>/<path>}, so that lookup never resolves, and
 * {@code supportsRecipe} then rejects every TMRV recipe whose JEI RecipeType has a
 * recipe class - killing the recipe fill button for all JEI-plugin workstations.
 * <p>
 * TMRV already holds the original JEI recipe object, so hand that over directly.
 */
@Mixin(value = JemiRecipeHandler.class, remap = false)
public class JemiRecipeHandlerMixin {

	@Inject(method = "getRawRecipe", at = @At("HEAD"), cancellable = true)
	private void tmrv$getRawRecipe$HEAD(EmiRecipe emiRecipe, CallbackInfoReturnable<Object> cir) {
		if (!(emiRecipe instanceof TMRVRecipe<?>))
			return;

		final var runtime = TooManyRecipeViewers.runtime;
		if (runtime == null || runtime.recipeManager == null)
			return;

		final var jeiRecipe = runtime.recipeManager
			.category(emiRecipe.getCategory())
			.recipe(emiRecipe)
			.getJEIRecipe();

		if (jeiRecipe != null)
			cir.setReturnValue(jeiRecipe);
	}

}
