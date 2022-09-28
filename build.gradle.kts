plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.5.2"
    //id("org.jetbrains.intellij") version "0.6.5"
}

group = "site.duqian.plugin"
version = "1.0.3"

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
    //version.set("2021.2")
    //version.set("2021.2.1")
    version.set("2022.1.1") //2020.1 Could not resolve com.jetbrains:jbre:jbr_jcef-11_0_8-osx-aarch64-b944.34.
    type.set("IC") // Target IDE Platform
    //type.set("AI") // Target Android Studio Platform

    //plugins.set(listOf())
    plugins.set(listOf("android"))
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("org.freemarker:freemarker:2.3.31")

    // https://mvnrepository.com/artifact/com.hynnet/DJNativeSwing-SWT
    //implementation("com.hynnet:DJNativeSwing-SWT:1.0.0")
    //implementation("chrriis:DJNativeSwing-SWT:+")

    // https://mvnrepository.com/artifact/org.eclipse.platform/org.eclipse.swt.win32.win32.x86_64
    //implementation("org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64:+")

    // https://mvnrepository.com/artifact/org.eclipse.platform/org.eclipse.swt.cocoa.macosx.x86_64
    //implementation("org.eclipse.platform:org.eclipse.swt.cocoa.macosx.x86_64:3.121.0")

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }
    /*withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }*/

    patchPluginXml {
        //sinceBuild.set("212")
        sinceBuild.set("202")
        //untilBuild.set("222.*")
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
