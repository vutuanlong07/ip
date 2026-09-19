import org.gradle.internal.os.OperatingSystem

plugins {
    application
    id("java-common-conventions")
    id("org.graalvm.buildtools.native")
    id("com.gradleup.shadow") version "9.5.1"
}

group = findProperty("group")!!
version = findProperty(project.name + "-version")!!

val platform: OperatingSystem = OperatingSystem.current()
var platformTag = (
        if (platform.isMacOsX) "mac_os"
        else if (platform.isWindows) "windows"
        else if (platform.isLinux) "linux" else "others"
) + "_" + System.getProperty("os.arch")

dependencies {
    implementation(project(":marquee-core"))
}

application {
    mainClass = "org.cs2103t.marquee.cli.Application"
}

graalvmNative {
    toolchainDetection = true

    binaries.named("main") {
        imageName = "${project.name}-v${project.version}-${platformTag}"
        buildArgs.add("--static-nolibc")
        buildArgs.add("-march=native")
        buildArgs.add("-O3")
    }

    agent {
        defaultMode = "standard"
    }
}

tasks.jar {
    archiveFileName = "${project.name}-v${project.version}-jar_lean.jar"
}

tasks.shadowJar {
    archiveFileName = "${project.name}-v${project.version}-jar.jar"
}

tasks.run {
    standardInput = System.`in`
}