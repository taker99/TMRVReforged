"""Run the collector's actual ingredient methods against a small Java API stub."""
from pathlib import Path
import subprocess
import tempfile


root = Path(__file__).resolve().parents[1]
source = (root / "src/main/java/dev/nolij/toomanyrecipeviewers/impl/jei/api/gui/builder/TMRVIngredientCollector.java").read_text(encoding="utf-8")


def method(signature):
    start = source.index(signature)
    opening = source.index("{", start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end].replace("@Nullable ", "")


harness = """
import java.util.*;
class TMRVIngredientCollector {
    interface IIngredientType<I> {}
    interface IIngredientTypeWithSubtypes<B, I> extends IIngredientType<I> { B getBase(I value); Class<?> getIngredientClass(); }
    record ITypedIngredient<I>(I value) {}
    static class ItemStack {}
    static class Fluid {}
    static class FlowingFluid extends Fluid {
        final boolean source;
        FlowingFluid(boolean source) { this.source = source; }
        Object defaultFluidState() { return this; }
        boolean isSource(Object state) { return source; }
    }
    record FluidStack(FlowingFluid fluid, int amount, Object components) {}
    static class FluidType implements IIngredientTypeWithSubtypes<Fluid, FluidStack> {
        public Class<?> getIngredientClass() { return FluidStack.class; }
        public Fluid getBase(FluidStack stack) { return stack.fluid(); }
    }
    static class FluidHelper {
        final FluidType type = new FluidType();
        IIngredientTypeWithSubtypes<Fluid, ?> getFluidIngredientType() { return type; }
    }
    static class TooManyRecipeViewers { static final FluidHelper fluidHelper = new FluidHelper(); }
    static class VanillaTypes { static final IIngredientType<Object> ITEM_STACK = new IIngredientType<>() {}; }
    static class ErrorIngredient { static final ITypedIngredient<?> TYPED_INSTANCE = new ITypedIngredient<>("error"); }
    static class TypedItemStack {
        static ITypedIngredient<?> create(ItemStack stack) { return new ITypedIngredient<>(stack); }
    }
    static class TypedIngredient {
        static <I> ITypedIngredient<I> createAndFilterInvalid(Object manager, IIngredientType<I> type, I value, boolean normalize) {
            if (value == null || value instanceof FluidStack stack && stack.amount() <= 0 || value.equals("invalid")) return null;
            return new ITypedIngredient<>(value);
        }
    }
    final Object ingredientManager = new Object();
    final ArrayList<ITypedIngredient<?>> collectedIngredients = new ArrayList<>();
    %s
    %s
    public static void main(String[] args) {
        var type = TooManyRecipeViewers.fluidHelper.type;
        for (int variant = 0; variant < 20; variant++) {
            var collector = new TMRVIngredientCollector();
            var components = new Object();
            var still = new FluidStack(new FlowingFluid(true), 125 + variant, components);
            var flowing = new FluidStack(new FlowingFluid(false), 125 + variant, components);
            assert collector.addIngredients(type, List.of(flowing, still)) == collector;
            assert collector.collectedIngredients.size() == 1;
            assert collector.collectedIngredients.getFirst().value() == still;
            collector.addIngredient(type, flowing);
            assert collector.collectedIngredients.size() == 1;
            collector.addIngredient(type, null);
            collector.addIngredient(type, new FluidStack(new FlowingFluid(true), 0, components));
            assert collector.collectedIngredients.get(1) == ErrorIngredient.TYPED_INSTANCE;
            assert collector.collectedIngredients.get(2) == ErrorIngredient.TYPED_INSTANCE;
            var item = new ItemStack();
            collector.addIngredient(VanillaTypes.ITEM_STACK, item);
            assert collector.collectedIngredients.get(3).value() == item;
            collector.addIngredient(VanillaTypes.ITEM_STACK, "wrong type");
            assert collector.collectedIngredients.get(4) == ErrorIngredient.TYPED_INSTANCE;
            collector.addIngredient(new IIngredientType<String>() {}, "invalid");
            assert collector.collectedIngredients.get(5) == ErrorIngredient.TYPED_INSTANCE;
        }
        System.out.println("PASS: mixed fluid tags, direct flowing variants, source amounts/components, null/empty errors, items and custom errors");
    }
}
""" % (method("public <I> TMRVIngredientCollector addIngredient("), method("public <I> TMRVIngredientCollector addIngredients("))

with tempfile.TemporaryDirectory(prefix="tmrv-fluid-test-") as directory:
    java_file = Path(directory) / "TMRVIngredientCollector.java"
    java_file.write_text(harness, encoding="utf-8")
    subprocess.run(["javac", str(java_file)], check=True)
    subprocess.run(["java", "-ea", "-cp", directory, "TMRVIngredientCollector"], check=True)
