package org.scrapper.multiplatform.extension

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//Platform
const val androidPlatformId = "android"
const val jvmPlatformId = "jvm"

const val webUserAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

//Links
const val SiipBPJSLoginUrl = "https://sipp.bpjsketenagakerjaan.go.id/"

const val SiipBPJSInput = "https://sipp.bpjsketenagakerjaan.go.id/tenaga-kerja/baru/form-tambah-tk-individu"

const val dptUrlInput = "https://cekdptonline.kpu.go.id/"

const val lasikInputUrl = "https://lapakasik.bpjsketenagakerjaan.go.id/?source=e419a6aed6c50fefd9182774c25450b333de8d5e29169de6018bd1abb1c8f89b"

//Save Directory
expect val siipPath: String
expect val lasikPath: String
expect val dptPath: String

fun removeDoubleQuote(string: String) : String {
    val removedQuote = string.replace("\"", "")
    return removedQuote
}

fun getFullName(string: String) : String {
    val fullName = string.replace("Nama Pemilih\\n", "")
    return fullName
}

fun getRegencyName(string: String) : String {
    val regencyName = string.replace("Kabupaten", "")
    return regencyName
}

fun getSubdistrictName(string: String) : String {
    val subdistrictName = string.replace("Kecamatan", "")
    return subdistrictName
}

fun getWardName(string: String) : String {
    val wardName = string.replace("Kelurahan", "")
    return wardName
}

fun quoteSafeString(string: String) : String {
    return string.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r")
}

fun getCurrentTime() : String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")

    return currentDateTime.format(formatter)
}