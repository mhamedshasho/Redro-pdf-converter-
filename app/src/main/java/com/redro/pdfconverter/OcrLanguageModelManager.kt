package com.redro.pdfconverter

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object OcrLanguageModelManager {
    private val languages = listOf("eng", "ara")

    fun isReady(context: Context): Boolean {
        val directory = File(File(context.filesDir, "tesseract"), "tessdata")
        return languages.all { File(directory, "$it.traineddata").length() > 1_000_000L }
    }

    fun downloadAll(context: Context, onProgress: (String) -> Unit) {
        val directory = File(File(context.filesDir, "tesseract"), "tessdata")
        check(directory.exists() || directory.mkdirs())
        languages.forEach { language ->
            val target = File(directory, "$language.traineddata")
            if (target.length() > 1_000_000L) {
                onProgress("$language already installed")
            } else {
                val temporary = File(directory, "$language.traineddata.part")
                val connection = URL("https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/main/$language.traineddata")
                    .openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 20000
                    connection.readTimeout = 60000
                    connection.instanceFollowRedirects = true
                    connection.connect()
                    check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                        "Unable to download $language OCR model"
                    }
                    connection.inputStream.use { input ->
                        FileOutputStream(temporary).use { output ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                output.write(buffer, 0, count)
                            }
                        }
                    }
                    check(temporary.length() > 1_000_000L) { "Invalid $language OCR model" }
                    if (target.exists()) target.delete()
                    check(temporary.renameTo(target)) { "Unable to install $language OCR model" }
                    onProgress("$language installed")
                } finally {
                    connection.disconnect()
                    temporary.delete()
                }
            }
        }
    }

    fun dataPath(context: Context): String =
        File(context.filesDir, "tesseract").absolutePath
}
