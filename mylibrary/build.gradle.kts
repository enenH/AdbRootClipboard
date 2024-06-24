import com.android.tools.r8.R8
import org.apache.tools.ant.taskdefs.Sleep
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.pathString

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.example.mylibrary"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "VERSION_NAME", "\"1.0\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        aidl = true
        buildConfig = true
    }
}
android.libraryVariants.all {
    val jarTask = tasks.register("create${name.capitalize()}MainJar") {

        doLast {
            val classDir = Paths.get(buildDir.path, "intermediates",
                    "javac", this@all.name, "classes")

            val classFiles = Files.walk(classDir)
                    .filter { Files.isRegularFile(it) && it.toString().endsWith(".class") }
                    .collect(Collectors.toList())

            val androidJar = Paths.get(android.sdkDirectory.path, "platforms",
                    android.compileSdkVersion, "android.jar")

            val output = Paths.get(
                    android.sourceSets.getByName("main").assets.srcDirs.first().path, "main.jar")

            if (Files.notExists(output.parent))
                Files.createDirectories(output.parent)

            val pgConf = File(buildDir, "mainJar.pro")

            PrintStream(pgConf.outputStream()).use {
                it.println("-keep class com.example.mylibrary.Main")
                it.println("{ *; }")


            }

            val args = mutableListOf<Any>(
                    "--release", "--output", output,
                    "--pg-conf", pgConf,
                    "--classpath", androidJar
            ).apply { addAll(classFiles) }
                    .map { it.toString() }
                    .toTypedArray()

            R8.main(args)

            val path = Paths.get(
                android.sourceSets.getByName("main").assets.srcDirs.first().path)
            //解压zip
            val unzip = "tar -xf ${output} -C ${path}"
            println(unzip)
            Runtime.getRuntime().exec(unzip).waitFor()

            //run xxd
            val xxd = "${path}\\xxd.exe -n classes -i ${path}\\classes.dex > ${path}\\classes.h"
            println(xxd)
            val processBuilder = ProcessBuilder("cmd", "/c", xxd)
            processBuilder.directory(File(path.pathString))
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            println("Exit code: $exitCode")

           // Runtime.getRuntime().exec(xxd).waitFor()
        }
    }
    javaCompileProvider {
        finalizedBy(jarTask)
    }
}
dependencies {
}