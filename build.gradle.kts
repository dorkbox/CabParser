/*
 * Copyright 2020 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.time.Instant


///////////////////////////////
//////    PUBLISH TO SONATYPE / MAVEN CENTRAL
////// TESTING : (to local maven repo) <PUBLISHING - publishToMavenLocal>
////// RELEASE : (to sonatype/maven central), <PUBLISHING - publishToSonatypeAndRelease>
///////////////////////////////

gradle.startParameter.showStacktrace = ShowStacktrace.ALWAYS_FULL   // always show the stacktrace!
gradle.startParameter.warningMode = WarningMode.All

println("\tGradle ${project.gradle.gradleVersion} on Java ${JavaVersion.current()}")

plugins {
    java

    id("com.dorkbox.CrossCompile") version "1.0.1"
    id("com.dorkbox.Licensing") version "1.4"
    id("com.dorkbox.VersionUpdate") version "1.4.1"
    id("com.dorkbox.GradleUtils") version "1.5"
    id("com.dorkbox.GradlePublish") version "1.0"

    kotlin("jvm") version "1.3.72"
}

object Extras {
    // set for the project
    const val description = "Parse and extract data from Microsoft LZX compressed .cab files for Java 6+"
    const val group = "com.dorkbox"
    const val version = "2.1"

    // set as project.ext
    const val name = "CabParser"
    const val id = "CabParser"
    const val vendor = "Dorkbox LLC"
    const val vendorUrl = "https://dorkbox.com"
    const val url = "https://git.dorkbox.com/dorkbox/CabParser"
    val buildDate = Instant.now().toString()

    val JAVA_VERSION = JavaVersion.VERSION_1_6.toString()

    var sonatypeUserName = ""
    var sonatypePassword = ""
    var sonatypePrivateKeyFile = ""
    var sonatypePrivateKeyPassword = ""
}

///////////////////////////////
/////  assign 'Extras'
///////////////////////////////
GradleUtils.load("$projectDir/../../gradle.properties", Extras)
description = Extras.description
group = Extras.group
version = Extras.version


licensing {
    license(License.APACHE_2) {
        author(Extras.vendor)
        url(Extras.url)
        note(Extras.description)
    }


    license("Dorkbox Utils", License.APACHE_2) {
        author(Extras.vendor)
        url("https://git.dorkbox.com/dorkbox/Utilities")
    }

    license("jOOU - Unsigned Numbers for Java", License.APACHE_2) {
        copyright( 2013)
        author("Lukas Eder, lukas.eder@gmail.com")
        url("https://github.com/jOOQ/jOOU")
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src"))

            // want to include java files for the source. 'setSrcDirs' resets includes...
            include("**/*.java")
        }
    }
}

repositories {
    mavenLocal() // this must be first!
    jcenter()
}

///////////////////////////////
//////    Task defaults
///////////////////////////////
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    sourceCompatibility = Extras.JAVA_VERSION
    targetCompatibility = Extras.JAVA_VERSION
}

tasks.jar.get().apply {
    manifest {
        // https://docs.oracle.com/javase/tutorial/deployment/jar/packageman.html
        attributes["Name"] = Extras.name

        attributes["Specification-Title"] = Extras.name
        attributes["Specification-Version"] = Extras.version
        attributes["Specification-Vendor"] = Extras.vendor

        attributes["Implementation-Title"] = "${Extras.group}.${Extras.id}"
        attributes["Implementation-Version"] = Extras.buildDate
        attributes["Implementation-Vendor"] = Extras.vendor

        attributes["Automatic-Module-Name"] = Extras.id
    }
}

tasks.compileJava.get().apply {
    println("\tCompiling classes to Java $sourceCompatibility")
}

dependencies {
    implementation("com.dorkbox:Utilities:1.1")
}

publishToSonatype {
    groupId = Extras.group
    artifactId = Extras.id
    version = Extras.version

    name = Extras.name
    description = Extras.description
    url = Extras.url

    vendor = Extras.vendor
    vendorUrl = Extras.vendorUrl

    issueManagement {
        url = "${Extras.url}/issues"
        nickname = "Gitea Issues"
    }

    developer {
        id = "dorkbox"
        name = Extras.vendor
        email = "email@dorkbox.com"
    }

    sonatype {
        userName = Extras.sonatypeUserName
        password = Extras.sonatypePassword
    }

    privateKey {
        fileName = Extras.sonatypePrivateKeyFile
        password = Extras.sonatypePrivateKeyPassword
    }
}
