import org.taumc.gradle.compression.DeflateAlgorithm
import org.taumc.gradle.compression.task.AdvzipTask
import org.taumc.gradle.minecraft.MinecraftVersion
import org.taumc.gradle.minecraft.ModEnvironment
import org.taumc.gradle.minecraft.ModLoader
import org.taumc.gradle.publishing.api.artifact.Relation
import org.taumc.gradle.publishing.publishing
import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask
import xyz.wagyourtail.unimined.api.unimined
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

plugins {
	id("java")
	id("idea")
	id("maven-publish")
	id("com.gradleup.shadow")
	id("xyz.wagyourtail.unimined")
	id("com.github.gmazzo.buildconfig")
	id("org.taumc.gradle.versioning")
	id("org.taumc.gradle.compression")
	id("xyz.wagyourtail.jvmdowngrader")
}

operator fun String.invoke(): String = project.properties[this] as? String ?: error("Property $this not found")

fun loadTestDependencies(file: java.io.File): List<String> {
	if (!file.isFile)
		return emptyList()

	val properties = Properties()
	file.inputStream().use { properties.load(it) }
	return properties.stringPropertyNames().sorted().map { properties.getProperty(it) }
}

idea.module {
	isDownloadJavadoc = true
	isDownloadSources = true
}

val minecraftVersion = MinecraftVersion.get("minecraft_version"()) ?: error("Invalid `minecraft_version`!")
val modLoader = ModLoader.get("mod_loader"()) ?: error("Invalid `mod_loader`!")
val javaVersion = JavaVersion.valueOf("java_version"())
val buildJavaVersion = JavaVersion.valueOf("build_java_version"())
val testDependencies = loadTestDependencies(rootDir.resolve("versions/${stonecutter.current.project}/test-mods.properties"))

project.group = "maven_group"()
// TauGradle needs its version state initialized for the publishing block below.
tau.versioning.subVersion("mc.${minecraftVersion.conciseName}")
project.version = "${"mod_version"()}+mc.${minecraftVersion.conciseName}"

println("TooManyRecipeViewers version: ${project.version}")

buildConfig {
	className("TooManyRecipeViewersConstants")
	packageName("dev.nolij.toomanyrecipeviewers")

	useJavaOutput()

	buildConfigField("MOD_ID", "mod_id"())
}

base {
	archivesName = "mod_name"()
}

repositories {
	maven("https://repo.spongepowered.org/maven")
	maven("https://jitpack.io/")
	exclusiveContent {
		forRepository { maven("https://api.modrinth.com/maven") }
		filter {
			includeGroup("maven.modrinth")
		}
	}
	exclusiveContent {
		forRepository { maven("https://cursemaven.com") }
		filter {
			includeGroup("curse.maven")
		}
	}
	maven("https://maven.blamejared.com")
	maven("https://maven.taumc.org/releases")
	maven("https://maven.teamresourceful.com/repository/TerrarformerMC/")
	maven("https://maven.parchmentmc.org")
	maven("https://modmaven.dev/")
	mavenCentral()
	maven("https://central.sonatype.com/repository/maven-snapshots/")
//	maven("https://maven.createmod.net/")
//	maven("https://maven.architectury.dev/")
//	maven("https://maven.squiddev.cc/")
//	maven("https://maven.ithundxr.dev/snapshots")
//	maven("https://maven.ftb.dev/releases")
//	maven("https://maven.theillusivec4.top/")
}

dependencies {
	compileOnly("org.jetbrains:annotations:${"jetbrains_annotations_version"()}")
	
	compileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
	annotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")
}

tasks.withType<JavaCompile> {
	if (name !in arrayOf("compileMcLauncherJava", "compilePatchedMcJava")) {
		options.encoding = "UTF-8"
		sourceCompatibility = buildJavaVersion.majorVersion
		options.release = buildJavaVersion.majorVersion.toInt()
		javaCompiler = javaToolchains.compilerFor {
			languageVersion = JavaLanguageVersion.of(buildJavaVersion.majorVersion.toInt())
		}
		options.compilerArgs.addAll(arrayOf("-Xplugin:Manifold no-bootstrap"))
		options.forkOptions.jvmArgs?.add("-XX:+EnableDynamicAgentLoading")
	}
}

