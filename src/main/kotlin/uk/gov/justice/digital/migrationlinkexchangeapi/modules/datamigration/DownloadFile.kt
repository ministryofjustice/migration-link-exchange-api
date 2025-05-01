/**
 * This file was copied from the original file at
 * https://github.com/isamadrid90/aws-kotlin-examples/tree/main/download-s3-file
 */

package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

class DownloadFile(private val downloader: FileDownloader) {
    suspend operator fun invoke(path: String): Result<String> =
        downloader(path).fold(
            onFailure = {
                Result.failure(it)
            },
            onSuccess = {
                it?.let {
                    Result.success(it)
                } ?: Result.failure(FileNotExists(path))
            },
        )
}

data class FileNotExists(val path: String) : Throwable("The file at $path doesn't exists")
