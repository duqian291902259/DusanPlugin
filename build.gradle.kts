plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.5.2"
    //id("org.jetbrains.intellij") version "0.6.5"
}

group = "site.duqian.plugin"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    mavenLocal()
    maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { setUrl("https://maven.aliyun.com/repository/central") }
    maven { setUrl("https://maven.aliyun.com/repository/public") }
    maven { setUrl("https://maven.aliyun.com/repository/google") }
    maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
    maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { setUrl("https://jitpack.io") }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.2")
    //version.set("2020.2.3") //Could not resolve com.jetbrains:jbre:jbr_jcef-11_0_8-osx-aarch64-b944.34.
    //type.set("IC") // Target IDE Platform

    //plugins.set(listOf())
}

dependencies {
    //implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation("org.freemarker:freemarker:2.3.31")
    // https://mvnrepository.com/artifact/com.hynnet/DJNativeSwing-SWT
    //implementation("com.hynnet:DJNativeSwing-SWT:1.0.0")
    //implementation("chrriis:DJNativeSwing-SWT:+")

    // https://mvnrepository.com/artifact/org.eclipse.platform/org.eclipse.swt.win32.win32.x86_64
    //implementation("org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64:+")

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    /*withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }*/

    patchPluginXml {
        sinceBuild.set("212")
        //sinceBuild.set("202.60")
        untilBuild.set("222.*")
    }

   /* signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }*/
}
