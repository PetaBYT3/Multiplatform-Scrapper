import com.google.common.jimfs.Configuration.windows
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.google.services)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            //Koin
            api(libs.koin.android)

            //Firebase
            api(project.dependencies.platform(libs.firebase.bom))
            api(libs.firebase.database.ktx)
            api(libs.firebase.auth.ktx)
            api(libs.firebase.firestore.ktx)

            //Xlsx Reader
            api(libs.poi.ooxml)
            api(libs.poi.xmlbeans)
            api(libs.poi.commonsCompress)
            api(libs.poi.curvesapi)

            //Ktor
            api(libs.ktor.client.android)

            //WebView
            api(libs.delight.webview)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.components.resources)

            //Koin
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)

            //Navigation
            api(libs.androidx.compose.navigation)

            //Kotlinx Serialization
            api(libs.kotlinx.serialization.json)

            //File Picker
            api(libs.filekit.compose)
            api(libs.filekit.core)

            //Time
            api(libs.kotlinx.datetime)

            //Material Icon
            api(compose.materialIconsExtended)

            //DataStore
            api(libs.multiplatform.settings)
            api(libs.multiplatform.settings.coroutines)

            //WebView
            api(libs.compose.webview.multiplatform)

            //Shimmer Loading
            api(libs.compose.shimmer)

            //Ktor
            api(libs.ktor.client.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            //Firebase
            api(libs.firebase.admin)

            //Xlsx Reader
            api(libs.poi.ooxml)
            api(libs.poi.xmlbeans)
            api(libs.poi.commonsCompress)
            api(libs.poi.curvesapi)

            //Ktor
            api(libs.ktor.client.cio)

            //JvmLog
            api(libs.slf4j.simple)

            //WebView
            api(libs.delight.webview)
        }
    }
}

android {
    namespace = "org.scrapper.multiplatform"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.scrapper.multiplatform"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.scrapper.multiplatform.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Speed Runner"
            packageVersion = "1.0.0"
            group = "Andrea Hussanini (andreahussanini.2103@gmail.com)"
            copyright = "© 2025 Andrea Hussanini (andreahussanini.2103@gmail.com)"

            windows {
                shortcut = true
                menu = true
                perUserInstall = true
                iconFile.set(project.file("src/jvmMain/resources/speed_runner.ico"))
            }
        }
    }
}

afterEvaluate {
    tasks.withType<JavaExec> {
        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}
