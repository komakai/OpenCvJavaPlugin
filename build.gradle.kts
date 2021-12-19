import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  id("java")
  id("maven-publish")
  id("org.jetbrains.kotlin.jvm") version "1.5.10"
  id("org.jetbrains.intellij") version "1.0"
  id("io.gitlab.arturbosch.detekt") version "1.17.1"
  id("com.github.blueboxware.tocme") version "1.3"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
  mavenCentral()
}

intellij {
  pluginName.set(properties("pluginName"))
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))
  downloadSources.set(properties("platformDownloadSources").toBoolean())
  updateSinceUntilBuild.set(true)

  // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
  plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}


sourceSets {
  main {
    resources.srcDirs("resources")
  }
}

tasks {
  // Set the compatibility versions to 1.8
  withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  runIde {
    maxHeapSize = "2g"
    systemProperties = mapOf(
      "idea.ProcessCanceledException" to "disabled"
    )
  }

  tocme {
    doc("README.md")
  }

  buildSearchableOptions {
    // TODO
    enabled = false
  }

  patchPluginXml {
    version.set(properties("pluginVersion"))
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))
  }

  runPluginVerifier {
    ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
  }

  test {
    systemProperty("idea.home.path", System.getenv("OPENCVPLUGIN_IDEA"))
  }

}
