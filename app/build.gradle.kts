plugins {
    // Kotlin được AGP 9.3.3 hỗ trợ tích hợp sẵn, không cần plugin Kotlin riêng.
    alias(libs.plugins.android.application)
    // KSP sinh mã cho Room. Không dùng kapt vì AGP 9 dùng Kotlin tích hợp sẵn, không áp
    // dụng được plugin kotlin-kapt.
    alias(libs.plugins.ksp)
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
        val newsApiBaseUrl = providers.gradleProperty("newsApiBaseUrl")
            .orElse("http://10.0.2.2:5000/api/v1/")
            .get()
        buildConfigField("String", "NEWS_API_BASE_URL", "\"$newsApiBaseUrl\"")
    }

    buildTypes {
        release {
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
}
