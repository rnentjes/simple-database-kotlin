import org.jetbrains.kotlin.konan.library.impl.buildLibrary

plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jetbrains.dokka") version "1.9.10"
    `maven-publish`
    `java-library`
}

allprojects {
    group = "nl.astraeus"
    version = "2.0.8-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://nexus.astraeus.nl/nexus/content/groups/public")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:1.4.196")

    api("nl.astraeus:simple-database:2.0.8-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

/*val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}*/

publishing {
    repositories {
        maven {
            name = "releases"
            // change to point to your repo, e.g. http://my.org/repo
            setUrl("https://reposilite.astraeus.nl/releases")
            credentials {
                val reposiliteUsername: String? by project
                val reposilitePassword: String? by project

                username = reposiliteUsername
                password = reposilitePassword
            }
        }
        maven {
            name = "snapshots"
            // change to point to your repo, e.g. http://my.org/repo
            setUrl("https://reposilite.astraeus.nl/snapshots")
            credentials {
                val reposiliteUsername: String? by project
                val reposilitePassword: String? by project

                username = reposiliteUsername
                password = reposilitePassword
            }
        }
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                val ossrhUsername: String? by project
                val ossrhPassword: String? by project

                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            // Define the group ID, artifact ID, and version
            groupId = "nl.astraeus"
            artifactId = "simple-database-kotlin"
            version = "2.0.8-SNAPSHOT"

            // You can also include sources and documentation JARs
            artifact(sourcesJar.get())
            //artifact(javadocJar.get())
        }
    }
}
