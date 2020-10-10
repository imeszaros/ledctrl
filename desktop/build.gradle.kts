plugins {
    kotlin("jvm") version "1.4.10"
    application
    id("com.diffplug.eclipse.mavencentral") version "3.23.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("edu.sc.seis.launch4j") version "2.4.8"
}

group = "imeszaros.ledctrl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.fazecast:jSerialComm:2.6.2")
    implementation("com.typesafe:config:1.4.0")
}

application {
    mainClassName = "imeszaros.ledctrl.MainKt"
}

val swtBundleId = "org.eclipse.swt"

eclipseMavenCentral {
    release("4.16.0") {
        implementationNative(swtBundleId)
        useNativesForRunningPlatform()
    }
}

launch4j {
    productName = "LED Strip Controller"
    outfile = "ledctrl.exe"
    mainClassName = "imeszaros.ledctrl.MainKt"
    copyConfigurable = project.tasks.shadowJar.get().outputs.files
    jar = "$projectDir/build/libs/${project.tasks.shadowJar.get().archiveFileName.get()}"
    icon = "$projectDir/icon.ico"
    jreRuntimeBits = "64"
    headerType = "gui"
    stayAlive = true
    bundledJrePath = "jre"
    mutexName = "ledctrl"
}
