plugins {
    application
    id("java-common-conventions")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "9.5.1"
}

group = findProperty("group")!!
version = findProperty(project.name + "-version")!!

val jarName = findProperty("jarName") ?: "${project.name}-v${project.version}-all"

javafx {
    version = "26.0.2"
    modules("javafx.controls", "javafx.graphics", "javafx.fxml")
}

dependencies {
    implementation(project(":marquee-core"))

    val jfxVersion = "26.0.2"
    val jfxPlatforms = arrayOf("win", "linux", "mac")
    for (platform in jfxPlatforms) {
        implementation("org.openjfx:javafx-base:$jfxVersion:$platform")
        implementation("org.openjfx:javafx-controls:$jfxVersion:$platform")
        implementation("org.openjfx:javafx-graphics:$jfxVersion:$platform")
        implementation("org.openjfx:javafx-fxml:$jfxVersion:$platform")
    }
}

application {
    mainClass = "org.cs2103t.marquee.gui.MainApplication"
}

tasks.jar {
    archiveFileName = "${jarName}-lean.jar"
}

tasks.shadowJar {
    mainClass = "org.cs2103t.marquee.gui.AlternativeMain"
    archiveFileName = "${jarName}.jar"
}