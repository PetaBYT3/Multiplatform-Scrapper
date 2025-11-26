package org.scrapper.multiplatform

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.navigation.NavHostController
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.koin.dsl.module
import org.scrapper.multiplatform.dataclass.DptResult
import org.scrapper.multiplatform.dataclass.LasikResult
import org.scrapper.multiplatform.dataclass.SiipResult
import org.scrapper.multiplatform.extension.androidPlatformId
import org.scrapper.multiplatform.repository.FirebaseRepository
import org.scrapper.multiplatform.services.FirebaseServices
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@SuppressLint("StaticFieldLeak")
private lateinit var androidContext: Context

fun initAndroidContext(context: Context) {
    androidContext = context
}

actual val platformId: String = androidPlatformId

@SuppressLint("HardwareIds")
actual fun getHardwareId(): String {
    return Settings.Secure.getString(
        androidContext.contentResolver,
        Settings.Secure.ANDROID_ID
    )
}

actual val platformSettingsModule = module {
    single<ObservableSettings> { SharedPreferencesSettings(androidContext.getSharedPreferences("settings", Context.MODE_PRIVATE)) }
}

actual val platformFirebaseModule = module {
    single<FirebaseServices> { FirebaseRepository(get()) }
}

actual fun readXlsxSiip(fileBytes: ByteArray): Flow<String> {
    return flow {
        var inputSteam: InputStream? = null
        try {
            inputSteam = ByteArrayInputStream(fileBytes)

            val workBook = WorkbookFactory.create(inputSteam)
            val sheet = workBook.getSheetAt(0)
            val formatter = DataFormatter()

            for (row in sheet) {
                val cell = row.getCell(0)
                val value = formatter.formatCellValue(cell)
                emit(value)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputSteam?.close()
        }
    }.flowOn(Dispatchers.IO)
}

actual suspend fun createXlsxSiip(siipResult: List<SiipResult>): ByteArray? {
    return withContext(Dispatchers.IO) {
        val workBook = XSSFWorkbook()
        val sheet = workBook.createSheet("SIIP Result")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("KPJ Number")
        headerRow.createCell(1).setCellValue("NIK Number")
        headerRow.createCell(2).setCellValue("Full Name")
        headerRow.createCell(3).setCellValue("Birth Date")
        headerRow.createCell(4).setCellValue("E-Mail")

        siipResult.forEachIndexed { index, result ->
            val dataRow = sheet.createRow(index + 1)
            dataRow.createCell(0).setCellValue(result.kpjNumber)
            dataRow.createCell(1).setCellValue(result.nikNumber)
            dataRow.createCell(2).setCellValue(result.fullName)
            dataRow.createCell(3).setCellValue(result.birthDate)
            dataRow.createCell(4).setCellValue(result.email)
        }

        try {

            val outputStream = ByteArrayOutputStream()
            outputStream.use { stream ->
                workBook.write(stream)
            }
            workBook.close()

            outputStream.toByteArray()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

actual fun readXlsxDpt(fileBytes: ByteArray): Flow<DptResult> {
    return flow {
        var inputStream: InputStream? = null
        try {
            inputStream = ByteArrayInputStream(fileBytes)

            val workBook = WorkbookFactory.create(inputStream)
            val sheet = workBook.getSheetAt(0)
            val formatter = DataFormatter()

            for (row in sheet) {
                if (row.rowNum == 0) {
                    continue
                }

                val kpjNumber = formatter.formatCellValue(
                    row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val nikNumber = formatter.formatCellValue(
                    row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val fullName = formatter.formatCellValue(
                    row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val birthDate = formatter.formatCellValue(
                    row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val email = formatter.formatCellValue(
                    row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                if (kpjNumber.isNotEmpty() && nikNumber.isNotEmpty()) {
                    emit(
                        DptResult(
                            kpjNumber = kpjNumber,
                            nikNumber = nikNumber,
                            fullName = fullName,
                            birthDate = birthDate,
                            email = email,
                            regencyName = "",
                            subdistrictName = "",
                            wardName = "",
                        )
                    )
                }
            }

            workBook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
    }
}

actual suspend fun createXlsxDpt(siipResult: List<DptResult>): ByteArray? {
    return withContext(Dispatchers.IO) {
        val workBook = XSSFWorkbook()
        val sheet = workBook.createSheet("SIIP Result")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("KPJ Number")
        headerRow.createCell(1).setCellValue("NIK Number")
        headerRow.createCell(2).setCellValue("Full Name")
        headerRow.createCell(3).setCellValue("Birth Date")
        headerRow.createCell(4).setCellValue("E-Mail")
        headerRow.createCell(5).setCellValue("Kabupaten Name")
        headerRow.createCell(6).setCellValue("Kecamatan Name")
        headerRow.createCell(7).setCellValue("Kelurahan Name")

        siipResult.forEachIndexed { index, result ->
            val dataRow = sheet.createRow(index + 1)
            dataRow.createCell(0).setCellValue(result.kpjNumber)
            dataRow.createCell(1).setCellValue(result.nikNumber)
            dataRow.createCell(2).setCellValue(result.fullName)
            dataRow.createCell(3).setCellValue(result.birthDate)
            dataRow.createCell(4).setCellValue(result.email)
            dataRow.createCell(5).setCellValue(result.regencyName)
            dataRow.createCell(6).setCellValue(result.subdistrictName)
            dataRow.createCell(7).setCellValue(result.wardName)
        }

        try {

            val outputStream = ByteArrayOutputStream()
            outputStream.use { stream ->
                workBook.write(stream)
            }
            workBook.close()

            outputStream.toByteArray()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

actual fun readXlsxLasik(fileBytes: ByteArray): Flow<LasikResult> {
    return flow {
        var inputStream: InputStream? = null
        try {
            inputStream = ByteArrayInputStream(fileBytes)

            val workBook = WorkbookFactory.create(inputStream)
            val sheet = workBook.getSheetAt(0)
            val formatter = DataFormatter()

            for (row in sheet) {
                if (row.rowNum == 0) {
                    continue
                }

                val kpjNumber = formatter.formatCellValue(
                    row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val nikNumber = formatter.formatCellValue(
                    row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val fullName = formatter.formatCellValue(
                    row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val birthDate = formatter.formatCellValue(
                    row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val email = formatter.formatCellValue(
                    row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val regencyName = formatter.formatCellValue(
                    row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val subdistrictName = formatter.formatCellValue(
                    row.getCell(6, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                val wardName = formatter.formatCellValue(
                    row.getCell(7, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                ).trim()

                if (kpjNumber.isNotEmpty() && nikNumber.isNotEmpty()) {
                    emit(
                        LasikResult(
                            kpjNumber = kpjNumber,
                            nikNumber = nikNumber,
                            fullName = fullName,
                            birthDate = birthDate,
                            email = email,
                            regencyName = regencyName,
                            subdistrictName = subdistrictName,
                            wardName = wardName,
                            lasikResult = "",
                        )
                    )
                }
            }

            workBook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
    }
}

actual suspend fun createXlsxLasik(siipResult: List<LasikResult>): ByteArray? {
    return withContext(Dispatchers.IO) {
        val workBook = XSSFWorkbook()
        val sheet = workBook.createSheet("SIIP Result")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("KPJ Number")
        headerRow.createCell(1).setCellValue("NIK Number")
        headerRow.createCell(2).setCellValue("Full Name")
        headerRow.createCell(3).setCellValue("Birth Date")
        headerRow.createCell(4).setCellValue("E-Mail")
        headerRow.createCell(5).setCellValue("Kabupaten Name")
        headerRow.createCell(6).setCellValue("Kecamatan Name")
        headerRow.createCell(7).setCellValue("Kelurahan Name")
        headerRow.createCell(8).setCellValue("Lasik Result")

        siipResult.forEachIndexed { index, result ->
            val dataRow = sheet.createRow(index + 1)
            dataRow.createCell(0).setCellValue(result.kpjNumber)
            dataRow.createCell(1).setCellValue(result.nikNumber)
            dataRow.createCell(2).setCellValue(result.fullName)
            dataRow.createCell(3).setCellValue(result.birthDate)
            dataRow.createCell(4).setCellValue(result.email)
            dataRow.createCell(5).setCellValue(result.regencyName)
            dataRow.createCell(6).setCellValue(result.subdistrictName)
            dataRow.createCell(7).setCellValue(result.wardName)
            dataRow.createCell(8).setCellValue(result.lasikResult)
        }

        try {

            val outputStream = ByteArrayOutputStream()
            outputStream.use { stream ->
                workBook.write(stream)
            }
            workBook.close()

            outputStream.toByteArray()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

actual fun saveFile(fileName: String, fileBytes: ByteArray, filePath: String) {

    val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val targetDir = File(baseDir, filePath)

    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }

    val file = File(targetDir, fileName)

    FileOutputStream(file).use { stream ->
        stream.write(fileBytes)
    }
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled) {
        onBack()
    }
}

@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current

    DisposableEffect(enabled) {
        val screenState = view.keepScreenOn
        if (enabled) {
            view.keepScreenOn = true
        }

        onDispose { view.keepScreenOn = screenState }
    }
}