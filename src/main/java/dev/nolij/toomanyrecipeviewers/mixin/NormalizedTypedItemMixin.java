package dev.nolij.toomanyrecipeviewers.mixin;

//? if <21.1 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
*///?}
import dev.nolij.toomanyrecipeviewers.util.IItemStackish;
//? if >=21.1
import mezz.jei.common.ingredients.itemStacks.TypedItemStack;
//? if <21.1
//import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import org.spongepowered.asm.mixin.Mixin;

//? if >=21.1
@Mixin(targets = "mezz/jei/common/ingredients/itemStacks/NormalizedTypedItem", remap = false)
//? if <21.1
//@Mixin(targets = "mezz/jei/library/ingredients/itemStacks/NormalizedTypedItem", remap = false)
public abstract class NormalizedTypedItemMixin implements IItemStackish<TypedItemStack> {
	
	//? if <21.1 {
	/*@Shadow @Final private Holder<Item> itemHolder;
	
	@Override
	public Item tmrv$getItem() {
		return itemHolder.value();
	}
	*///?}
	
}
