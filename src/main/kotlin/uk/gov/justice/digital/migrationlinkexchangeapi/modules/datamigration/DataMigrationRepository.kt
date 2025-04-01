package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import org.springframework.data.jpa.repository.JpaRepository

interface DataMigrationRepository : JpaRepository<DataMigration, Long> {
  fun existsByChecksum(checksum: String): Boolean
}
