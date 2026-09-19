plugins {
    application
    id("java-common-conventions")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gluonhq.gluonfx-gradle-plugin") version "1.0.29"
    id("com.gradleup.shadow") version "9.5.1"
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
    mainClass = "org.javafx.MainApplication"
}

tasks.jar {
    enabled = false
    archiveClassifier = "without-dependencies"
}

tasks.shadowJar {
    archiveFileName = "${project.name}-v${project.version}-jar.jar"
}