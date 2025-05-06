package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.springframework.stereotype.Service
import uk.gov.justice.digital.migrationlinkexchangeapi.common.FileInformation
import uk.gov.justice.digital.migrationlinkexchangeapi.common.FileInformationRepository
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.OffsetDateTime

@Service
class DataSetImporter(
  private val fileInformationRepository: FileInformationRepository,
  private val migrationRepo: DataMigrationRepository,
) {

  val s3Config = GetS3ClientConfig().getS3Client()

  fun getEtag(path: String): String {
    val result = runBlocking {
        GetMetaForFile(
          S3FileGetMeta(s3Config),
        ).run {
          this(
              path = path,
          )
        }
      }
    return result.getOrThrow()
  }

  fun downloadFileToPath(sourcePath: String, destinationPath: Path): String {
    val result =  runBlocking {
        DownloadFile(
          S3FileDownloader(s3Config),
        ).run {
          this(
            sourcePath = sourcePath,
            destinationPath = destinationPath,
          )
        }
      }
    return result.getOrThrow()
  }

  fun parseGoogleLastAccessedTime(
    googleLastAccessedTime: String?,
  ): OffsetDateTime? {
    return if (googleLastAccessedTime == null || googleLastAccessedTime == "N/A") {
      null
    } else {
      OffsetDateTime.parse(googleLastAccessedTime)
    }
  }

  fun sha256Hash(inputStream: InputStream): String {
    val buffer = ByteArray(1024)
    val digest = MessageDigest.getInstance("SHA-256")
    var bytesRead: Int
    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
      digest.update(buffer, 0, bytesRead)
    }
    return digest.digest().joinToString(separator = "") { "%02x".format(it) }
  }

  fun importFromS3Path(datasetPath: String) {

    val etag = try {
      getEtag(datasetPath)
    } catch (e: Exception) {
      println("Unable to get etag for $datasetPath. Skipping import.")
      return
    }

    if (migrationRepo.existsByEtag(etag)) {
      println("Migration with etag $etag has already been applied. Skipping.")
      return
    }

    val fsPath = Paths.get("/tmp/$datasetPath")

    try {
      downloadFileToPath(
        sourcePath = datasetPath,
        destinationPath = fsPath,
      )
    } catch (e: Exception) {
      println("Unable to download file for $datasetPath. Skipping import.")
      return
    }

    val checksum = sha256Hash(fsPath.toFile().inputStream())
    if (migrationRepo.existsByChecksum(checksum)) {
      println("Migration with checksum $checksum has already been applied. Skipping.")
      return
    }

    val fileInformationBatch = mutableListOf<FileInformation>()
    val batchSize = 500

    var csvStream = fsPath.toFile().inputStream()

    csvReader().open(csvStream) {
      val rowsSequence = readAllWithHeaderAsSequence()
      rowsSequence.forEach { rowMap ->
        val fileInfo = FileInformation(
          googleFileId = rowMap["googleFileId"] ?: "",
          googleFileName = rowMap["googleFileName"] ?: "",
          googlePath = rowMap["googlePath"] ?: "",
          googleUrl = rowMap["googleUrl"] ?: "",
          googleOwnerEmail = rowMap["googleOwnerEmail"] ?: "",
          googleLastAccessedTime = parseGoogleLastAccessedTime(rowMap["googleLastAccessedTime"]),
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

    csvStream.close()
    fsPath.toFile().delete()

    migrationRepo.save(DataMigration(etag = etag, checksum = checksum))
    println("Migration applied with etag $etag and checksum $checksum")
  }
}
