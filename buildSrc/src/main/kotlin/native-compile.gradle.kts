plugins {
    java
    application
    id("org.graalvm.buildtools.native")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

graalvmNative {
    binaries.named("main") {
        logger.lifecycle(project.name)
        logger.lifecycle(project.version.toString())
        imageName = "${project.name}-v${findProperty(project.name + "-version")}-windows-x64"
        buildArgs.add("--static-nolibc")
        buildArgs.add("-march=native")
        buildArgs.add("-O3")
    }
}

tasks.nativeCompile {
    doFirst {
        logger.lifecycle(graalvmNative.binaries.named("main").get().imageName.get())
    }
    outputDirectory = file("releases/${findProperty(project.name + "-version")}/");
}