package uk.gov.justice.digital.migrationlinkexchangeapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MigrationLinkExchangeApi

fun main(args: Array<String>) {
  runApplication<MigrationLinkExchangeApi>(*args)
}
