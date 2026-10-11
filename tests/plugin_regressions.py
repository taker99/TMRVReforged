"""Exercise plugin lifecycle and registry methods from the current Java sources."""
from pathlib import Path
import os
import re
import subprocess
import tempfile


root = Path(__file__).resolve().parents[1]
java_root = root / "src/main/java/dev/nolij/toomanyrecipeviewers"


def extract(file, signature):
    source = (java_root / file).read_text(encoding="utf-8")
    start = re.search(signature, source).start()
    opening = source.index("{", start)
    depth, end = 1, opening + 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end].replace("@Nullable ", "").replace("@NotNull ", "").replace("@Unmodifiable ", "")


harness = r"""
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
class PluginRegressions {
    static class System {
        static long time;
        static long currentTimeMillis() { return time; }
        static final java.io.PrintStream out = java.lang.System.out;
    }
    static class Logger { void error(Object... args) {} }
    static final Logger LOGGER = new Logger();
    static class ResourceLocation {
        final String id;
        ResourceLocation(String id) { this.id = id; }
        static ResourceLocation parse(String id) { return new ResourceLocation(id); }
    }
    enum PluginType { NORMAL, DISABLED, FORCE_PARTIAL_LOAD }
    enum DispatchStrategy { ASYNC, SYNC_EMI, SYNC_MAIN, SKIP }
    interface IModPlugin {
        default ResourceLocation getPluginUid() { return ResourceLocation.parse(getClass().getName()); }
        default void onConfigManagerAvailable(Object manager) {}
    }
    static boolean goodConfigured;
    static class BadPlugin implements IModPlugin {
        public BadPlugin() {}
        public void onConfigManagerAvailable(Object manager) { throw new IllegalStateException("bad plugin"); }
    }
    static class GoodPlugin implements IModPlugin {
        public GoodPlugin() {}
        public void onConfigManagerAvailable(Object manager) { goodConfigured = true; }
    }
    static class VanillaPlugin implements IModPlugin {}
    static class JeiInternalPlugin implements IModPlugin { public JeiInternalPlugin() {} }
    static class JemiPlugin implements IModPlugin {}
    static class TypeToken {
        static Class<?> getType(Class<?> type) { return type; }
    }
    record AnnotationData(Class<?> annotationType, String memberName) {}
    record ScanData(List<AnnotationData> annotations) {
        List<AnnotationData> getAnnotations() { return annotations; }
    }
    static class ModList {
        static final ModList INSTANCE = new ModList();
        List<ScanData> scans;
        static ModList get() { return INSTANCE; }
        List<ScanData> getAllScanData() { return scans; }
    }
    static class Refraction {
        Class<?> getClassOrNull(String name) {
            return switch (name) {
                case "missing" -> null;
                case "linkage" -> throw new NoClassDefFoundError("missing dependency");
                case "invalid" -> String.class;
                default -> GoodPlugin.class;
            };
        }
    }
    static final Refraction REFRACTION = new Refraction();
    static class Discovery {
        __DISCOVERY__
    }
    static class PluginInitializer {
        static final Object jeiConfigManager = new Object();
        static final List<Class<? extends IModPlugin>> pluginClasses = new ArrayList<>(List.of(BadPlugin.class, GoodPlugin.class));
        record LocatedPlugin(ResourceLocation id, IModPlugin plugin) {}
        static final List<LocatedPlugin> unfilteredPlugins = new ArrayList<>();
        static final VanillaPlugin vanillaPlugin = new VanillaPlugin();
        __INITIALIZER__
    }
    interface IIngredientType<T> {
        Class<?> getIngredientClass();
        String getUid();
    }
    static class Type<T> implements IIngredientType<T> {
        final Class<T> klass;
        Type(Class<T> klass) { this.klass = klass; }
        public Class<?> getIngredientClass() { return klass; }
        public String getUid() { return klass.getName(); }
    }
    interface IIngredientTypeWithSubtypes<B, I> extends IIngredientType<I> { Class<?> getIngredientBaseClass(); }
    record ItemStack(int id) {}
    static class FluidStack {}
    static class VanillaTypes { static final Type<ItemStack> ITEM_STACK = new Type<>(ItemStack.class); }
    static class FluidHelper {
        final Type<FluidStack> type = new Type<>(FluidStack.class);
        IIngredientType<FluidStack> getFluidIngredientType() { return type; }
    }
    static final FluidHelper fluidHelper = new FluidHelper();
    interface ITypedIngredient<T> { T getIngredient(); }
    interface EmiIngredient { List<EmiStack> getEmiStacks(); }
    static class EmiStack implements EmiIngredient, ITypedIngredient<Object> {
        static final EmiStack EMPTY = new EmiStack(null);
        final Object value;
        long amount = 1;
        EmiStack(Object value) { this.value = value; }
        public List<EmiStack> getEmiStacks() { return List.of(this); }
        public Object getIngredient() { return value; }
        boolean isEmpty() { return false; }
        EmiStack setAmount(long amount) { this.amount = amount; return this; }
        public boolean equals(Object other) { return other instanceof EmiStack stack && Objects.equals(value, stack.value); }
        public int hashCode() { return Objects.hashCode(value); }
    }
    static class FluidEmiStack extends EmiStack { FluidEmiStack(Object value) { super(value); } }
    static class ErrorEmiStack extends EmiStack { ErrorEmiStack() { super(null); } }
    static class ListEmiIngredient implements EmiIngredient {
        final List<? extends EmiIngredient> ingredients;
        ListEmiIngredient(EmiIngredient... ingredients) { this.ingredients = List.of(ingredients); }
        List<? extends EmiIngredient> getIngredients() { return ingredients; }
        public List<EmiStack> getEmiStacks() { return ingredients.stream().flatMap(i -> i.getEmiStacks().stream()).toList(); }
    }
    static class Helper {}
    record IngredientInfo<T>(IIngredientType<T> type) {
        IIngredientType<T> getIngredientType() { return type; }
    }
    record TypedIngredientUID(IIngredientType<?> type, String uid) {}
    record PluginFlag(boolean partialLoad) {}
    record Plugin(PluginFlag type) {}
    record ThreadContext(Plugin plugin) {}
    static class JEIPluginManager { static final ThreadLocal<ThreadContext> threadContext = new ThreadLocal<>(); }
    static class Component { static String translatable(String text) { return text; } }
    static class Registry {
        final List<EmiStack> stacks = new ArrayList<>();
        final List<String> aliases = new ArrayList<>();
        void addEmiStack(EmiStack stack) { stacks.add(stack); }
        void addAlias(EmiStack stack, String alias) { aliases.add(alias); }
    }
    static class Runtime { final Registry emiRegistry = new Registry(); }
    static class IngredientManager {
        final Runtime runtime = new Runtime();
        boolean locked;
        Collection<ItemStack> itemStacks = new ArrayList<>();
        Collection<FluidStack> fluidStacks = new ArrayList<>();
        final Map<IIngredientType<?>, IngredientInfo<?>> typeInfoMap = Collections.synchronizedMap(new HashMap<>());
        final Map<Class<?>, IIngredientType<?>> classTypeMap = Collections.synchronizedMap(new HashMap<>());
        final Map<Class<?>, IIngredientTypeWithSubtypes<?, ?>> baseClassTypeMap = Collections.synchronizedMap(new HashMap<>());
        final Map<String, IIngredientType<?>> typeUidLookup = Collections.synchronizedMap(new HashMap<>());
        final Map<IIngredientType<?>, List<ITypedIngredient<?>>> typedIngredients = Collections.synchronizedMap(new HashMap<>());
        final Map<TypedIngredientUID, Object> ingredientUidLookup = Collections.synchronizedMap(new HashMap<>());
        <V> EmiStack getEMIStack(IIngredientType<V> type, V value) { return new EmiStack(value); }
        EmiStack getEMIStack(ITypedIngredient<?> value) { return (EmiStack) value; }
        EmiStack getEMIStack(ItemStack value) { return new EmiStack(value); }
        EmiIngredient getEMIIngredient(Stream<ItemStack> values) { return new ListEmiIngredient(values.map(this::getEMIStack).toArray(EmiStack[]::new)); }
        ITypedIngredient<?> getTypedIngredient(EmiStack stack) { return stack; }
        <V> TypedIngredientUID getLegacyUid(IIngredientType<V> type, V value) { return new TypedIngredientUID(type, value.toString()); }
        <V> void registerIngredientBaseComparison(IIngredientType<V> type, V value) {}
        __INGREDIENT_METHODS__
    }
    enum RecipeIngredientRole { INPUT, OUTPUT, CATALYST, RENDER_ONLY }
    interface IFocus<T> {
        List<String> recipes();
        RecipeIngredientRole getRole();
        EmiStack getTypedValue();
    }
    record Focus(List<String> recipes, RecipeIngredientRole role, EmiStack value) implements IFocus<String> {
        Focus(List<String> recipes) { this(recipes, RecipeIngredientRole.INPUT, new EmiStack("input")); }
        public RecipeIngredientRole getRole() { return role; }
        public EmiStack getTypedValue() { return value; }
    }
    record IFocusGroup(List<IFocus<?>> focuses) {
        boolean isEmpty() { return focuses.isEmpty(); }
        List<IFocus<?>> getAllFocuses() { return focuses; }
    }
    static class FocusGroup { static final IFocusGroup EMPTY = new IFocusGroup(List.of()); }
    record EmiRecipeCategory(String id) {}
    interface EmiRecipe {
        default EmiRecipeCategory getCategory() { return null; }
        default List<EmiIngredient> getInputs() { return List.of(); }
        default List<EmiStack> getOutputs() { return List.of(); }
        default List<EmiIngredient> getCatalysts() { return List.of(); }
    }
    record BasicRecipe(List<EmiIngredient> inputs, List<EmiStack> outputs) implements EmiRecipe {
        public List<EmiIngredient> getInputs() { return inputs; }
        public List<EmiStack> getOutputs() { return outputs; }
    }
    static class EmiApi {
        static final EmiApi INSTANCE = new EmiApi();
        static EmiApi getRecipeManager() { return INSTANCE; }
        List<EmiIngredient> getWorkstations(EmiRecipeCategory category) { return List.of(new EmiStack("workstation")); }
    }
    record RecipeType<T>(Category<T> category) {
        ResourceLocation getUid() { return ResourceLocation.parse(category.emiCategory.id()); }
    }
    interface IRecipeCategory<T> { RecipeType<T> getRecipeType(); }
    static class ResourceLocationHolderComparator {
        static <T> Comparator<T> create(java.util.function.Function<T, ResourceLocation> key) { return Comparator.comparing(x -> key.apply(x).id); }
    }
    static class Category<T> implements IRecipeCategory<T> {
        static class Recipe {
            final boolean registered;
            final EmiRecipe emiRecipe;
            Recipe(boolean registered) { this(registered, new BasicRecipe(List.of(new EmiStack("input")), List.of(new EmiStack("output")))); }
            Recipe(boolean registered, EmiRecipe emiRecipe) { this.registered = registered; this.emiRecipe = emiRecipe; }
            EmiRecipe getEMIRecipe() { return emiRecipe; }
        }
        final EmiRecipeCategory emiCategory;
        Category() { this("category"); }
        Category(String id) { emiCategory = new EmiRecipeCategory(id); }
        final Map<T, Recipe> jeiRecipeMap = Collections.synchronizedMap(new IdentityHashMap<>());
        Recipe recipe(T value) { return jeiRecipeMap.get(value); }
        EmiRecipeCategory getEMICategory() { return emiCategory; }
        IRecipeCategory<T> getJEICategory() { return this; }
        public RecipeType<T> getRecipeType() { return new RecipeType<>(this); }
    }
    static class RecipeManager {
        final IngredientManager ingredientManager = new IngredientManager();
        final Set<EmiRecipe> hiddenRecipes = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<EmiRecipeCategory> hiddenCategories = Collections.newSetFromMap(new IdentityHashMap<>());
        final Set<Object> replacedRecipeIDs = new HashSet<>(), replacementRecipes = new HashSet<>();
        final List<IRecipeCategory<?>> jeiRecipeCategories = new ArrayList<>();
        List<RecipeType<?>> indexedTypes = List.of();
        boolean locked = true;
        <T> Category<T> category(RecipeType<T> type) { return type.category(); }
        Stream<RecipeType<?>> getRecipeTypes(IFocusGroup focuses) { return indexedTypes.stream(); }
        <T> Stream<T> getRecipes(Category<T> category, IFocus<?> focus) { return (Stream<T>) focus.recipes().stream(); }
        __LOOKUP__
        __VISIBILITY__
    }
    record SlotWidget(EmiIngredient stack, int x, int y) {}
    static class CraftingFallback {
        final List<EmiIngredient> emiInputs;
        CraftingFallback(List<EmiIngredient> inputs) { emiInputs = inputs; }
        __INPUT_WIDGET__
    }
    interface IJeiBrewingRecipe {
        List<ItemStack> getPotionInputs();
        List<ItemStack> getIngredients();
        ItemStack getPotionOutput();
    }
    record BrewingRecipe(List<ItemStack> potionInputs) implements IJeiBrewingRecipe {
        public List<ItemStack> getPotionInputs() { return potionInputs; }
        public List<ItemStack> getIngredients() { return List.of(new ItemStack(3)); }
        public ItemStack getPotionOutput() { return new ItemStack(4); }
    }
    static class EmiBrewingRecipe implements EmiRecipe {
        final EmiStack input;
        EmiBrewingRecipe(EmiStack input, EmiIngredient ingredient, EmiStack output, ResourceLocation id) { this.input = input; }
    }
    static class TMRVRecipe<T> implements EmiRecipe {
        final T jeiRecipe;
        TMRVRecipe(Object runtime, Object category, T recipe, ResourceLocation id) { jeiRecipe = recipe; }
    }
    static class BrewingCategory {
        final Object runtime = new Object();
        final IngredientManager ingredientManager = new IngredientManager();
        record Extension(Object plugin) {}
        Extension getBrewingCategoryExtension(IJeiBrewingRecipe recipe) { return null; }
        class Recipe {
            final IJeiBrewingRecipe jeiRecipe;
            Recipe(IJeiBrewingRecipe recipe) { jeiRecipe = recipe; }
            ResourceLocation getID() { return ResourceLocation.parse("brew"); }
            __BREWING__
        }
    }
    static class Slot {
        __DISPLAY__
    }
    static class Tank {
        final IngredientManager ingredientManager = new IngredientManager();
        final EmiIngredient ingredient;
        Tank(EmiIngredient ingredient) { this.ingredient = ingredient; }
        EmiIngredient getStack() { return ingredient; }
        __TANK_DISPLAY__
    }
    static class Screen {}
    record Area(int left, int top, int right, int bottom) {
        boolean contains(int x, int y) { return x >= left && x < right && y >= top && y < bottom; }
    }
    interface IGhostIngredientHandler<T> {
        interface Target<I> { Area getArea(); void accept(I value); }
        <I> List<Target<I>> getTargetsTyped(T screen, ITypedIngredient<I> ingredient, boolean start);
        void onComplete();
    }
    static class ScreenHelper {
        final List<IGhostIngredientHandler<Screen>> handlers = new ArrayList<>();
        <T> List<IGhostIngredientHandler<Screen>> getGhostIngredientHandlers(Screen screen) { return handlers; }
    }
    static class DropHandler {
        final ScreenHelper screenHelper = new ScreenHelper();
        __DROP__
    }
    static class GhostHandler implements IGhostIngredientHandler<Screen> {
        final Area area;
        final boolean fail;
        final List<String> events = new ArrayList<>();
        GhostHandler(Area area, boolean fail) { this.area = area; this.fail = fail; }
        public <I> List<Target<I>> getTargetsTyped(Screen screen, ITypedIngredient<I> ingredient, boolean start) {
            return List.of(new Target<>() {
                public Area getArea() { return area; }
                public void accept(I value) { events.add("accept:" + value); if (fail) throw new IllegalStateException(); }
            });
        }
        public void onComplete() { events.add("complete"); }
    }
    static void checkLookup() {
        var category = new Category<String>(); category.jeiRecipeMap.put("one", new Category.Recipe(true)); category.jeiRecipeMap.put("two", new Category.Recipe(true));
        category.jeiRecipeMap.put("rejected", new Category.Recipe(false));
        var manager = new RecipeManager();
        assert manager.getRecipes(category, new IFocusGroup(List.of())).collect(Collectors.toSet()).equals(Set.of("one", "two"));
        assert manager.getRecipes(category, new IFocusGroup(List.of(new Focus(List.of("two")), new Focus(List.of("two"))))).toList().equals(List.of("two"));
        assert manager.getRecipes(new Category<String>(), new IFocusGroup(List.of())).count() == 0;
    }
    static void checkDiscovery() {
        ModList.get().scans = List.of(new ScanData(List.of(
            new AnnotationData(IModPlugin.class, "missing"), new AnnotationData(IModPlugin.class, "linkage"),
            new AnnotationData(IModPlugin.class, "invalid"), new AnnotationData(IModPlugin.class, "good"),
            new AnnotationData(String.class, "ignored"))));
        assert Discovery.getInstances(IModPlugin.class, IModPlugin.class).equals(List.of(GoodPlugin.class));
    }
    static void checkConverters() {
        for (int size : List.of(0, 2, 9)) {
            var inputs = IntStream.range(0, size).mapToObj(i -> (EmiIngredient) new EmiStack(i)).toList();
            var crafting = new CraftingFallback(inputs);
            for (int slot = 0; slot < 9; slot++) {
                assert crafting.getInputWidget(slot, 10, 20).stack() == (slot < size ? inputs.get(slot) : EmiStack.EMPTY);
            }
        }
        var category = new BrewingCategory();
        var single = new BrewingRecipe(List.of(new ItemStack(1)));
        assert ((EmiBrewingRecipe) category.new Recipe(single).convertEMIBrewingRecipe()).input.value.equals(new ItemStack(1));
        var alternatives = new BrewingRecipe(List.of(new ItemStack(1), new ItemStack(2)));
        var fallback = (TMRVRecipe<?>) category.new Recipe(alternatives).convertEMIBrewingRecipe();
        assert fallback.jeiRecipe == alternatives;
        assert fallback.getOutputs().size() == 1 && fallback.getOutputs().getFirst().amount == 3;
        assert fallback.getOutputs().getFirst().value.equals(alternatives.getPotionOutput());
    }
    static void checkVisibility() {
        var category = new Category<String>("visible");
        var one = new Category.Recipe(true);
        var two = new Category.Recipe(true);
        category.jeiRecipeMap.put("one", one); category.jeiRecipeMap.put("two", two);
        category.jeiRecipeMap.put("rejected", new Category.Recipe(false));
        var manager = new RecipeManager(); manager.jeiRecipeCategories.add(category);
        manager.hiddenRecipes.add(two.getEMIRecipe());
        var type = category.getRecipeType();
        assert manager.getRecipesStream(type, FocusGroup.EMPTY, false).toList().equals(List.of("one"));
        assert manager.getRecipesStream(type, FocusGroup.EMPTY, true).collect(Collectors.toSet()).equals(Set.of("one", "two"));
        var beforeBake = new IFocusGroup(List.of(new Focus(List.of("one", "two"))));
        assert manager.getRecipesStream(type, beforeBake, false).toList().equals(List.of("one"));
        var afterBake = new IFocusGroup(List.of(new Focus(List.of("one"))));
        assert manager.getRecipesStream(type, afterBake, true).collect(Collectors.toSet()).equals(Set.of("one", "two"));
        manager.recipesBaked();
        assert manager.hiddenRecipes.contains(two.getEMIRecipe());
        assert manager.getRecipesStream(type, afterBake, false).toList().equals(List.of("one"));
        assert manager.getRecipesStream(type, afterBake, true).collect(Collectors.toSet()).equals(Set.of("one", "two"));
        for (var role : RecipeIngredientRole.values()) {
            String value = switch (role) { case INPUT -> "input"; case OUTPUT -> "output"; case CATALYST -> "workstation"; default -> "input"; };
            var focus = new IFocusGroup(List.of(new Focus(List.of(), role, new EmiStack(value))));
            assert manager.getRecipesStream(type, focus, true).toList().equals(role == RecipeIngredientRole.RENDER_ONLY ? List.of() : List.of("two"));
        }
        var unmatched = new IFocusGroup(List.of(new Focus(List.of(), RecipeIngredientRole.INPUT, new EmiStack("unrelated"))));
        assert manager.getRecipesStream(type, unmatched, true).count() == 0;
        manager.hiddenCategories.add(category.getEMICategory());
        assert manager.getRecipesStream(type, FocusGroup.EMPTY, false).count() == 0;
        assert manager.getRecipesStream(type, afterBake, false).count() == 0;
        assert manager.getRecipesStream(type, new IFocusGroup(List.of(new Focus(List.of()))), true).count() == 2;
        manager.recipesBaked(); assert manager.hiddenCategories.contains(category.getEMICategory());
        assert manager.getRecipeCategoriesUncached(List.of(), FocusGroup.EMPTY, false).count() == 0;
        assert manager.getRecipeCategoriesUncached(List.of(), FocusGroup.EMPTY, true).toList().equals(List.of(category));
        assert manager.getRecipeCategoriesUncached(List.of(), afterBake, false).count() == 0;
        assert manager.getRecipeCategoriesUncached(List.of(), afterBake, true).toList().equals(List.of(category));
        assert manager.getRecipeCategoriesUncached(List.of(), unmatched, true).count() == 0;
        assert manager.getRecipeCategoriesUncached(List.of(new Category<String>("other")), afterBake, true).count() == 0;
        manager.indexedTypes = List.of(type);
        assert manager.getRecipeCategoriesUncached(List.of(), afterBake, true).toList().equals(List.of(category));
    }
    static void checkRegistry() throws Exception {
        var manager = new IngredientManager();
        manager.registerIngredientType(new IngredientInfo<>(VanillaTypes.ITEM_STACK), List.of());
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(8);
        try {
            var futures = new ArrayList<Future<?>>();
            for (int thread = 0; thread < 8; thread++) {
                final int index = thread;
                futures.add(pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < 500; i++) {
                        manager.registerIngredients(VanillaTypes.ITEM_STACK, List.of(new ItemStack(index * 500 + i)));
                        manager.addAlias(new EmiStack(i), index + ":" + i);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (var future : futures) future.get();
        } finally { pool.shutdownNow(); }
        assert manager.runtime.emiRegistry.stacks.size() == 4000;
        assert manager.runtime.emiRegistry.aliases.size() == 4000;
        assert new HashSet<>(manager.runtime.emiRegistry.aliases).size() == 4000;
        assert manager.getAllIngredients(VanillaTypes.ITEM_STACK).size() == 4000;
        assert manager.getAllTypedIngredients(VanillaTypes.ITEM_STACK).size() == 4000;
        var snapshot = manager.getAllIngredients(VanillaTypes.ITEM_STACK);
        manager.registerIngredients(VanillaTypes.ITEM_STACK, List.of(new ItemStack(4001)));
        assert snapshot.size() == 4000;
        try { snapshot.clear(); throw new AssertionError("mutable snapshot"); } catch (UnsupportedOperationException expected) {}
        try { manager.registerIngredientType(new IngredientInfo<>(VanillaTypes.ITEM_STACK), List.of()); throw new AssertionError("duplicate type"); } catch (IllegalStateException expected) {}
        manager.locked = true;
        try { manager.registerIngredients(VanillaTypes.ITEM_STACK, List.of()); throw new AssertionError("late ingredient"); } catch (IllegalStateException expected) {}
        try { manager.addAlias(new EmiStack(1), "late"); throw new AssertionError("late alias"); } catch (IllegalStateException expected) {}
    }
    static void checkDisplay() {
        var manager = new IngredientManager();
        var a = new EmiStack("a"); var b = new EmiStack("b"); var c = new EmiStack("c");
        var grouped = new ListEmiIngredient(new ListEmiIngredient(a, b), c);
        System.time = 0; assert Slot.getDisplayedIngredient(manager, grouped).orElseThrow().getIngredient().equals("a");
        System.time = 1000; assert Slot.getDisplayedIngredient(manager, grouped).orElseThrow().getIngredient().equals("c");
        System.time = 2000; assert Slot.getDisplayedIngredient(manager, grouped).orElseThrow().getIngredient().equals("a");
        assert Slot.getDisplayedIngredient(manager, new ListEmiIngredient()).isEmpty();
        assert new Tank(new ListEmiIngredient(a, new FluidEmiStack("water"), new FluidEmiStack("lava"))).getDisplayedIngredient().orElseThrow().getIngredient().equals("water");
        assert new Tank(new ListEmiIngredient()).getDisplayedIngredient().isEmpty();
    }
    static void checkDrop() {
        var drop = new DropHandler(); var screen = new Screen();
        var first = new GhostHandler(new Area(0, 0, 10, 10), false);
        var second = new GhostHandler(new Area(10, 10, 20, 20), false);
        drop.screenHelper.handlers.addAll(List.of(first, second));
        assert !drop.drop(screen, Optional.empty(), 15, 15);
        assert drop.drop(screen, Optional.of(new EmiStack("item")), 15, 15);
        assert first.events.isEmpty() && second.events.equals(List.of("accept:item", "complete"));
        assert !drop.drop(screen, Optional.of(new EmiStack("item")), 30, 30);
        var failing = new GhostHandler(new Area(30, 30, 40, 40), true); drop.screenHelper.handlers.add(failing);
        try { drop.drop(screen, Optional.of(new EmiStack("item")), 35, 35); throw new AssertionError("failed drop accepted"); } catch (IllegalStateException expected) {}
        assert failing.events.equals(List.of("accept:item", "complete"));
    }
    public static void main(String[] args) throws Exception {
        assert PluginInitializer.unfilteredPlugins.size() == 4 && goodConfigured;
        checkLookup(); checkRegistry(); checkDisplay(); checkDrop(); checkDiscovery(); checkConverters(); checkVisibility();
        System.out.println("PASS: plugin discovery/callback isolation, crafting boundaries, brewing alternatives, hidden/focused lookups, parallel registry writes, displayed alternatives and ghost drop completion");
    }
}
"""

