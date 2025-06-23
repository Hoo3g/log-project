plugins {
    java
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"
// Chính xác, sử dụng Java 21
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    // PostgreSQL JDBC Driver
    implementation("org.postgresql:postgresql:42.7.3")

    // OpenSearch Java Client (cấp cao)
    implementation("org.opensearch.client:opensearch-java:2.12.0")

    // OpenSearch REST Client (cấp thấp) - THƯ VIỆN BỊ THIẾU
    // Dòng này sẽ sửa lỗi "Cannot resolve symbol 'RestClient'"
    implementation("org.opensearch.client:opensearch-rest-client:2.12.0")

    // Jackson, bắt buộc cho opensearch-java client
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    // Apache HttpComponents, bắt buộc cho RestClient
    // Dòng này sẽ sửa lỗi "Cannot resolve symbol 'HttpHost'"
    implementation("org.apache.httpcomponents.core5:httpcore5:5.2.4")

    // Logger đơn giản
    implementation("org.slf4j:slf4j-simple:2.0.7")
}

// Các task tùy chỉnh để chạy ứng dụng của bạn vẫn hoàn hảo, không cần thay đổi
tasks.register<JavaExec>("runIngestor") {
    group = "Application"
    description = "Runs the LogIngestor to send data to OpenSearch."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("LogIngestor")
}

tasks.register<JavaExec>("runSearcher") {
    group = "Application"
    description = "Runs the LogSearcher to query data from OpenSearch."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("LogSearcher")
}