plugins {
    id("java-common-conventions")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = findProperty("group")!!
version = findProperty(project.name + "-version")!!

javafx {
    version = "26.0.2"
    modules.add("javafx.base")
}