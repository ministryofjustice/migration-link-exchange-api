package uk.gov.justice.digital.migrationlinkexchangeapi.common

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FileInformationRepository : JpaRepository<FileInformation, String> {
  fun findAllByGoogleOwnerEmail(googleOwnerEmail: String): MutableList<FileInformation>
  fun findByGoogleUrl(googleUrl: String): MutableList<FileInformation>
  fun findByGoogleFileId(googleFileId: String): MutableList<FileInformation>
}
