plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.register("test") {
    dependsOn("jvmTest")
}
