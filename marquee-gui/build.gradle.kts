plugins {
    application
    id("java-common-conventions")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gluonhq.gluonfx-gradle-plugin") version "1.0.29"
    id("com.gradleup.shadow") version "9.5.1"
}

group = findProperty("group")!!
version = findProperty(project.name + "-version")!!

val platform: org.gradle.internal.os.OperatingSystem = org.gradle.internal.os.OperatingSystem.current()
var platformTag = (
        if (platform.isMacOsX) "mac_os"
        else if (platform.isWindows) "windows"
        else if (platform.isLinux) "linux" else "others"
) + "_" + System.getProperty("os.arch")

dependencies {
    implementation(project(":marquee-core"))
}

javafx {
    version = "26.0.2"
    modules.add("javafx.controls")
    modules.add("javafx.fxml")
}

application {
    mainClass = "org.cs2103t.marquee.gui.MainApplication"
}

tasks.jar {
    archiveFileName = "${project.name}-v${project.version}-jar_lean.jar"
}

tasks.shadowJar {
    mainClass = "org.cs2103t.marquee.gui.AlternativeMain"
    archiveFileName = "${project.name}-v${project.version}-jar.jar"
}