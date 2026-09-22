package dev.nolij.toomanyrecipeviewers.mixin;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.screen.RecipeDisplay;
import dev.nolij.toomanyrecipeviewers.client.recipe.TMRVRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = RecipeDisplay.class, remap = false)
public class RecipeDisplayMixin {

	@Shadow @Final private EmiRecipe recipe;

	@ModifyArgs(method = "addButtons", at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/widget/RecipeFillButtonWidget;<init>(IILdev/emi/emi/api/recipe/EmiRecipe;)V"))
	private void tmrv$moveRecipeTransferButton(Args args) {
		if (recipe instanceof TMRVRecipe<?> tmrvRecipe && tmrvRecipe.hasRecipeTransferPosition()) {
			args.set(0, tmrvRecipe.getRecipeTransferX());
			args.set(1, tmrvRecipe.getRecipeTransferY());
		}
	}

}
