package uk.gov.justice.digital.migrationlinkexchangeapi.modules.exchangelink

import org.springframework.stereotype.Service
import uk.gov.justice.digital.migrationlinkexchangeapi.common.FileInformation
import uk.gov.justice.digital.migrationlinkexchangeapi.common.FileInformationRepository

@Service
class ExchangeLinkService(
  private val repository: FileInformationRepository
) {
  private fun extractGoogleFileIdFromGoogleUrl(url: String): String? {
    val allowedDomainPattern = Regex("^https?://(?:docs\\.google|drive\\.google)\\.com")
    val idExtractionPattern = Regex("/(?:d|folders)/([a-zA-Z0-9_-]+)")

    return url
      .takeIf { allowedDomainPattern.containsMatchIn(it) }
      ?.let { idExtractionPattern.find(it)?.groupValues?.get(1) }
  }

  fun getAllFileInformationByGoogleUrl(url: String): FileLookupResult {
    val fileId = extractGoogleFileIdFromGoogleUrl(url)
      ?: return FileLookupResult.InvalidUrl

    return repository.findByGoogleFileId(fileId)
      .takeIf { it.isNotEmpty() }
      ?.let { FileLookupResult.Success(it) }
      ?: FileLookupResult.NotFound
  }
}

sealed class FileLookupResult {
  data class Success(val files: List<FileInformation>) : FileLookupResult()
  data object InvalidUrl : FileLookupResult()
  data object NotFound : FileLookupResult()
}