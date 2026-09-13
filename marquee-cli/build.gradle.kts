plugins {
    application
    id("java-common-conventions")
    id("org.graalvm.buildtools.native")
}

group = findProperty("group")!!
version = findProperty(project.name + "-version")!!

dependencies {
    implementation(project(":marquee-core"))
}

application {
    mainClass = "org.cs2103t.marquee.cli.Application"
}

graalvmNative {
    binaries.named("main") {
        imageName = "${project.name}-v${project.version}-windows-x64"
        buildArgs.add("--static-nolibc")
        buildArgs.add("-march=native")
        buildArgs.add("-O3")
    }
}

tasks.run {
    standardInput = System.`in`
}