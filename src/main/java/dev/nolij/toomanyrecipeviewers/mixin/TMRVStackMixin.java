package dev.nolij.toomanyrecipeviewers.mixin;

import dev.nolij.toomanyrecipeviewers.client.ingredient.TMRVStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
//? if >=21.1
import mezz.jei.common.ingredients.TypedIngredient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// yes, a self-mixin is necessary; `ITypedIngredient<T>` "clashes" with `EmiStack`
// (but the JVM doesn't care, so self-mixin go brr)
@SuppressWarnings("NonExtendableApiUsage")
@Mixin(value = TMRVStack.class, remap = false)
public class TMRVStackMixin<T> implements ITypedIngredient<T> {
	
	@Shadow @Final public IIngredientType<T> type;
	@Shadow @Final public T ingredient;
	
	@Override
	public IIngredientType<T> getType() {
		return type;
	}
	
	@Override
	public T getIngredient() {
		return ingredient;
	}

	//? if >=21.1 {
	@Override
	public ITypedIngredient<T> normalize(IIngredientHelper<T> ingredientHelper) {
		return TypedIngredient.createUnvalidated(type, ingredientHelper.normalizeIngredient(ingredient));
	}
	//?}
	
	@Override
	public @Nullable <V> ITypedIngredient<V> cast(IIngredientType<V> otherType) {
		//noinspection unchecked
		return type.equals(otherType) ? (ITypedIngredient<V>) this : null;
	}
	
}
