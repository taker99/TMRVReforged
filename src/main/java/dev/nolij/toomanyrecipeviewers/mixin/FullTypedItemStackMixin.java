package dev.nolij.toomanyrecipeviewers.mixin;

//? if <21.1 {
/*import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;
*///?}
import dev.nolij.toomanyrecipeviewers.util.IItemStackish;
//? if >=21.1
import mezz.jei.common.ingredients.itemStacks.TypedItemStack;
//? if <21.1
//import mezz.jei.library.ingredients.itemStacks.TypedItemStack;
import net.minecraft.core.component.DataComponentPatch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//? if >=21.1
@Mixin(targets = "mezz/jei/common/ingredients/itemStacks/FullTypedItemStack", remap = false)
//? if <21.1
//@Mixin(targets = "mezz/jei/library/ingredients/itemStacks/FullTypedItemStack", remap = false)
public abstract class FullTypedItemStackMixin implements IItemStackish<TypedItemStack> {
	
	//? if <21.1 {
	/*@Shadow @Final private Holder<Item> itemHolder;
	
	@Override
	public Item tmrv$getItem() {
		return itemHolder.value();
	}
	*///?}
	
	@Shadow(aliases = "tag") @Final private DataComponentPatch dataComponentPatch;
	
	@Shadow @Final private int count;
	
	@Override
	public /*? if >=21.1 {*/ DataComponentPatch /*?} else {*/ /*CompoundTag *//*?}*/ tmrv$getDataComponentPatch() {
		return dataComponentPatch;
	}
	
	@Override
	public long tmrv$getAmount() {
		return count;
	}
	
}
