import org.gradle.api.tasks.compile.JavaCompile

plugins {
    alias(libs.plugins.android.application)
}

val stubClasses = layout.buildDirectory.dir("stub-classes")

tasks.register<Exec>("assembleStubs") {
    val source = rootProject.file("../stubs/src/main/jasmin")
    val jasmin = rootProject.file("tools/jasmin.jar")
    inputs.dir(source)
    inputs.file(jasmin)
    outputs.dir(stubClasses)
    val dest = stubClasses
    doFirst { dest.get().asFile.mkdirs() }
    commandLine(
        "bash",
        "-lc",
        "cd '${source.absolutePath}' && find . -name '*.j' -print0 | xargs -0 java -jar '${jasmin.absolutePath}' -d '${dest.get().asFile.absolutePath}'"
    )
}

val stubJar = tasks.register<Jar>("stubJar") {
    dependsOn(tasks.named("assembleStubs"))
    from(stubClasses)
    archiveFileName.set("sony-stubs.jar")
    destinationDirectory.set(layout.buildDirectory.dir("stubs"))
}

val sonyStubs = files(stubJar.flatMap { it.archiveFile }).builtBy(stubJar)

android {
    namespace = "org.andrewgao.sony.compatibility"
    compileSdk {
        // Framework sources call Jelly Bean camera APIs. minSdk stays 10 so this still installs on the camera emulator.
        version = release(16)
    }

    defaultConfig {
        applicationId = "org.andrewgao.sony.compatibility"
        minSdk = 10
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 10
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets.named("main") {
        java.srcDir(rootProject.file("../framework/src/main/java"))
    }
}

dependencies {
    compileOnly(sonyStubs)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(stubJar)
}
