/**
 * This file was copied and adapted from the original file at
 * https://github.com/isamadrid90/aws-kotlin-examples/tree/main/download-s3-file
 */

package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.net.Host
import aws.smithy.kotlin.runtime.net.Scheme
import aws.smithy.kotlin.runtime.net.url.Url
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import java.nio.charset.StandardCharsets
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue


class DownloadFileIntegrationTest {
    @Test
    fun `should download file successfully`() {
        val s3Endpoint = URI.create("http://minio:9000")
        `given there is a uploaded file in`("file.txt", s3Endpoint)
        val result =
            runBlocking {
                DownloadFile(
                    S3FileDownloader(
                        S3ClientConfig(
                            bucketName = "dev-bucket",
                            region = "us-east-1",
                            url = s3Endpoint.toURL(),
                            forcePathStyle = true,
                            credentials =
                                StaticCredentialsProvider {
                                    accessKeyId = "minio-user"
                                    secretAccessKey = "minio-pass"
                                },
                        ),
                    ),
                ).run {
                    this(
                        path = "file.txt",
                    )
                }
            }
        `then the file downloaded content is correct`(result)
    }

    private fun `then the file downloaded content is correct`(response: Result<String>) {

        assertTrue(response.isSuccess)
        assertEquals(
            "Hello world!",
            response.getOrNull(),
        )
        assertEquals(
            InputStreamReader(file.inputStream(), StandardCharsets.UTF_8).readText(),
            response.getOrNull(),
        )
    }

    private fun `given there is a uploaded file in`(
        path: String,
        s3Endpoint: URI,
    ) {
        val s3Client =
            S3Client {
                region = "us-east-1"
                endpointUrl =
                    Url {
                        scheme = Scheme.parse(s3Endpoint.toURL().protocol)
                        host = Host.parse(s3Endpoint.toURL().host)
                        port = s3Endpoint.toURL().port
                    }
                credentialsProvider =
                    StaticCredentialsProvider {
                        accessKeyId = "minio-user"
                        secretAccessKey = "minio-pass"
                    }
                forcePathStyle = true
            }
        runBlocking {
            s3Client.use {
                val res =
                    it.putObject(
                        PutObjectRequest {
                            bucket = "dev-bucket"
                            key = "file.txt"
                            body = file.asByteStream()
                        },
                    )
            }
        }
    }

    companion object {
        private val file = File("./src/test/kotlin/uk/gov/justice/digital/migrationlinkexchangeapi/integration/modules/datamigration/file.txt")
    }
}
