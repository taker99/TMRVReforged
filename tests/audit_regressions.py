"""Exercise actual Java methods with API stubs; optionally pass a Git revision."""
from pathlib import Path
import subprocess
import sys
import tempfile


root = Path(__file__).resolve().parents[1]
java_root = Path("src/main/java/dev/nolij/toomanyrecipeviewers")


def read_source(file):
    path = java_root / file
    if len(sys.argv) > 1:
        return subprocess.check_output(
            ["rtk", "proxy", "git", "show", f"{sys.argv[1]}:{path.as_posix()}"],
            cwd=root, text=True, encoding="utf-8",
        )
    return (root / path).read_text()


def methods(file, *signatures):
    source = read_source(file)
    result = []
    for signature in signatures:
        start = source.index(signature)
        opening = source.index("{", start)
        depth, end = 1, opening + 1
        while depth:
            depth += (source[end] == "{") - (source[end] == "}")
            end += 1
        result.append(source[start:end])
    return "\n".join(result)


harness = r"""
import java.util.*;
import java.util.function.Consumer;
import com.google.gson.*;
class AuditRegressions {
    static final Logger LOGGER = new Logger();
    static class Logger { void error(Object... args) {} }
    enum UidContext { Ingredient }
    interface IIngredientType<T> { String getUid(); }
    interface IIngredientTypeWithSubtypes<B, I> extends IIngredientType<I> { I getDefaultIngredient(B base); }
    record Type<T>(String uid) implements IIngredientType<T> { public String getUid() { return uid; } }
    interface ITypedIngredient<T> { T getIngredient(); }
    record TypedIngredient<T>(T value) implements ITypedIngredient<T> {
        public T getIngredient() { return value; }
        static <T> ITypedIngredient<T> createUnvalidated(IIngredientType<T> type, T value) { return new TypedIngredient<>(value); }
    }
    static class Fluid {}
    record FluidStack(Fluid fluid) {}
    static class FluidType implements IIngredientTypeWithSubtypes<Fluid, FluidStack> {
        public String getUid() { return "fluid"; }
        public FluidStack getDefaultIngredient(Fluid fluid) { return new FluidStack(fluid); }
    }
    static class FluidHelper {
        final FluidType type = new FluidType();
        IIngredientType getFluidIngredientType() { return type; }
    }
    static final FluidHelper fluidHelper = new FluidHelper();
    static class VanillaTypes { static final IIngredientType<String> ITEM_STACK = new Type<>("item"); }
    static class Helper {
        <T> T copyIngredient(T value) { return (T) new String(value.toString()); }
        boolean isValidIngredient(Object value) { return !"invalid".equals(value); }
        boolean isIngredientOnServer(Object value) { return !"absent".equals(value); }
        String getErrorInfo(Object value) { return String.valueOf(value); }
        String getUniqueId(Object value, UidContext context) { return value.toString(); }
    }
    static class EmiStack {
        static final EmiStack EMPTY = new EmiStack();
        long amount = 1;
        float chance = 1;
        Object comparison = "default";
        EmiStack remainder;
        long getAmount() { return amount; }
        float getChance() { return chance; }
        EmiStack getRemainder() { return remainder == null ? EMPTY : remainder; }
        EmiStack setAmount(long value) { amount = value; return this; }
        EmiStack setChance(float value) { chance = value; return this; }
        EmiStack setRemainder(EmiStack value) { remainder = value; return this; }
        EmiStack copy() { return this == EMPTY ? EMPTY : new EmiStack().setAmount(amount); }
        boolean isEmpty() { return this == EMPTY; }
    }
    static class TMRVStack<T> extends EmiStack implements ITypedIngredient<T> {
        final IIngredientType<T> type;
        final Helper helper;
        final Object renderer;
        final T ingredient;
        TMRVStack(IIngredientType<T> type, Helper helper, Object renderer, T ingredient) {
            this.type = type; this.helper = helper; this.renderer = renderer; this.ingredient = ingredient;
        }
        public T getIngredient() { return ingredient; }
        __COPY__
    }
    record TypedIngredientUID(IIngredientType<?> type, String uid) {}
    interface IIngredientListener { void onIngredientsAdded(Helper helper, List<? extends ITypedIngredient<?>> values); }
    static class Info {
        final Codec<Object> codec;
        Info(Codec<Object> codec) { this.codec = codec; }
        Helper getIngredientHelper() { return new Helper(); }
        Codec<Object> getIngredientCodec() { return codec; }
    }
    static class IngredientManager {
        final Map<TypedIngredientUID, Object> ingredientUidLookup = new HashMap<>();
        final Map<String, IIngredientType<?>> types = new HashMap<>();
        final Map<IIngredientType<?>, Info> infos = new HashMap<>();
        final List<IIngredientListener> listeners = new ArrayList<>();
        final List<Object> registered = new ArrayList<>();
        final List<String> aliases = new ArrayList<>();
        boolean locked;
        <V> Info getIngredientInfo(IIngredientType<V> type) { return infos.get(type); }
        Helper getIngredientHelper(IIngredientType<?> type) { return new Helper(); }
        Codec<Object> getIngredientCodec(IIngredientType<?> type) { return infos.get(type).codec; }
        <T> IIngredientType<T> getIngredientType(String uid) { return (IIngredientType<T>) types.get(uid); }
        <V> void registerIngredients(IIngredientType<V> type, Collection<V> values) { registered.addAll(values); }
        <V> EmiStack getEMIStack(IIngredientType<V> type, V value) {
            return value == null ? EmiStack.EMPTY : new TMRVStack<>(type, new Helper(), null, value);
        }
        <I> void addAlias(IIngredientType<I> type, I ingredient, String alias) {
            assert type != fluidHelper.type || ingredient instanceof FluidStack : "fluid aliases must use FluidStack";
            aliases.add(alias);
        }
        <I> void addAliases(IIngredientType<I> type, I ingredient, Collection<String> aliases) {
            for (String alias : aliases) addAlias(type, ingredient, alias);
        }
        __MANAGER__
    }
    static class TooManyRecipeViewers { final IngredientManager ingredientManager = new IngredientManager(); }
    static class JsonOps { static final JsonOps INSTANCE = new JsonOps(); }
    record DataResult<T>(T value) {
        Optional<T> resultOrPartial(Consumer<String> error) { return Optional.ofNullable(value); }
    }
    static class Codec<T> {
        DataResult<JsonElement> encodeStart(JsonOps ops, T value) { return new DataResult<>(new JsonPrimitive(value.toString())); }
        DataResult<Object> parse(JsonOps ops, JsonElement value) { return new DataResult<>(value.getAsString()); }
    }
    static class GsonHelper {
        static String getAsString(JsonObject object, String name) { return object.get(name).getAsString(); }
        static JsonElement getNonNull(JsonObject object, String name) { return Objects.requireNonNull(object.get(name)); }
        static boolean isValidNode(JsonObject object, String name) { return object.has(name) && !object.get(name).isJsonNull(); }
        static long getAsLong(JsonObject object, String name, long fallback) { return object.has(name) ? object.get(name).getAsLong() : fallback; }
        static float getAsFloat(JsonObject object, String name, float fallback) { return object.has(name) ? object.get(name).getAsFloat() : fallback; }
    }
    interface EmiIngredient {}
    static class EmiIngredientSerializer {
        static JsonElement getSerialized(EmiStack stack) { return new JsonPrimitive(stack.amount); }
        static Object getDeserialized(JsonElement json) { return new EmiStack().setAmount(json.getAsLong()); }
    }
    static class Serializer {
        static final String TYPE="type", TYPE_UID="type_uid", DATA="data", UID="uid", AMOUNT="amount", CHANCE="chance", REMAINDER="remainder";
        final TooManyRecipeViewers runtime;
        Serializer(TooManyRecipeViewers runtime) { this.runtime = runtime; }
        String getType() { return "tmrv"; }
        __SERIALIZER__
    }
    enum InputType { SIMULATE, EXECUTE, IMMEDIATE }
    static class InputConstants {
        enum Type { MOUSE, KEYSYM, SCANCODE; Key getOrCreate(int value) { return new Key(this, value); } }
        record Key(Type type, int value) { Type getType() { return type; } int getValue() { return value; } }
        static Key getKey(int code, int scan) { return new Key(Type.KEYSYM, code); }
    }
    record UserInput(InputConstants.Key key, int x, int y, int modifiers, InputType inputType) {}
    record Position(int x, int y) {}
    record ScreenRectangle(int left, int top, int right, int bottom) { Position position() { return new Position(left, top); } }
    static class EmiScreenManager { static int lastMouseX = 15, lastMouseY = 25; }
    interface IJeiInputHandler {
        ScreenRectangle getArea();
        boolean handleInput(int x, int y, UserInput input);
    }
    static class Listener {
        int clicks, releases, keys;
        final ScreenRectangle area = new ScreenRectangle(10, 20, 30, 40);
        ScreenRectangle getArea() { return area; }
        boolean mouseClicked(int x, int y, int button) { assert x == 5 && y == 5; clicks++; return true; }
        boolean mouseReleased(int x, int y, int button) { releases++; return true; }
        boolean keyPressed(int x, int y, int key, int scan, int modifiers) { assert modifiers == 4; keys++; return true; }
    }
    static class Widget {
        final List<IJeiInputHandler> inputHandlers = new ArrayList<>();
        final List<Listener> guiEventListeners = new ArrayList<>();
        int fallbackCalls;
        protected boolean handleInput(int x, int y, InputConstants.Key key) { fallbackCalls++; return true; }
        __WIDGET__
    }
    static void checkCopy() {
        var original = new TMRVStack<>(new Type<String>("custom"), new Helper(), null, new String("ingredient"));
        original.setAmount(8).setChance(.25f).setRemainder(new EmiStack().setAmount(2));
        original.comparison = new Object();
        var copy = (TMRVStack<?>) original.copy();
        assert copy.getAmount() == 8 && copy.getChance() == .25f;
        assert copy.getRemainder() != original.getRemainder() && copy.getRemainder().getAmount() == 2;
        assert copy.comparison == original.comparison;
        assert copy.ingredient.equals(original.ingredient) && copy.ingredient != original.ingredient;
    }
    static void checkUids() {
        var manager = new IngredientManager();
        var custom = new Type<String>("custom");
        for (var type : List.of(VanillaTypes.ITEM_STACK, fluidHelper.type, custom)) {
            assert manager.getIngredientByUid(type, "missing").isEmpty();
            assert manager.getTypedIngredientByUid(type, "missing").isEmpty();
        }
        manager.ingredientUidLookup.put(new TypedIngredientUID(VanillaTypes.ITEM_STACK, "known"), new TypedIngredient<>("item"));
        manager.ingredientUidLookup.put(new TypedIngredientUID(custom, "known"), "custom");
        assert manager.getIngredientByUid(VanillaTypes.ITEM_STACK, "known").orElseThrow().equals("item");
        assert manager.getIngredientByUid(custom, "known").orElseThrow().equals("custom");
        assert manager.getTypedIngredientByUid(custom, "known").orElseThrow().getIngredient().equals("custom");
    }
    static void checkAliases() {
        var manager = new IngredientManager();
        manager.addAlias(new Fluid(), "one");
        manager.addAliases(new Fluid(), List.of("two", "three"));
        assert manager.aliases.equals(List.of("one", "two", "three"));
    }
    static void checkValidation() {
        var manager = new IngredientManager();
        var custom = new Type<String>("custom");
        manager.infos.put(custom, new Info(null));
        var notified = new ArrayList<Object>();
        manager.listeners.add((helper, values) -> values.forEach(value -> notified.add(value.getIngredient())));
        manager.addIngredientsAtRuntime(custom, List.of("valid", "invalid", "absent"));
        assert manager.registered.equals(List.of("valid"));
        assert notified.equals(manager.registered);
        manager.locked = true;
        manager.addIngredientsAtRuntime(custom, List.of("late"));
        assert manager.registered.equals(List.of("valid"));
    }
    static void checkSerializer() {
        for (boolean withCodec : List.of(false, true)) {
            var runtime = new TooManyRecipeViewers();
            var type = new Type<String>("custom");
            runtime.ingredientManager.types.put(type.uid(), type);
            runtime.ingredientManager.infos.put(type, new Info(withCodec ? new Codec<>() : null));
            runtime.ingredientManager.ingredientUidLookup.put(new TypedIngredientUID(type, "ingredient"), "ingredient");
            var serializer = new Serializer(runtime);
            var stack = new TMRVStack<>(type, new Helper(), null, "ingredient");
            stack.setAmount(8).setChance(.25f).setRemainder(new EmiStack().setAmount(2));
            var json = serializer.serialize(stack).getAsJsonObject();
            assert json.has(withCodec ? "data" : "uid");
            assert !json.has(withCodec ? "uid" : "data");
            var restored = (TMRVStack<?>) serializer.deserialize(json);
            assert restored.ingredient.equals("ingredient");
            assert restored.amount == 8 && restored.chance == .25f && restored.getRemainder().amount == 2;
            var legacy = new JsonObject();
            legacy.addProperty("type_uid", "custom"); legacy.addProperty("uid", "ingredient");
            assert ((TMRVStack<?>) serializer.deserialize(legacy)).ingredient.equals("ingredient");
            legacy.addProperty("uid", "missing");
            assert serializer.deserialize(legacy) == EmiStack.EMPTY;
            runtime.ingredientManager.types.clear();
            assert serializer.deserialize(json) == EmiStack.EMPTY;
        }
    }
    static void checkWidget() {
        var widget = new Widget();
        var listener = new Listener(); widget.guiEventListeners.add(listener);
        assert widget.mouseClicked(15, 25, 0);
        assert listener.clicks == 1 && listener.releases == 0 && widget.fallbackCalls == 0;
        assert widget.keyPressed(65, 0, 4) && listener.keys == 1;
        assert widget.mouseClicked(50, 50, 0) && widget.fallbackCalls == 1 && listener.clicks == 1;
        var phases = new ArrayList<InputType>();
        widget.inputHandlers.add(new IJeiInputHandler() {
            public ScreenRectangle getArea() { return listener.area; }
            public boolean handleInput(int x, int y, UserInput input) { assert x == 5 && y == 5; phases.add(input.inputType()); return true; }
        });
        assert widget.mouseClicked(15, 25, 0);
        assert phases.equals(List.of(InputType.SIMULATE, InputType.EXECUTE));
        assert listener.clicks == 1;
    }
    public static void main(String[] args) {
        var checks = List.of(
            Map.entry("stack copies", (Runnable) AuditRegressions::checkCopy),
            Map.entry("UID lookups", (Runnable) AuditRegressions::checkUids),
            Map.entry("fluid aliases", (Runnable) AuditRegressions::checkAliases),
            Map.entry("ingredient validation", (Runnable) AuditRegressions::checkValidation),
            Map.entry("codec/UID serialization", (Runnable) AuditRegressions::checkSerializer),
            Map.entry("GUI clicks", (Runnable) AuditRegressions::checkWidget));
        int failures = 0;
        for (var check : checks) {
            try { check.getValue().run(); }
            catch (AssertionError | RuntimeException error) { failures++; System.out.println("FAIL: " + check.getKey() + ": " + error); }
        }
        assert failures == 0 : failures + " regression checks failed";
        System.out.println("PASS: stack copies, UID misses/hits, fluid aliases, validation, codec/UID serialization and single GUI clicks");
    }
}
"""

