package dev.nolij.toomanyrecipeviewers.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "giselle.jei_mekanism_multiblocks.client.jei.JEI_MekanismMultiblocks_JeiPlugin", remap = false)
public class JEMMJEIPluginMixin {

	@Redirect(
		method = "registerRecipes",
		at = @At(
			value = "FIELD",
			target = "Lgiselle/jei_mekanism_multiblocks/common/JEI_MekanismMultiblocks;EMILoaded:Z",
			opcode = Opcodes.GETSTATIC
		),
		require = 0
	)
	private boolean tmrv$registerRecipesWhenJemiIsSuppressed() {
		return false;
	}

}
