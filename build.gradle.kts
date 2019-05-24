import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.31"
}

group = "com.rsredsq.revolut"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.javalin:javalin:3.0.0.RC0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.github.microutils:kotlin-logging:1.6.26")
    implementation("cc.vileda:kotlin-openapi3-dsl:0.20.1")
    implementation("io.github.classgraph:classgraph:4.8.36")
    implementation("org.webjars:swagger-ui:3.17.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
    implementation("io.swagger.core.v3:swagger-models:2.0.8")
    implementation("org.jetbrains.exposed:exposed:0.13.7")


}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}