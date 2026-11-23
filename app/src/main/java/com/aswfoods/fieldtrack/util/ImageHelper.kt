package com.aswfoods.fieldtrack.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.aswfoods.fieldtrack.data.remote.RetrofitClient
import com.aswfoods.fieldtrack.ui.theme.AswCharcoal
import com.aswfoods.fieldtrack.ui.theme.AswSurfaceLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Utilitas pengelolaan berkas foto kamera, kompresi gambar JPEG, cache foto offline SPV,
 * dan komponen tampilan gambar ringan untuk Jetpack Compose.
 */
object ImageHelper {

    /**
     * Membuat berkas foto JPEG baru di penyimpanan internal persisten aplikasi (`filesDir/survey_photos`).
     *
     * @param context Context aplikasi
     * @param prefix Awalan nama file (misal: "rak" atau "sku")
     * @return File tujuan penulisan hasil kamera
     */
    fun createPhotoFile(context: Context, prefix: String): File {
        val dir = File(context.filesDir, "survey_photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val fileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        return File(dir, fileName)
    }

    /**
     * Menghasilkan URI aman via FileProvider agar aplikasi kamera sistem dapat menulis foto ke berkas internal.
     *
     * @param context Context aplikasi
     * @param file Berkas tujuan
     * @return Uri FileProvider
     */
    fun getUriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /**
     * Memperkecil resolusi dan mengompres gambar JPEG di tempat agar efisien penyimpanan dan kuota upload.
     *
     * @param filePath Path absolut berkas foto lokal
     * @param maxDimension Ukuran sisi terpanjang maksimum dalam piksel
     * @param quality Kualitas kompresi JPEG (0-100)
     * @return Path berkas yang telah dikompres
     */
    suspend fun compressJpegInPlace(
        filePath: String,
        maxDimension: Int = 1280,
        quality: Int = 80
    ): String = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) return@withContext filePath

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(filePath, boundsOptions)
        val srcWidth = boundsOptions.outWidth
        val srcHeight = boundsOptions.outHeight
        if (srcWidth <= 0 || srcHeight <= 0) return@withContext filePath

        var sampleSize = 1
        while (srcWidth / sampleSize > maxDimension * 2 || srcHeight / sampleSize > maxDimension * 2) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decodedBitmap = BitmapFactory.decodeFile(filePath, decodeOptions) ?: return@withContext filePath
        val orientedBitmap = applyExifOrientation(filePath, decodedBitmap)
        val scaledBitmap = scaleDownIfNeeded(orientedBitmap, maxDimension)

        FileOutputStream(file).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        if (scaledBitmap != orientedBitmap) orientedBitmap.recycle()
        if (orientedBitmap != decodedBitmap) decodedBitmap.recycle()
        scaledBitmap.recycle()

        filePath
    }

    /**
     * Mengunduh foto SPV ke cache lokal permanen saat sinkronisasi master data
     * agar blind test L12 tetap menampilkan foto wajah SPV dalam kondisi offline.
     *
     * @param context Context aplikasi
     * @param imageUrl URL foto SPV dari server
     * @param supervisorId ID unik supervisor
     * @return Path file lokal jika unduhan berhasil, atau null jika gagal
     */
    suspend fun downloadSupervisorPhoto(
        context: Context,
        imageUrl: String,
        supervisorId: Long
    ): String? = withContext(Dispatchers.IO) {
        if (imageUrl.isBlank()) return@withContext null
        val resolvedUrl = RetrofitClient.rewriteLocalhostUrl(imageUrl)
        runCatching {
            val dir = File(context.filesDir, "spv_photos")
            if (!dir.exists()) dir.mkdirs()
            val targetFile = File(dir, "spv_$supervisorId.jpg")

            val connection = (URL(resolvedUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                instanceFollowRedirects = true
            }
            connection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            connection.disconnect()
            targetFile.absolutePath
        }.getOrNull()
    }

    private fun applyExifOrientation(filePath: String, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            ExifInterface(filePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (rotationDegrees == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(rotationDegrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxCurrent = maxOf(width, height)
        if (maxCurrent <= maxDimension) return bitmap

        val ratio = maxDimension.toFloat() / maxCurrent.toFloat()
        val newWidth = (width * ratio).toInt().coerceAtLeast(1)
        val newHeight = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}

/**
 * Komponen pratinjau gambar ringan yang mendukung berkas lokal maupun URL HTTP
 * dengan fallback inisial apabila gambar belum tersedia secara offline.
 *
 * @param pathOrUrl Path berkas lokal atau URL foto
 * @param fallbackLabel Teks inisial yang ditampilkan saat foto belum tersedia
 * @param contentDescription Deskripsi aksesibilitas gambar
 * @param modifier Modifier ukuran dan bentuk kontainer
 */
@Composable
fun AsyncPhotoPreview(
    pathOrUrl: String?,
    fallbackLabel: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = pathOrUrl) {
        value = withContext(Dispatchers.IO) {
            loadImageBitmap(pathOrUrl)
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(AswSurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AswCharcoal
            )
        }
    }
}

private val thumbnailMemoryCache = LruCache<String, ImageBitmap>(32)

private fun loadImageBitmap(pathOrUrl: String?): ImageBitmap? {
    if (pathOrUrl.isNullOrBlank()) return null

    thumbnailMemoryCache.get(pathOrUrl)?.let { cached ->
        return cached
    }

    // 1. Cek jika berupa berkas lokal yang tersimpan di perangkat (decode ukuran thumbnail hemat memori)
    val localFile = File(pathOrUrl)
    if (localFile.exists()) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(localFile.absolutePath, bounds)
        var sampleSize = 1
        val targetMaxPx = 256
        while (bounds.outWidth / sampleSize > targetMaxPx * 2 ||
            bounds.outHeight / sampleSize > targetMaxPx * 2
        ) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = BitmapFactory.decodeFile(localFile.absolutePath, options)?.asImageBitmap()
        if (decoded != null) {
            thumbnailMemoryCache.put(pathOrUrl, decoded)
        }
        return decoded
    }

    // 2. Jika berupa URL HTTP/HTTPS, tulis ulang host lokal jika perlu lalu muat dari jaringan
    val resolvedUrl = RetrofitClient.rewriteLocalhostUrl(pathOrUrl)
    if (resolvedUrl.startsWith("http://", ignoreCase = true) || resolvedUrl.startsWith("https://", ignoreCase = true)) {
        val decoded = runCatching {
            val conn = (URL(resolvedUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
            }
            conn.inputStream.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
        if (decoded != null) {
            thumbnailMemoryCache.put(pathOrUrl, decoded)
        }
        return decoded
    }

    return null
}

