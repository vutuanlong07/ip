plugins {
    java
    `java-library`
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    api(project(":marquee-core"))
}

application {
    mainClass = "org.cs2103t.marquee.cli.Main";
}

tasks.test {
    useJUnitPlatform()
}