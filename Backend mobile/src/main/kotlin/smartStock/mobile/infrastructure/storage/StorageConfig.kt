package smartStock.mobile.infrastructure.storage

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.S3Exception
import java.net.URI

@Configuration
@EnableConfigurationProperties(StorageProperties::class)
class StorageConfig {
    @Bean
    fun s3Client(properties: StorageProperties): S3Client =
        S3Client
            .builder()
            .endpointOverride(URI.create(properties.endpoint))
            .region(Region.of(properties.region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKey, properties.secretKey),
                ),
            ).serviceConfiguration(
                S3Configuration
                    .builder()
                    .pathStyleAccessEnabled(properties.pathStyleAccessEnabled)
                    .build(),
            ).build()

    @Bean
    fun ensureBucket(
        s3Client: S3Client,
        properties: StorageProperties,
    ): CommandLineRunner =
        CommandLineRunner {
            if (!properties.autoCreateBucket) {
                return@CommandLineRunner
            }

            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.bucket).build())
            } catch (_: NoSuchBucketException) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(properties.bucket).build())
            } catch (exception: S3Exception) {
                if (exception.statusCode() == 404) {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(properties.bucket).build())
                } else {
                    throw exception
                }
            }
        }
}
