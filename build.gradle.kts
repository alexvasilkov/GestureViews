import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

plugins {
  // 9.2.0 drops K1 UAST fallback while K2 UAST still crashes on .kts lint analysis:
  id("com.android.application") version "9.1.1" apply false
  id("com.android.library") version "9.1.1" apply false
  id("com.vanniktech.maven.publish") version "0.36.0" apply false
  id("com.github.ben-manes.versions") version "0.54.0"
}

subprojects {
  repositories {
    google()
    mavenCentral()
  }

  // Enabling checkstyle for all sub projects
  afterEvaluate {
    apply(plugin = "checkstyle")

    extensions.configure<CheckstyleExtension> {
      toolVersion = "8.32"
      configFile = file("$rootDir/checkstyle.xml")
    }

    tasks.register<Checkstyle>("checkstyle") {
      source("src")
      include("**/*.java", "**/*.xml")
      classpath = files()
    }

    tasks.named("check") {
      dependsOn("checkstyle")
    }

    tasks.withType<JavaCompile>().configureEach {
      options.compilerArgs.add("-Xlint:deprecation")
    }
  }
}

// Check versions with `./gradlew dependencyUpdates`
// https://github.com/ben-manes/gradle-versions-plugin
private val stableRegex = Regex(pattern = "^[0-9,.v-]+(-r)?$")
private val String.isStable: Boolean get() = stableRegex.matchEntire(this) != null
private val String.isBeta: Boolean get() = contains("beta")

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
  rejectVersionIf {
    val ver = candidate.version
    val curr = currentVersion
    (curr.isStable && !ver.isStable) || (curr.isBeta && !ver.isStable && !ver.isBeta)
  }
}
