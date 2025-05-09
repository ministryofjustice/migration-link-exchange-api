package uk.gov.justice.digital.migrationlinkexchangeapi.common

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "file_information")
data class FileInformation(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "google_file_id", nullable = false)
  val googleFileId: String,

  @Column(name = "google_file_name", columnDefinition = "TEXT")
  val googleFileName: String,

  @Column(name = "google_path", columnDefinition = "TEXT")
  val googlePath: String,

  @Column(name = "google_url", columnDefinition = "TEXT")
  val googleUrl: String,

  @Column(name = "google_owner_email")
  val googleOwnerEmail: String,

  @Column(name = "google_last_accessed_time")
  val googleLastAccessedTime: OffsetDateTime? = null,

  @Column(name = "google_last_modifying_user")
  val googleLastModifyingUser: String,

  @Column(name = "microsoft_url", columnDefinition = "TEXT")
  val microsoftUrl: String,

  @Column(name = "microsoft_path", columnDefinition = "TEXT")
  val microsoftPath: String,

  @Column(name = "microsoft_file_type")
  val microsoftFileType: String,
)
