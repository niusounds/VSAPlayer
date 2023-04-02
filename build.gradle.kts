plugins {
    alias(libs.plugins.android.application) apply (false)
    alias(libs.plugins.android.library) apply (false)
    alias(libs.plugins.kotlin.android) apply (false)
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}
