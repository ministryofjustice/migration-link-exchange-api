/**
 * This file was copied from the original file at
 * https://github.com/isamadrid90/aws-kotlin-examples/tree/main/download-s3-file
 */

package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import java.nio.file.Path

interface FileDownloader {
    suspend operator fun invoke(sourcePath: String, destinationPath: Path): Result<String?>
}
