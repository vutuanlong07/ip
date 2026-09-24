plugins {
    application
    id("java-common-conventions")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "9.5.1"
}

group = findProperty("group")!!
version = findProperty(project.name + "-version")!!

val platform = (findProperty("myProperty") ?: "unknown").toString()

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
    archiveFileName = "${project.name}-v${project.version}-${platform}-lean.jar"
}

tasks.shadowJar {
    mainClass = "org.cs2103t.marquee.gui.AlternativeMain"
    archiveFileName = "${project.name}-v${project.version}-${platform}.jar"
}