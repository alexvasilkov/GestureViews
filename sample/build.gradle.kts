plugins {
  id("com.android.application")
}

android {
  namespace = "com.alexvasilkov.gestures.sample"
  compileSdk = 37

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  defaultConfig {
    minSdk = 23
    targetSdk = 37

    applicationId = "com.alexvasilkov.gestures.sample"
    versionName = "2.9.0"
    versionCode = 1

    @Suppress("DEPRECATION") // Suggested alternative is marked "Unstable"...
    resourceConfigurations.addAll(listOf("en"))
  }

  buildFeatures {
    buildConfig = true
  }

  signingConfigs {
    getByName("debug") {
      storeFile = file("debug.jks")
    }
  }

  buildTypes {
    getByName("debug") {
      signingConfig = signingConfigs.getByName("debug")
      applicationIdSuffix = ".debug"
    }
  }

  lint {
    disable.addAll(
      listOf("GoogleAppIndexingWarning", "ContentDescription", "RtlHardcoded", "IconMissingDensityFolder")
    )
  }
}

dependencies {
  implementation(project(":library"))

  implementation("androidx.appcompat:appcompat:1.7.1")
  implementation("androidx.recyclerview:recyclerview:1.4.0")
  implementation("androidx.viewpager2:viewpager2:1.1.0")
  implementation("com.google.android.material:material:1.14.0")

  implementation("com.github.bumptech.glide:glide:5.0.7")

  implementation("com.alexvasilkov:android-commons:2.0.2")
  implementation("com.alexvasilkov:events:1.0.0")

  implementation("com.googlecode.flickrj-android:flickrj-android:2.1.0")
  implementation("org.slf4j:slf4j-android:1.7.36") // Required by Flickr library
}
