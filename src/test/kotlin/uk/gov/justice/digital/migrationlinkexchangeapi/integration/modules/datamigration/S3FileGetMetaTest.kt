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

 
class GetMetaForFileIntegrationTest {
    @Test
    fun `should ping minio`() {
        // Get the IP of the minio container, given the hostname is set to minio
        val ip = java.net.InetAddress.getByName("minio").hostAddress
        // Check if the IP is not empty
        assertTrue(ip.isNotEmpty(), "Minio IP should not be empty")
    }

    @Test
    fun `should get head successfully`() {
        val s3Endpoint = URI.create("http://minio:9000")
        `given there is a uploaded file in`("file.txt", s3Endpoint)
        val s3Config = GetS3ClientConfig().getS3Client()
        val result =
            runBlocking {
                GetMetaForFile(
                    S3FileGetMeta(s3Config),
                ).run {
                    this(
                        path = "file.txt",
                    )
                }
            }
        `then the file meta content is correct`(result)
    }

    private fun `then the file meta content is correct`(response: Result<String>) {

    assertTrue(response.isSuccess)

    // Check the type of the response is a string.
    assertTrue(response.getOrNull() is String)

    // It must match the char length 32
    assertEquals(
        34,
        response.getOrNull()?.length,
    )

    // It must match the expected regex pattern.
    assertTrue(
        response.getOrNull()?.matches(Regex("^\"[a-f0-9]{32}\"$")) == true,
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
