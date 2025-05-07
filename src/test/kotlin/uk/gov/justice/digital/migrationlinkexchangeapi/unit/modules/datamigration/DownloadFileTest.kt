/**
 * This file was copied and adapted from the original file at
 * https://github.com/isamadrid90/aws-kotlin-examples/tree/main/download-s3-file
 */

package uk.gov.justice.digital.migrationlinkexchangeapi.modules.datamigration

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.file.Path
import java.nio.file.Paths


class DownloadFileTest {
    private lateinit var fileDownloader: FileDownloader
    private lateinit var downloadFile: DownloadFile

    @BeforeEach
    fun setUp() {
        fileDownloader = mockk()
        downloadFile = DownloadFile(fileDownloader)
    }

    @Test
    fun `should successfully access file content`() {
        runBlocking {
            val path = "path/to/s3/file.txt"
            val destinationPath = Paths.get("/tmp/tests/file.txt")
            `given there is a file at given path`()
            val result = `when is access with sourcePath and destinationPath`(path, destinationPath)
            `then the file downloader should have been called`(path, destinationPath)
            `then the result content is correct`(result)
        }
    }

    @Test
    fun `should fail when file does not exist`() {
        runBlocking {
            val sourcePath = "path/to/non/existing/s3/file.txt"
            val destinationPath = Paths.get("/tmp/tests/file.txt")
            `given there is no file at given path`(sourcePath, destinationPath)
            val result = `when is access with sourcePath and destinationPath`(sourcePath, destinationPath)
            `then the file downloader should have been called`(sourcePath, destinationPath)
            `then the result is failure`(result, sourcePath)
        }
    }

    private fun `given there is a file at given path`() {
        coEvery { fileDownloader.invoke(any(), any()) } returns Result.success(FILE_CONTENT)
    }

    private fun `given there is no file at given path`(sourcePath: String, destinationPath: Path) {
        coEvery { fileDownloader.invoke(any(), any()) } returns Result.failure(FileNotExists(sourcePath))
    }

    private suspend fun `when is access with sourcePath and destinationPath`(sourcePath: String, destinationPath: Path): Result<String> {
        return downloadFile(sourcePath, destinationPath)
    }

    private fun `then the result content is correct`(result: Result<String>) {
        assertEquals(Result.success(FILE_CONTENT), result)
    }

    private fun `then the result is failure`(
        result: Result<String>,
        path: String,
    )  {
        assertEquals(Result.failure<String>(FileNotExists(path)), result)
    }

    private fun `then the file downloader should have been called`(path: String, destinationPath: Path) {
        coVerify { fileDownloader(path, destinationPath) }
    }

    companion object {
        private const val FILE_CONTENT = "Download file with content from S3\n"
    }
}