tasks.processResources {
	inputs.file(rootDir.resolve("gradle.properties"))
	inputs.property("version", project.version.toString())
	inputs.property("mod_loader", modLoader.commonName)

	filteringCharset = "UTF-8"

	val props = mutableMapOf<String, String>()
	props.putAll(project.properties
		.filterValues { value -> value is String }
		.mapValues { entry -> entry.value as String })
	props["mod_version"] = project.version.toString()
	props["mod_loader"] = modLoader.commonName
	props["mixin_java_version"] = (project.properties["java_version"] as String).replace("VERSION_", "")

	filesMatching(listOf("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
		expand(props)
	}

	if (modLoader != ModLoader.LEXFORGE) {
		exclude("pack.mcmeta")
	}

	doLast {
		if (modLoader == ModLoader.LEXFORGE || (modLoader == ModLoader.NEOFORGE && minecraftVersion < "20.5")) {
			fileTree(mapOf("dir" to tasks.processResources.get().outputs.files.asPath, "include" to "META-INF/neoforge.mods.toml")).onEach { file ->
				Files.copy(file.toPath(), kotlin.io.path.Path(outputs.files.asPath).resolve("META-INF/mods.toml"), StandardCopyOption.REPLACE_EXISTING)
				file.delete()
			}
		}
	}
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	
	// TODO: yell at wyt to fix Unimined
	includeEmptyDirs = false
	exclude("assets/minecraft/textures/**")
}

tasks.withType<GenerateModuleMetadata> {
	enabled = false
}

val shade: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

val modRuntimeOnly: Configuration by configurations.creating {
	configurations.runtimeClasspath.get().extendsFrom(this)
}

if (minecraftVersion < "20.5") {
	configurations.configureEach {
		resolutionStrategy.eachDependency {
			if (requested.group == "org.lwjgl" && requested.version?.startsWith("3.") == true) {
				useVersion("lwjgl_version"())
				because("compatible with the configured build Java version")
			}
		}
	}
}

unimined.minecraft {
	version(minecraftVersion.mojangName)

	runs {
		config("client") {
			javaVersion = buildJavaVersion
			jvmArgs("-Xmx4G")
		}
	}

	when (modLoader) {
		ModLoader.NEOFORGE -> {
			neoForge {
				loader("mod_loader_version"())
				mixinConfig("toomanyrecipeviewers.mixins.json")
//				accessTransformer = file("src/main/resources/META-INF/accesstransformer.cfg")
			}
		}
		ModLoader.LEXFORGE -> {
			minecraftForge {
				loader("mod_loader_version"())
				mixinConfig("toomanyrecipeviewers.mixins.json")
//				accessTransformer = file("src/main/resources/META-INF/accesstransformer.cfg")
			}
		}
		else -> error("Invalid `mod_loader`!")
	}

	mappings {
		mojmap()
		parchment(mcVersion = minecraftVersion.mojangName, version = "parchment_version"())
	}
	
	if (modLoader == ModLoader.LEXFORGE) {
		mods {
			remap(modRuntimeOnly)
		}
	}
}

dependencies {
	val minecraftLibraries by configurations.getting
	minecraftLibraries.isTransitive = true
	val modImplementation by configurations.getting
	val include by configurations.getting

	shade("dev.nolij:libnolij:${"libnolij_version"()}")
	minecraftLibraries("dev.nolij:libnolij:${"libnolij_version"()}")

	shade("dev.nolij:zson:${"zson_version"()}")
	minecraftLibraries("dev.nolij:zson:${"zson_version"()}")
	
	if (minecraftVersion >= "20.2")
		implementation("dev.emi:emi-${modLoader.commonName}:${"emi_version"()}")
	else
		modImplementation("dev.emi:emi-${modLoader.commonName}:${"emi_version"()}")
	
	shade(project(stonecutter.node.sibling("jei-api")!!.project.path, configuration = "jeiAPIJar")) { isTransitive = false }
	
	// for testing purposes
	if (minecraftVersion.equals("21.1") && modLoader == ModLoader.NEOFORGE) {
		testDependencies.forEach { runtimeOnly(it) }
	} else if (minecraftVersion.equals("20.1") && modLoader == ModLoader.LEXFORGE) {
		compileOnly("io.github.llamalad7:mixinextras-common:${"mixinextras_version"()}")
		include("io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}")
		implementation("io.github.llamalad7:mixinextras-forge:${"mixinextras_version"()}")

		testDependencies.forEach { modRuntimeOnly(it) }
	}
}

val sourcesJar by tasks.registering(Jar::class) {
	group = "build"

	archiveClassifier = "sources"
	from(rootProject.files("ATTRIBUTION.md", "DISTRIBUTION.md"))

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${"mod_id"()}" }
	}

	listOf(project.sourceSets, stonecutter.node.sibling("jei-api")!!.project.sourceSets).flatten().forEach {
		from(it.allSource) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	}
}

