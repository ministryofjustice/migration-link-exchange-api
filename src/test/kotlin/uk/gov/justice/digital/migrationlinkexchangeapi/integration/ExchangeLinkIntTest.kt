package uk.gov.justice.digital.migrationlinkexchangeapi.integration

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.migrationlinkexchangeapi.modules.exchangelink.*
import uk.gov.justice.digital.migrationlinkexchangeapi.common.FileInformation
import java.time.OffsetDateTime

@Import(ExchangeLinkControllerIntTest.TestConfig::class)
class ExchangeLinkControllerIntTest : IntegrationTestBase() {

  @Autowired
  lateinit var service: ExchangeLinkService

  companion object {
    val mockService: ExchangeLinkService = mockk(relaxed = true)
  }

  @TestConfiguration
  class TestConfig {
    @Bean
    fun exchangeLinkService(): ExchangeLinkService = mockService
  }

  private val validUrl = "https://drive.google.com/file/d/abc123/view"

  private val fileInfo = FileInformation(
    googleFileId = "abc123",
    googleFileName = "test-file.pdf",
    googlePath = "/My Drive/test-folder/test-file.pdf",
    googleUrl = "https://drive.google.com/file/d/abc123/view",
    googleOwnerEmail = "user@example.com",
    googleLastAccessedTime = OffsetDateTime.parse("2024-03-01T10:15:30+01:00"),
    googleLastModifyingUser = "modifier@example.com",
    microsoftUrl = "https://sharepoint.com/sites/team/Shared%20Documents/test-file.docx",
    microsoftPath = "/Shared Documents/test-folder/test-file.docx",
    microsoftFileType = "docx"
  )

  @Nested
  @DisplayName("GET /link")
  inner class GetFilesByUrl {

    @Test
    fun `should return 400 for invalid URL`() {
      every { service.getAllFileInformationByGoogleUrl("invalid") } returns FileLookupResult.InvalidUrl

      webTestClient.get()
        .uri { it.path("/link").queryParam("q", "invalid").build() }
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("$").isEqualTo("Invalid Google Drive URL")
    }

    @Test
    fun `should return 404 when no matching files`() {
      every { service.getAllFileInformationByGoogleUrl(validUrl) } returns FileLookupResult.NotFound

      webTestClient.get()
        .uri { it.path("/link").queryParam("q", validUrl).build() }
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("$").isEqualTo("No matching files found")
    }

    @Test
    fun `should return 200 with complete file data`() {
      every { service.getAllFileInformationByGoogleUrl(validUrl) } returns FileLookupResult.Success(listOf(fileInfo))

      webTestClient.get()
        .uri { it.path("/link").queryParam("q", validUrl).build() }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[0].googleFileId").isEqualTo("abc123")
        .jsonPath("$[0].googleFileName").isEqualTo("test-file.pdf")
        .jsonPath("$[0].googlePath").isEqualTo("/My Drive/test-folder/test-file.pdf")
        .jsonPath("$[0].googleUrl").isEqualTo("https://drive.google.com/file/d/abc123/view")
        .jsonPath("$[0].googleOwnerEmail").isEqualTo("user@example.com")
        .jsonPath("$[0].googleLastAccessedTime").isEqualTo("2024-03-01T10:15:30+01:00")
        .jsonPath("$[0].googleLastModifyingUser").isEqualTo("modifier@example.com")
        .jsonPath("$[0].microsoftUrl").isEqualTo("https://sharepoint.com/sites/team/Shared%20Documents/test-file.docx")
        .jsonPath("$[0].microsoftPath").isEqualTo("/Shared Documents/test-folder/test-file.docx")
        .jsonPath("$[0].microsoftFileType").isEqualTo("docx")
    }
  }

  @Nested
  @DisplayName("GET /link/owner")
  inner class GetOwnerByUrl {

    @Test
    fun `should return 400 for invalid URL`() {
      every { service.getAllFileInformationByGoogleUrl("invalid") } returns FileLookupResult.InvalidUrl

      webTestClient.get()
        .uri { it.path("/link/owner").queryParam("q", "invalid").build() }
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("$").isEqualTo("Invalid Google Drive URL")
    }

    @Test
    fun `should return 404 when no owner found`() {
      every { service.getAllFileInformationByGoogleUrl(validUrl) } returns FileLookupResult.NotFound

      webTestClient.get()
        .uri { it.path("/link/owner").queryParam("q", validUrl).build() }
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("$").isEqualTo("No matching files found")
    }

    @Test
    fun `should return 200 with owner email`() {
      every { service.getAllFileInformationByGoogleUrl(validUrl) } returns FileLookupResult.Success(listOf(fileInfo))

      webTestClient.get()
        .uri { it.path("/link/owner").queryParam("q", validUrl).build() }
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("$").isEqualTo("user@example.com")
    }
  }
}