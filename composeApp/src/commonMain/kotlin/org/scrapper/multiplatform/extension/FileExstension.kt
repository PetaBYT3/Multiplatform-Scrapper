package org.scrapper.multiplatform.extension

import androidx.compose.runtime.Composable
import java.nio.file.Files
import java.nio.file.Paths

@Composable
fun saveFile(
    fileName: String,
    fileBytes: ByteArray
) {
    val userHome = System.getProperty("user.home")
    val documentsDir = Paths.get(userHome, "Documents")

    Files.createDirectories(documentsDir)

    val filePath = documentsDir.resolve(fileName)

    Files.write(filePath, fileBytes)
}