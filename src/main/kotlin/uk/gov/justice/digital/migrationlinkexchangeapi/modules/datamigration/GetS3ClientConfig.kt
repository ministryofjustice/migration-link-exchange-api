package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.net.Host
import aws.smithy.kotlin.runtime.net.Scheme
import aws.smithy.kotlin.runtime.net.url.Url
import java.net.URI

class GetS3ClientConfig {
    fun getCredentials(): StaticCredentialsProvider? {
        return if (System.getenv("MINIO_USER") != null && System.getenv("MINIO_PASSWORD") != null) {
            StaticCredentialsProvider {
                accessKeyId = System.getenv("MINIO_USER")
                secretAccessKey = System.getenv("MINIO_PASSWORD")
            }
        } else {
            null
        }
    }

    fun getUrl(): Url? {
        return if(System.getenv("MINIO_ENDPOINT") == null) {
            null;
        } else {
            val url = URI.create(System.getenv("MINIO_ENDPOINT")).toURL()
            Url {
                scheme = Scheme.parse(url.protocol)
                host = Host.parse(url.host)
                port = url.port
            }
        }
    }

    fun getS3Client(): S3ClientConfig {
        return S3ClientConfig(
            bucketName = System.getenv("S3_BUCKET"),
            region = System.getenv("S3_REGION"),
            credentials = getCredentials(),
            endpointUrl = getUrl(),
            forcePathStyle = System.getenv("MINIO_ENDPOINT") != null,
        );
    }

}


data class S3ClientConfig(
    val bucketName: String,
    val region: String,
    val credentials: CredentialsProvider? = null,
    val endpointUrl: Url? = null,
    var forcePathStyle: Boolean = false,
)
