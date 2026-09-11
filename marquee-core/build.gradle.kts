plugins {
    id("java-common-conventions")
}

group = findProperty("group")!!
version = findProperty(project.name + "-version")!!