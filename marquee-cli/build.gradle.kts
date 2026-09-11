plugins {
    java
    application
    id("org.graalvm.buildtools.native") version "1.1.12"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(project(":marquee-core"))
}

application {
    mainClass = "org.cs2103t.marquee.cli.Application"
}

graalvmNative {
    binaries.all {
        buildArgs.add("--static-nolibc")
        buildArgs.add("-march=native")
        buildArgs.add("-O3")
    }
}

tasks.run {
    standardInput = System.`in`
}

tasks.test {
    useJUnitPlatform()
}