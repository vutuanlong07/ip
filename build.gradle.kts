plugins {
    checkstyle
}

group = "org.cs2103t.marquee"
version = "1.0.0"

repositories {
    mavenCentral()
}

checkstyle {
    configFile = file("config/checkstyle/checkstyle.xml")
}