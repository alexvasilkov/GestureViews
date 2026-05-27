import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
  id("com.android.library")
  id("com.vanniktech.maven.publish")
}

val libVersion = "2.9.0"

android {
  namespace = "com.alexvasilkov.gestures"
  compileSdk = 37

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  defaultConfig {
    minSdk = 23
  }

  lint {
    disable.addAll(
      listOf("GoogleAppIndexingWarning", "ContentDescription", "RtlHardcoded", "IconMissingDensityFolder")
    )
  }
}

dependencies {
  compileOnly("androidx.annotation:annotation:1.10.0")
  compileOnly("androidx.viewpager:viewpager:1.1.0")
  compileOnly("androidx.viewpager2:viewpager2:1.1.0")
  compileOnly("androidx.recyclerview:recyclerview:1.4.0")
}

tasks.withType<Javadoc>().configureEach {
  exclude("**/BuildConfig.java")
  exclude("**/R.java")
  options.windowTitle = "GestureViews $libVersion API"
}

// Publish with library/publish.sh (manual release), or add --release to auto-release.
// Secrets (Maven Central token + GPG key) for the plugin come from env vars set by publish.sh:
// ORG_GRADLE_PROJECT_mavenCentralUsername/Password and signingInMemoryKey/KeyId/KeyPassword.
mavenPublishing {
  configure(AndroidSingleVariantLibrary(JavadocJar.Javadoc(), SourcesJar.Sources(), "release"))

  publishToMavenCentral(false) // false = manual release
  signAllPublications() // Uses the in-memory key

  coordinates("com.alexvasilkov", "gesture-views", libVersion)

  pom {
    name.set("GestureViews")
    description.set("ImageView and FrameLayout with gestures control and position animation")
    url.set("https://github.com/alexvasilkov/GestureViews")
    licenses {
      license {
        name.set("The Apache License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
      }
    }
    developers {
      developer {
        id.set("alexvasilkov")
        name.set("Alex Vasilkov")
      }
    }
    scm {
      connection.set("scm:git@github.com:alexvasilkov/GestureViews.git")
      developerConnection.set("scm:git@github.com:alexvasilkov/GestureViews.git")
      url.set("https://github.com/alexvasilkov/GestureViews")
    }
  }
}
