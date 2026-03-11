plugins {
    // Usando alias para sincronizar com o seu libs.versions.toml
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
