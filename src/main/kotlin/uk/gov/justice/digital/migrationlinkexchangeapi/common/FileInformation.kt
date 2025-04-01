package uk.gov.justice.digital.migrationlinkexchangeapi.common

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "file_information")
data class FileInformation(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "google_file_id", nullable = false)
  val googleFileId: String,

  @Column(name = "google_file_name")
  val googleFileName: String,

  @Column(name = "google_path")
  val googlePath: String,

  @Column(name = "google_url")
  val googleUrl: String,

  @Column(name = "google_owner_email")
  val googleOwnerEmail: String,

  @Column(name = "google_last_accessed_time")
  val googleLastAccessedTime: OffsetDateTime,

  @Column(name = "google_last_modifying_user")
  val googleLastModifyingUser: String,

  @Column(name = "microsoft_url")
  val microsoftUrl: String,

  @Column(name = "microsoft_path")
  val microsoftPath: String,

  @Column(name = "microsoft_file_type")
  val microsoftFileType: String
)