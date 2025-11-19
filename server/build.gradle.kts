plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "org.ufku.ii12task"
version = "1.0.0"
application {
    mainClass.set("org.ufku.ii12task.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(dependencies.platform(libs.ktor.bom))
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.mcp.kotlin.server)
    implementation(libs.slf4j.simple)
    implementation("io.ktor:ktor-client-logging:3.3.1")
    testImplementation(libs.kotlin.testJunit)
}