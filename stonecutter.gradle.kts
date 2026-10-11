import org.taumc.gradle.publishing.api.PublishChannel
import org.taumc.gradle.publishing.publishing

plugins {
	id("idea")
	id("dev.kikugie.stonecutter")
	id("org.taumc.gradle.versioning")
	id("org.taumc.gradle.publishing")
}
stonecutter active "21.1-neoforge" /* [SC] DO NOT EDIT */

operator fun String.invoke(): String = project.properties[this] as? String ?: error("Property $this not found")

idea.module {
	isDownloadJavadoc = true
	isDownloadSources = true
}

project.group = "maven_group"()
project.version = tau.versioning.version("mod_version"(), project.properties["release_channel"])

tasks.register<Exec>("auditRegressions") {
	group = "verification"
	environment("GRADLE_USER_HOME", gradle.gradleUserHomeDir.absolutePath)
	commandLine("python", rootDir.resolve("tests/audit_regressions.py"))
}

tasks.register<Exec>("pluginRegressions") {
	group = "verification"
	environment("GRADLE_USER_HOME", gradle.gradleUserHomeDir.absolutePath)
	commandLine("python", rootDir.resolve("tests/plugin_regressions.py"))
}

tasks.register<Exec>("flowingFluidIngredients") {
	group = "verification"
	commandLine("python", rootDir.resolve("tests/flowing_fluid_ingredients.py"))
}

tasks.register<Exec>("distributionArtifacts") {
	group = "verification"
	environment("GRADLE_USER_HOME", gradle.gradleUserHomeDir.absolutePath)
	dependsOn(":20.1-lexforge:assemble", ":21.1-neoforge:assemble")
	commandLine("python", rootDir.resolve("tests/distribution_artifacts.py"))
}

tasks.register("runClientActive") {
	group = "project"
	dependsOn("${stonecutter.current?.project}:runClient")
}

tau.publishing.publish {
	useTauGradleVersioning()
	changelog = rootProject.file("CHANGELOG.md").readText()

	github {
		supportAllChannels()

		accessToken = providers.environmentVariable("GITHUB_TOKEN")
		repository = "taker99/TMRVReforged"
		tagName = tau.versioning.releaseTag
	}

	val iconURL = "https://github.com/Nolij/TooManyRecipeViewers/raw/master/src/main/resources/icon.png"

	discord {
		supportAllChannelsExcluding(PublishChannel.RELEASE)

		webhookURL = providers.environmentVariable("DISCORD_WEBHOOK")
		avatarURL = iconURL

		testBuildPreset(modName = "${"mod_name"()}", repoURL = "${"repo_url"()}")
	}

	discord {
		supportChannels(PublishChannel.RELEASE)

		webhookURL = providers.environmentVariable("DISCORD_WEBHOOK")
		avatarURL = iconURL

		releasePreset(modName = "${"mod_name"()}")
	}
}

stonecutter parameters {
	replacements {
		string {
			direction = eval(current.version, ">=21.1")
			replace("mezz.jei.forge", "mezz.jei.neoforge")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace("mezz.jei.api.forge", "mezz.jei.api.neoforge")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace("net.minecraftforge.client.event", "net.neoforged.neoforge.client.event")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace("net.minecraftforge.fml", "net.neoforged.fml")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace("net.minecraftforge.network", "net.neoforged.neoforge.network")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace("net.minecraftforge.fluids", "net.neoforged.neoforge.fluids")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace("@Nullable CompoundTag dataComponentPatch", "DataComponentPatch dataComponentPatch")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace("(dataComponentPatch == null || dataComponentPatch.isEmpty())", "(dataComponentPatch.isEmpty())")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace("import net.minecraft.nbt.CompoundTag;", "import net.minecraft.core.component.DataComponentPatch;")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace(".getNbt()", ".getComponentChanges()")
		}
		string {
			direction = eval(current.version, ">=21.1")
			replace(".getTag()", ".getComponentsPatch()")
		}
	}
}
