/**
 * This file was copied and adapted from the original file at
 * https://github.com/isamadrid90/aws-kotlin-examples/tree/main/download-s3-file
 */

package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.HeadObjectRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.content.decodeToString
import aws.smithy.kotlin.runtime.net.Host
import aws.smithy.kotlin.runtime.net.Scheme
import aws.smithy.kotlin.runtime.net.url.Url
import java.net.URL

class S3FileGetMeta(private val s3ClientConfig: S3ClientConfig) : FileGetMeta {
    private val client =
        S3Client {
            region = s3ClientConfig.region
            endpointUrl =
                Url {
                    scheme = Scheme.parse(s3ClientConfig.url.protocol)
                    host = Host.parse(s3ClientConfig.url.host)
                    port = s3ClientConfig.url.port
                }
            credentialsProvider = s3ClientConfig.credentials
            forcePathStyle = s3ClientConfig.forcePathStyle
        }

    override suspend fun invoke(path: String): Result<String?> {
        return runCatching {
            client.getObject(
                HeadObjectRequest {
                    bucket = s3ClientConfig.bucketName
                    key = path
                },
            ) { response ->
                response.body?.decodeToString()
            }
        }
    }
}

data class S3ClientConfig(
    val bucketName: String,
    val region: String,
    val url: URL,
    val credentials: CredentialsProvider,
    var forcePathStyle: Boolean = false,
)