tasks.shadowJar {
	from(rootProject.files("ATTRIBUTION.md", "DISTRIBUTION.md"))
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${"mod_id"()}" }
	}
	
	exclude("mezz/jei/common/platform/JEIAPIStub.class")

	configurations = listOf(shade)
	archiveClassifier = "unremapped"

	relocate("dev.nolij.libnolij", "dev.nolij.toomanyrecipeviewers.libnolij")
	relocate("dev.nolij.zson", "dev.nolij.toomanyrecipeviewers.zson")
}

tasks.named<RemapJarTask>("remapJar") {
	inputFile.set(tasks.shadowJar.get().archiveFile)

	asJar.archiveClassifier = "remapped"

	mixinRemap {
		enableMixinExtra()
	}
}
val inputJar = tasks.getByName("remapJar") as AbstractArchiveTask

val compressionInputJar = if (javaVersion.ordinal < buildJavaVersion.ordinal) {
	jvmdg.defaultTask {
		inputFile = inputJar.archiveFile
		downgradeTo = javaVersion
	}

	jvmdg.defaultShadeTask.get()!!
} else {
	inputJar
}

// Cannot use CompressionExtension.compress because the finalizedBy/dependency logic makes chiseled builds deadlock
val compressJar = tasks.register<AdvzipTask>("compressJar") {
	inputJar = compressionInputJar.archiveFile
	destinationDirectory = compressionInputJar.destinationDirectory
	level = DeflateAlgorithm.EXTRA
	throwIfNotInstalled = tau.versioning.isRelease
	archiveClassifier = ""
	dependsOn(compressionInputJar)
}

val outputJar = compressJar

val sourceArchive by tasks.registering(Zip::class) {
	group = "build"
	archiveClassifier = "source"
	destinationDirectory = layout.buildDirectory.dir("libs")
	from(rootDir) {
		include("src/**", "jei-api/src/**", "jei-api/build.gradle.kts", "buildSrc/src/**", "buildSrc/*.gradle.kts", "buildSrc/gradle.properties", "gradle/**", "versions/*/*.properties", "tests/*.py", "scripts/**")
		include("*.gradle.kts", "gradle.properties", "gradlew", "gradlew.bat", "LICENSE", "ATTRIBUTION.md", "DISTRIBUTION.md", "README*.md", "CHANGELOG.md", "BENCHMARKS.md", "AGENTS.md")
		exclude("**/build/**", "**/.gradle/**", "**/run/**", "**/logs/**")
	}
}

tasks.assemble {
	dependsOn(outputJar, sourcesJar, sourceArchive)
}

tasks.check {
	dependsOn(rootProject.tasks.named("auditRegressions"), rootProject.tasks.named("pluginRegressions"), rootProject.tasks.named("flowingFluidIngredients"))
}

rootProject.tau.publishing.modArtifact("${modLoader.displayName} ${minecraftVersion.conciseName}") {
	files(provider { outputJar.get().archiveFile }, provider { sourcesJar.get().archiveFile }, provider { sourceArchive.get().archiveFile })

	version = project.version.toString()

	minecraftVersionRange = minecraftVersion.mojangName
	javaVersions.add(javaVersion)

	environment = ModEnvironment.CLIENT_ONLY
	modLoaders.add(modLoader)

	relations.add(Relation(id = "emi", type = Relation.Type.REQUIRES))
	relations.add(Relation(id = "jei", type = Relation.Type.BREAKS))
}
