plugins {
    java
    `java-library`
    application
}

group = "org.cs2103t.marquee"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    api(project(":marquee-core"))
}

tasks.test {
    useJUnitPlatform()
}