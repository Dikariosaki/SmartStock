package smartStock.mobile.infrastructure.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.storage")
data class StorageProperties(
    var endpoint: String = "http://localhost:9000",
    var region: String = "us-east-1",
    var accessKey: String = "rustfsadmin",
    var secretKey: String = "rustfsadmin",
    var bucket: String = "reportes-evidencia",
    var publicBaseUrl: String = "http://localhost:9000",
    var autoCreateBucket: Boolean = true,
    var pathStyleAccessEnabled: Boolean = true,
)
