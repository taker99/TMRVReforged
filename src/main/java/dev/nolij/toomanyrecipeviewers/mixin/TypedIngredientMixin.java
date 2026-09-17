package dev.nolij.toomanyrecipeviewers.mixin;

//? if <21.1 {
/*import java.util.Optional;
*///?}
import dev.nolij.toomanyrecipeviewers.client.ingredient.ErrorIngredient;
import dev.nolij.toomanyrecipeviewers.client.ingredient.TMRVStack;
import dev.nolij.toomanyrecipeviewers.util.IStackish;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
//? if >=21.1
import mezz.jei.common.ingredients.TypedIngredientUtil;
//? if <21.1
//import mezz.jei.library.ingredients.TypedIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=21.1
@Mixin(value = TypedIngredientUtil.class, remap = false)
//? if <21.1
//@Mixin(value = TypedIngredient.class, remap = false)
public class TypedIngredientMixin {

	//? if >=21.1 {
	@Inject(method = "checkTypedIngredientFromApi(Lmezz/jei/api/ingredients/ITypedIngredient;)Lmezz/jei/api/ingredients/ITypedIngredient;", at = @At("HEAD"), cancellable = true)
	private static <T> void tmrv$checkTypedIngredientFromApi$HEAD(ITypedIngredient<T> value, CallbackInfoReturnable<ITypedIngredient<T>> cir) {
		if (value instanceof IStackish<?> || value instanceof TMRVStack<?> || value == ErrorIngredient.TYPED_INSTANCE) {
			cir.setReturnValue(value);
		}
	}
	//?}
	
	// somehow increases memory usage??
//	@Inject(method = "normalize", at = @At("HEAD"), cancellable = true)
//	private static <T> void tmrv$normalize$HEAD(ITypedIngredient<T> typedIngredient, IIngredientHelper<T> ingredientHelper, CallbackInfoReturnable<ITypedIngredient<T>> cir) {
//		if (typedIngredient instanceof IStackish<?> stackish) {
//			//noinspection unchecked
//			cir.setReturnValue((ITypedIngredient<T>) stackish.tmrv$normalize());
//		}
//	}
	
	//? if >=21.1
	@Inject(method = "checkTypedIngredientFromApi(Lmezz/jei/api/runtime/IIngredientManager;Lmezz/jei/api/ingredients/ITypedIngredient;)Lmezz/jei/api/ingredients/ITypedIngredient;", at = @At("HEAD"), cancellable = true)
	//? if <21.1
	//@Inject(method = "defensivelyCopyTypedIngredientFromApi", at = @At("HEAD"), cancellable = true)
	private static <T> void tmrv$defensivelyCopyTypedIngredientFromApi$HEAD(IIngredientManager ingredientManager, ITypedIngredient<T> value, CallbackInfoReturnable<ITypedIngredient<T>> cir) {
		if (value instanceof IStackish<?> || value instanceof TMRVStack<?> || value == ErrorIngredient.TYPED_INSTANCE) {
			cir.setReturnValue(value);
		}
	}
	
}
