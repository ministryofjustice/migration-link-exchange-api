package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "data_migrations")
data class DataMigration(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0,
  @Column(unique = true, nullable = false)
  val checksum: String,
  val appliedAt: OffsetDateTime = OffsetDateTime.now()
)