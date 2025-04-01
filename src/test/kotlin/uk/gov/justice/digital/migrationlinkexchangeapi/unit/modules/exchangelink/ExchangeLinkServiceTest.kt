package uk.gov.justice.digital.migrationlinkexchangeapi.unit.modules.exchangelink

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.migrationlinkexchangeapi.common.FileInformation
import uk.gov.justice.digital.migrationlinkexchangeapi.common.FileInformationRepository
import uk.gov.justice.digital.migrationlinkexchangeapi.modules.exchangelink.ExchangeLinkService
import uk.gov.justice.digital.migrationlinkexchangeapi.modules.exchangelink.FileLookupResult
import java.time.OffsetDateTime

@ExtendWith(MockKExtension::class)
class ExchangeLinkServiceTest {

  private val repository: FileInformationRepository = mockk()
  private val service = ExchangeLinkService(repository)

  @Test
  fun `should return InvalidUrl for non google url`() {
    val invalidUrl = "https://example.com/file/d/abc123/view"

    val result = service.getAllFileInformationByGoogleUrl(invalidUrl)

    assertEquals(FileLookupResult.InvalidUrl, result)
  }

  @Test
  fun `should return NotFound for valid google url but repository returns empty list`() {
    val validUrl = "https://drive.google.com/file/d/abc123/view"
    every { repository.findByGoogleFileId("abc123") } returns emptyList<FileInformation>().toMutableList()

    val result = service.getAllFileInformationByGoogleUrl(validUrl)

    assertEquals(FileLookupResult.NotFound, result)
  }

  @Test
  fun `should return Success for valid google url with file information`() {
    val validUrl = "https://drive.google.com/file/d/abc123/view"
    val fileInfo = FileInformation(
      googleFileId = "abc123",
      googleFileName = "test-file.pdf",
      googlePath = "/My Drive/test-folder/test-file.pdf",
      googleUrl = validUrl,
      googleOwnerEmail = "user@example.com",
      googleLastAccessedTime = OffsetDateTime.parse("2024-03-01T10:15:30+01:00"),
      googleLastModifyingUser = "modifier@example.com",
      microsoftUrl = "https://sharepoint.com/sites/team/Shared%20Documents/test-file.docx",
      microsoftPath = "/Shared Documents/test-folder/test-file.docx",
      microsoftFileType = "docx",
    )
    every { repository.findByGoogleFileId("abc123") } returns listOf(fileInfo).toMutableList()

    val result = service.getAllFileInformationByGoogleUrl(validUrl)
    assertTrue(result is FileLookupResult.Success)

    result as FileLookupResult.Success
    assertEquals(1, result.files.size)
    assertEquals(fileInfo, result.files[0])
  }

  @Test
  fun `should extract file id from docs google url`() {
    val validUrl = "https://docs.google.com/file/d/xyz789/view"
    val fileInfo = FileInformation(
      googleFileId = "xyz789",
      googleFileName = "doc-file.pdf",
      googlePath = "/My Drive/test-folder/doc-file.pdf",
      googleUrl = validUrl,
      googleOwnerEmail = "docuser@example.com",
      googleLastAccessedTime = OffsetDateTime.parse("2024-03-02T11:00:00+01:00"),
      googleLastModifyingUser = "docmodifier@example.com",
      microsoftUrl = "https://sharepoint.com/sites/team/Shared%20Documents/doc-file.docx",
      microsoftPath = "/Shared Documents/test-folder/doc-file.docx",
      microsoftFileType = "docx",
    )
    every { repository.findByGoogleFileId("xyz789") } returns listOf(fileInfo).toMutableList()

    val result = service.getAllFileInformationByGoogleUrl(validUrl)
    assertTrue(result is FileLookupResult.Success)

    result as FileLookupResult.Success
    assertEquals(1, result.files.size)
    assertEquals(fileInfo, result.files[0])
  }
}
