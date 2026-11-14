import java.util.Properties
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import org.gradle.internal.extensions.stdlib.capitalized

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.devtools.ksp")
  id("com.google.dagger.hilt.android")
  id("com.google.protobuf") version "0.9.5"
}

// OTPilot: builds outside main are test builds named "<base>-dev.N", N = commits since the last
// stable tag (dev prerelease tags are skipped so the count does not reset when one is published).
val baseVersion = "1.1.2"

fun git(vararg args: String): String? =
    runCatching { providers.exec { commandLine("git", *args) }.standardOutput.asText.get().trim() }.getOrNull()?.takeIf { it.isNotEmpty() }

// A build is stable on main, or when the commit is exactly a stable tag (F-Droid builds from the tag
// with a detached HEAD, so the branch name alone is not enough).
val onStableTag = git("describe", "--tags", "--exact-match", "--match", "v[0-9]*", "--exclude", "*-dev.*") != null
val appVersion: String =
    if (onStableTag || git("rev-parse", "--abbrev-ref", "HEAD") == "main") baseVersion
    else "$baseVersion-dev." + (git("describe", "--tags", "--abbrev=0", "--exclude=*-dev.*")?.let { git("rev-list", "--count", "$it..HEAD") } ?: "0")

android {
  namespace = "io.github.jd1378.otphelper"
  compileSdk = 36

  defaultConfig {
    applicationId = "dev.tvnclr.otpilot"
    minSdk = 24
    targetSdk = 36
    versionCode = 13
    // fdroid branch: literal so F-Droid's checkupdates can read it
    versionName = "1.1.2"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }
  }

  // OTPilot: release signing comes from keystore.properties (kept out of git)
  val otpilotKeystoreProps = rootProject.file("keystore.properties")
  signingConfigs {
    if (otpilotKeystoreProps.exists()) {
      val props = Properties().apply { otpilotKeystoreProps.inputStream().use { load(it) } }
      create("otpilot") {
        storeFile = file(props.getProperty("storeFile"))
        storePassword = props.getProperty("storePassword")
        keyAlias = props.getProperty("keyAlias")
        keyPassword = props.getProperty("keyPassword")
      }
    }
  }

  buildTypes {
    release {
      if (otpilotKeystoreProps.exists()) signingConfig = signingConfigs.getByName("otpilot")
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  flavorDimensions += "version"
  productFlavors {
    create("normal") {
      isDefault = true
      dimension = "version"
      buildConfigField("Boolean", "SMS_MODE_AVAILABLE", "true")
    }
    create("play") {
      dimension = "version"
      versionNameSuffix = "-play"
      buildConfigField("Boolean", "SMS_MODE_AVAILABLE", "false")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  // OTPilot: no Google-encrypted dependency metadata in the APK (F-Droid rejects it, and it is not
  // needed outside Google Play)
  dependenciesInfo {
    includeInApk = false
    includeInBundle = false
  }
  // OTPilot: keep the git commit out of the APK, so the same code built from main and from the
  // fdroid branch gives byte-identical APKs (reproducible builds on F-Droid)
  buildTypes.all { vcsInfo { include = false } }
  androidResources { generateLocaleConfig = true }

  applicationVariants.all(ApplicationVariantAction())
}

val protobufVersion = "4.33.0"

dependencies {
  implementation(platform("androidx.compose:compose-bom:2025.10.01"))
  androidTestImplementation(platform("androidx.compose:compose-bom:2025.10.01"))

  implementation("androidx.core:core-ktx:1.17.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
  implementation("androidx.activity:activity-compose:1.11.0")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-core")
  // OTPilot: tvnclr-ui design system
  implementation(project(":tvnclr-ui"))
  implementation("androidx.datastore:datastore-preferences:1.1.7")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.yaml:snakeyaml:2.2")
  androidTestImplementation("androidx.test.ext:junit:1.3.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  // debugImplementation because LeakCanary should only run in debug builds.
  debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
  // navigation
  implementation("androidx.navigation:navigation-compose:2.9.5")
  // hilt
  implementation("com.google.dagger:hilt-android:2.57.2")
  ksp("com.google.dagger:hilt-compiler:2.57.2")
  // hilt for navigation compose
  implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
  // hilt for work manager
  implementation("androidx.hilt:hilt-work:1.3.0")
  ksp("androidx.hilt:hilt-compiler:1.3.0")
  implementation("androidx.hilt:hilt-navigation-fragment:1.3.0")
  implementation("androidx.work:work-runtime-ktx:2.11.0")
  // app compat (for locales)
  val appcompatVersion = "1.7.1"
  implementation("androidx.appcompat:appcompat:$appcompatVersion")
  implementation("androidx.appcompat:appcompat-resources:$appcompatVersion")
  // room db
  val roomVersion = "2.8.3"
  implementation("androidx.room:room-runtime:$roomVersion")
  annotationProcessor("androidx.room:room-compiler:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  implementation("androidx.room:room-paging:$roomVersion")
  val pagingVersion = "3.3.6"
  implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
  implementation("androidx.paging:paging-compose:$pagingVersion")

  // datastore
  implementation("androidx.datastore:datastore:1.1.7")
  implementation("com.google.protobuf:protobuf-javalite:$protobufVersion")
  implementation("com.google.protobuf:protobuf-kotlin-lite:$protobufVersion")

  // for splash screen
  implementation("androidx.core:core-splashscreen:1.1.0-rc01")

  // immutable collections (for compose stability fix)
  implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")

  // cache
  implementation("io.github.reactivecircus.cache4k:cache4k:0.14.0")
}

hilt { enableAggregatingTask = true }

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

protobuf {
  protoc { artifact = "com.google.protobuf:protoc:$protobufVersion" }

  // Generates the java Protobuf-lite code for the Protobufs in this project. See
  // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
  // for more information.
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        register("java") {
          option("lite")
        }
        register("kotlin") {
          option("lite")
        }
      }
    }
  }
}

class ApplicationVariantAction : Action<ApplicationVariant> {
  override fun execute(variant: ApplicationVariant) {
    variant.outputs.all(VariantOutputAction(variant))
  }

  class VariantOutputAction(private val variant: ApplicationVariant) : Action<BaseVariantOutput> {
    override fun execute(output: BaseVariantOutput) {
      if (output is ApkVariantOutputImpl) {
        val abi =
            output.getFilter(com.android.build.api.variant.FilterConfiguration.FilterType.ABI.name)
        val abiVersionCode =
            when (abi) {
              "armeabi-v7a" -> 1
              "arm64-v8a" -> 2
              "x86" -> 3
              "x86_64" -> 4
              else -> 0
            }
        val versionCode = variant.versionCode * 1000 + abiVersionCode
        output.versionCodeOverride = versionCode

        val flavor = variant.flavorName
        val builtType = variant.buildType.name
        val versionName = variant.versionName
        val architecture = abi ?: "universal"

        output.outputFileName =
            "otpilot-${builtType}-${versionName}-${architecture}-${versionCode}.apk"
      }
    }
  }
}

androidComponents {
  onVariants(selector().all()) { variant ->
    afterEvaluate {
      val capName = variant.name.capitalized()
      val protoTask = tasks.getByName("generate${capName}Proto")
      val kspTask = tasks.getByName("ksp${capName}Kotlin")
      kspTask.dependsOn(protoTask)
      val testProtoTask = tasks.getByName("generate${capName}UnitTestProto")
      val testKspTask = tasks.getByName("ksp${capName}UnitTestKotlin")
      testKspTask.dependsOn(testProtoTask)
    }
  }
}

tasks.whenTaskAdded {
  if (name.contains("ArtProfile")) {
    enabled = false
  }
}
