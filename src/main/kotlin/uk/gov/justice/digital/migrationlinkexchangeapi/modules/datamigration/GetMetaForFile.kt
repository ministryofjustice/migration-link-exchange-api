/**
 * This file was copied from the original file at
 * https://github.com/isamadrid90/aws-kotlin-examples/tree/main/download-s3-file
 */

package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

class GetMetaForFile(private val getMeta: FileGetMeta) {
    suspend operator fun invoke(path: String): Result<String> =
        getMeta(path).fold(
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
