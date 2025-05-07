/**
 * This file was copied from the original file at
 * https://github.com/isamadrid90/aws-kotlin-examples/tree/main/download-s3-file
 */

package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import java.nio.file.Path

class DownloadFile(private val downloader: FileDownloader) {
    suspend operator fun invoke(sourcePath: String, destinationPath: Path): Result<String> =
        downloader(sourcePath, destinationPath).fold(
            onFailure = {
                Result.failure(it)
            },
            onSuccess = {
                it?.let {
                    Result.success(it)
                } ?: Result.failure(FileNotExists(sourcePath))
            },
        )
}

data class FileNotExists(val path: String) : Throwable("The file at $path doesn't exists")
