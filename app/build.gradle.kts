import java.net.URI

plugins {
    // Kotlin được AGP 9.3.3 hỗ trợ tích hợp sẵn, không cần plugin Kotlin riêng.
    alias(libs.plugins.android.application)
    // KSP sinh mã cho Room. Không dùng kapt vì AGP 9 dùng Kotlin tích hợp sẵn, không áp
    // dụng được plugin kotlin-kapt.
    alias(libs.plugins.ksp)
}

val configuredBackendUrl = providers.gradleProperty("newsApiBaseUrl")
val verifyReleaseBackendUrl by tasks.registering {
    val backendUrl = configuredBackendUrl.orElse("")
    inputs.property("backendUrl", backendUrl)
    doLast {
        val raw = backendUrl.get()
        val uri = runCatching { URI(raw) }.getOrNull()
        require(uri != null && uri.scheme == "https" && !uri.host.isNullOrBlank() &&
            uri.host.lowercase() !in setOf("localhost", "127.0.0.1", "10.0.2.2", "[::1]") &&
            uri.userInfo == null && uri.query == null && uri.fragment == null &&
            uri.path.endsWith("/")) {
            "Release requires -PnewsApiBaseUrl=https://<backend-host>/api/v1/ (HTTPS, no credentials/query/fragment, trailing slash)."
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" || it.name == "generateReleaseBuildConfig" }.configureEach {
    dependsOn(verifyReleaseBackendUrl)
}

android {
    namespace = "com.vlu.newsapi"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.vlu.newsapi"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Dia chi backend cho 4 API tin tuc. Mac dinh la may tinh chu chay emulator
        // (10.0.2.2 = localhost cua may that). Khi backend da deploy, truyen lai luc build:
        //   gradlew :app:assembleRelease -PnewsApiBaseUrl=https://<ten-app>.onrender.com/api/v1/
        val newsApiBaseUrl = configuredBackendUrl
            .orElse("http://10.0.2.2:5000/api/v1/")
            .get()
        buildConfigField("String", "NEWS_API_BASE_URL", "\"$newsApiBaseUrl\"")
    }

    // Supply release credentials through environment variables, never source control.
    val releaseStore = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
    signingConfigs {
        if (releaseStore != null) {
            create("production") {
                storeFile = file(releaseStore)
                storePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").get()
            }
        }
    }
    buildTypes {
        release {
            if (releaseStore != null) signingConfig = signingConfigs.getByName("production")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        // Can cho BuildConfig.NEWS_API_BASE_URL va BuildConfig.DEBUG (bat/tat log Retrofit).
        buildConfig = true
    }
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
}

// Xuất lược đồ cơ sở dữ liệu ra tệp. Bắt buộc phải có nếu muốn viết migration đúng về sau:
// không có lược đồ gốc thì không kiểm chứng được migration nào là hợp lệ.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)

    // Lifecycle + ViewModel (MVVM)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation Component - Single Activity, KHONG Bottom Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Danh sach + the bai bao
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    // Tang luu tru cuc bo (Room). Ma duoc sinh boi KSP.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Tai anh bai bao tu URL o trang Chi tiet (Muc 3.1 cua dac ta).
    implementation(libs.glide)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // 4 API tin tuc cua backend (main-grids, by-date, detail, search).
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    // MockWebServer: kiem tra lop Retrofit ma khong can backend that.
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.room:room-testing:2.8.5")
}
