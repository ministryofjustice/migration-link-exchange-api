package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.springframework.stereotype.Service
import uk.gov.justice.digital.migrationlinkexchangeapi.common.FileInformation
import uk.gov.justice.digital.migrationlinkexchangeapi.common.FileInformationRepository
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest
import java.time.OffsetDateTime

@Service
class DataSetImporter(
  private val fileInformationRepository: FileInformationRepository,
  private val migrationRepo: DataMigrationRepository,
) {

  fun downloadFileToByteArray(url: String): ByteArray = URL(url).readBytes()

  fun sha256Hash(inputStream: InputStream): String {
    val buffer = ByteArray(1024)
    val digest = MessageDigest.getInstance("SHA-256")
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
      digest.update(buffer, 0, bytesRead)
    }
    return digest.digest().joinToString(separator = "") { "%02x".format(it) }
  }

  fun importFromUrl(fileUrl: String) {
    val csvBytes = try {
      downloadFileToByteArray(fileUrl)
    } catch (e: Exception) {
      println("Unable to download file from $fileUrl: ${e.message}. Skipping import.")
      return
    }

    val checksum = sha256Hash(ByteArrayInputStream(csvBytes))
    if (migrationRepo.existsByChecksum(checksum)) {
      println("Migration with checksum $checksum has already been applied. Skipping.")
      return
    }

    val fileInformationBatch = mutableListOf<FileInformation>()
    val batchSize = 500

    csvReader().open(ByteArrayInputStream(csvBytes)) {
      val rowsSequence = readAllWithHeaderAsSequence()
      rowsSequence.forEach { rowMap ->
        val fileInfo = FileInformation(
          googleFileId = rowMap["googleFileId"] ?: "",
          googleFileName = rowMap["googleFileName"] ?: "",
          googlePath = rowMap["googlePath"] ?: "",
          googleUrl = rowMap["googleUrl"] ?: "",
          googleOwnerEmail = rowMap["googleOwnerEmail"] ?: "",
          googleLastAccessedTime = OffsetDateTime.parse(rowMap["googleLastAccessedTime"]),
          googleLastModifyingUser = rowMap["googleLastModifyingUser"] ?: "",
          microsoftUrl = rowMap["microsoftUrl"] ?: "",
          microsoftPath = rowMap["microsoftPath"] ?: "",
          microsoftFileType = rowMap["microsoftFileType"] ?: "",
        )
        fileInformationBatch.add(fileInfo)

        if (fileInformationBatch.size >= batchSize) {
          fileInformationRepository.saveAll(fileInformationBatch)
          fileInformationBatch.clear()
        }
      }
    }

    if (fileInformationBatch.isNotEmpty()) {
      fileInformationRepository.saveAll(fileInformationBatch)
    }

    migrationRepo.save(DataMigration(checksum = checksum))
    println("Migration applied with checksum $checksum")
  }
}
