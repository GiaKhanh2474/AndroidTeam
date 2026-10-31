plugins {
    // Kotlin được AGP 9.3.3 hỗ trợ tích hợp sẵn, không cần plugin Kotlin riêng.
    alias(libs.plugins.android.application)
    // KSP sinh mã cho Room. Không dùng kapt vì AGP 9 dùng Kotlin tích hợp sẵn, không áp
    // dụng được plugin kotlin-kapt.
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.news"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.news"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
