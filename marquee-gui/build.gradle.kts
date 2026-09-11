plugins {
    application
    id("java-common-conventions")
    id("native-compile")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = findProperty("group")!!
version = findProperty(project.name + "-version")!!

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