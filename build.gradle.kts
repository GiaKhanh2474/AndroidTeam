// Top-level build file where you can add configuration options common to all sub-projects/modules.
// AGP 9.3.3 có Kotlin tích hợp sẵn (built-in Kotlin) nên KHÔNG apply
// plugin org.jetbrains.kotlin.android ở đây — sẽ gây lỗi trùng extension "kotlin".
plugins {
    alias(libs.plugins.android.application) apply false
}