manager = "impl/jei/api/runtime/IngredientManager.java"
harness = harness.replace("__INGREDIENT_METHODS__", "\n".join(extract(manager, signature) for signature in (
    r"private (?:synchronized )?<V> void registerIngredients\(",
    r"private (?:synchronized )?<V> void registerIngredientType\(",
    r"public (?:synchronized )?<I> void addAlias\(ITypedIngredient",
    r"public (?:synchronized )?@Unmodifiable <V> Collection<ITypedIngredient<V>> getAllTypedIngredients\(",
    r"public (?:synchronized )?@Unmodifiable <V> Collection<V> getAllIngredients\(",
)))
harness = harness.replace("__INITIALIZER__", extract("compat/plugin/JEIPluginManager.java", r"static \{\s*pluginClasses.remove"))
harness = harness.replace("__DISCOVERY__", extract("compat/plugin/JEIPluginManager.java", r"private static <T> List<Class<\? extends T>> getInstances\(").replace("Type.getType", "TypeToken.getType"))
harness = harness.replace("__LOOKUP__", extract("impl/jei/api/recipe/RecipeManager.java", r"private <T> Stream<T> getRecipes\(Category<T> category, IFocusGroup focusGroup\)"))
harness = harness.replace("__VISIBILITY__", "\n".join(extract("impl/jei/api/recipe/RecipeManager.java", signature) for signature in (
    r"private <T> Stream<T> getRecipesStream\(",
    r"private boolean matchesFocus\(",
    r"private Stream<IRecipeCategory<\?>> getRecipeCategoriesUncached\(",
    r"public void recipesBaked\(",
)))
harness = harness.replace("__INPUT_WIDGET__", extract("impl/jei/api/recipe/RecipeManager.java", r"public SlotWidget getInputWidget\("))
harness = harness.replace("__BREWING__", extract("impl/jei/api/recipe/RecipeManager.java", r"private @NotNull Emi(?:Brewing)?Recipe convertEMIBrewingRecipe\(").replace("Category.this", "BrewingCategory.this"))
harness = harness.replace("__DISPLAY__", extract("impl/jei/api/gui/ingredient/TMRVSlotWidget.java", r"static Optional<ITypedIngredient<\?>> getDisplayedIngredient\("))
harness = harness.replace("__TANK_DISPLAY__", extract("impl/jei/api/gui/ingredient/TMRVTankWidget.java", r"public Optional<ITypedIngredient<\?>> getDisplayedIngredient\("))
harness = harness.replace("__DROP__", extract("compat/plugin/TMRVDragDropHandler.java", r"private <I> boolean drop\("))

