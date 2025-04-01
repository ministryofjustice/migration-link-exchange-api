package uk.gov.justice.digital.migrationlinkexchangeapi.modules.exchangelink

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/link")
class ExchangeLinkController(
  private val service: ExchangeLinkService,
) {

  @GetMapping
  fun getFilesByUrl(
    @RequestParam("q") url: String,
  ): ResponseEntity<out Any>? = when (val result = service.getAllFileInformationByGoogleUrl(url)) {
    is FileLookupResult.InvalidUrl -> ResponseEntity.badRequest().body("Invalid Google Drive URL")
    is FileLookupResult.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("No matching files found")
    is FileLookupResult.Success -> ResponseEntity.ok(result.files)
  }

  @GetMapping("/owner")
  fun getOwnerByUrl(
    @RequestParam("q") url: String,
  ): ResponseEntity<out Any>? = when (val result = service.getAllFileInformationByGoogleUrl(url)) {
    is FileLookupResult.InvalidUrl -> ResponseEntity.badRequest().body("Invalid Google Drive URL")
    is FileLookupResult.NotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("No matching files found")
    is FileLookupResult.Success -> ResponseEntity.ok(result.files.first().googleOwnerEmail)
  }
}