harness = harness.replace("class EmiStack {", "class EmiStack implements EmiIngredient {")
harness = harness.replace("__COPY__", methods("client/ingredient/TMRVStack.java", "public EmiStack copy()"))
harness = harness.replace("__MANAGER__", methods(
    "impl/jei/api/runtime/IngredientManager.java",
    "public <V> Optional<V> getIngredientByUid(",
    "public <V> Optional<ITypedIngredient<V>> getTypedIngredientByUid(",
    "public void addAlias(Fluid fluid, String alias)",
    "public <B, I> void addAlias(IIngredientTypeWithSubtypes<B, I> type, B base, String alias)",
    "public void addAliases(Fluid fluid, Collection<String> aliases)",
    "public <B, I> void addAliases(IIngredientTypeWithSubtypes<B, I> type, B base, Collection<String> aliases)",
    "public <V> void addIngredientsAtRuntime(",
))
harness = harness.replace("__SERIALIZER__", methods(
    "client/ingredient/TMRVStackSerializer.java",
    "public EmiIngredient deserialize(", "public JsonElement serialize(",
))
widget_signatures = [
    "public boolean mouseClicked(", "public boolean keyPressed(",
    "private static boolean containsPoint(",
]
if "private boolean handleInputHandlers(" in read_source("client/recipe/TMRVRecipeWidget.java"):
    widget_signatures.append("private boolean handleInputHandlers(")
widget_signatures.append("private boolean handleInput(")
harness = harness.replace("__WIDGET__", methods("client/recipe/TMRVRecipeWidget.java", *widget_signatures))

gson_root = Path.home() / ".gradle/caches/modules-2/files-2.1/com.google.code.gson/gson"
gson = next(gson_root.rglob("gson-*.jar"))
with tempfile.TemporaryDirectory(prefix="tmrv-audit-test-") as directory:
    java_file = Path(directory) / "AuditRegressions.java"
    java_file.write_text(harness)
    subprocess.run(["javac", "-cp", str(gson), str(java_file)], check=True)
    subprocess.run(["java", "-ea", "-cp", f"{directory};{gson}", "AuditRegressions"], check=True)