with tempfile.TemporaryDirectory(prefix="tmrv-plugin-test-") as directory:
    java_file = Path(directory) / "PluginRegressions.java"
    java_file.write_text(harness, encoding="utf-8")
    subprocess.run(["javac", str(java_file)], check=True)
    subprocess.run(["java", "-ea", "-cp", directory, "PluginRegressions"], check=True)

properties = dict(line.split("=", 1) for line in (root / "gradle.properties").read_text(encoding="utf-8").splitlines() if "=" in line and not line.startswith("#"))
properties = {key.strip(): value.strip() for key, value in properties.items()}
cache = Path(os.environ.get("GRADLE_USER_HOME", Path.home() / ".gradle")) / "caches/modules-2/files-2.1/dev.nolij"
dependencies = []
for name in ("zson", "libnolij"):
    version = properties[f"{name}_version"]
    candidates = list((cache / name / version).rglob(f"{name}-{version}.jar"))
    assert len(candidates) == 1, (name, candidates)
    dependencies.append(str(candidates[0]))

config = (java_root / "compat/plugin/TMRVPluginConfig.java").read_text(encoding="utf-8")
config = re.sub(r"^package .*;\s*", "", config)
config = re.sub(r"^import (?:net.minecraft|org.jetbrains|static dev.nolij.toomanyrecipeviewers).*;\s*", "", config, flags=re.MULTILINE)
config = config.replace("@Nullable ", "").replace("@NotNull ", "")
config = config.replace("public class TMRVPluginConfig {", "public class TMRVPluginConfig { static final ConfigChecks.Logger LOGGER = new ConfigChecks.Logger();")
config_checks = r"""
import java.nio.file.*;
class ConfigChecks {
    static class Logger { void error(Object... args) {} }
    public static void main(String[] args) throws Exception {
        var directory = Path.of(args[0]);
        var missing = directory.resolve("missing.json5");
        TMRVPluginConfig.readOrCreate(missing.toFile()); assert Files.isRegularFile(missing);
        var invalid = directory.resolve("invalid.json5");
        var broken = "{ broken:"; Files.writeString(invalid, broken);
        var defaults = TMRVPluginConfig.readOrCreate(invalid.toFile());
        assert defaults.pluginPhaseOverrides.isEmpty() && Files.readString(invalid).equals(broken);
        var valid = directory.resolve("valid.json5");
        var text = "{ allowDefaultAsync: false, blockedEMIPluginModIDs: ['test'] }"; Files.writeString(valid, text);
        var configured = TMRVPluginConfig.readOrCreate(valid.toFile());
        assert Boolean.FALSE.equals(configured.allowDefaultAsync) && configured.blockedEMIPluginModIDs.contains("test");
        assert Files.readString(valid).equals(text);
        var unreadable = directory.resolve("directory.json5"); Files.createDirectory(unreadable);
        assert TMRVPluginConfig.readOrCreate(unreadable.toFile()) != null && Files.isDirectory(unreadable);
        assert TMRVPluginConfig.readOrCreate(null) != null;
        System.out.println("PASS: real ZSON parsing, existing valid/invalid/unreadable configs preserved, missing config created");
    }
}
class ResourceLocation {
    static ResourceLocation parse(String text) { return new ResourceLocation(); }
}
enum PluginType { NORMAL, DISABLED, FORCE_PARTIAL_LOAD }
enum DispatchStrategy { ASYNC, SYNC_EMI, SYNC_MAIN, SKIP }
"""
with tempfile.TemporaryDirectory(prefix="tmrv-config-test-") as directory:
    config_file = Path(directory) / "TMRVPluginConfig.java"
    checks_file = Path(directory) / "ConfigChecks.java"
    config_file.write_text(config, encoding="utf-8")
    checks_file.write_text(config_checks, encoding="utf-8")
    classpath = os.pathsep.join(dependencies)
    subprocess.run(["javac", "-cp", classpath, str(config_file), str(checks_file)], check=True)
    subprocess.run(["java", "-ea", "-cp", os.pathsep.join((directory, classpath)), "ConfigChecks", directory], check=True)
