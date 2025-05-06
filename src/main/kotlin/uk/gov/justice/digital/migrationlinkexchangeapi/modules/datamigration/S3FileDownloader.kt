/**
 * This file was copied and adapted from the original file at
 * https://github.com/isamadrid90/aws-kotlin-examples/tree/main/download-s3-file
 */

package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.content.decodeToString
import aws.smithy.kotlin.runtime.net.Host
import aws.smithy.kotlin.runtime.net.Scheme
import aws.smithy.kotlin.runtime.net.url.Url
import java.net.URL

class S3FileDownloader(private val s3ClientConfig: S3ClientConfig) : FileDownloader {
    private val client =
        S3Client {
            region = s3ClientConfig.region
            endpointUrl = s3ClientConfig.endpointUrl
            credentialsProvider = s3ClientConfig.credentials ?: null
            forcePathStyle = s3ClientConfig.forcePathStyle
        }

    override suspend fun invoke(path: String): Result<String?> {
        return runCatching {
            client.getObject(
                GetObjectRequest {
                    bucket = s3ClientConfig.bucketName
                    key = path
                },
            ) { response ->
                response.body?.decodeToString()
            }
        }
    }
}

