package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DataSetInitializer(
  private val importer: DataSetImporter,

  @Value("\${app.migration-csv-url}")
  private val csvUrl: String,
) {

  @EventListener(ApplicationReadyEvent::class)
  fun onApplicationReady() {
    importer.importFromUrl(csvUrl)
  }
}
