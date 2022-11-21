package com.wuhongru.mylibrary

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.*
import java.util.*

/**
 * 保存到相册
 *
 * @param src  源图片
 * @param file 要保存到的文件
 */
fun savePhotoAlbum(context:Context,src:Bitmap, file:File) {
    context?.apply {
        //先保存到文件
        var outputStream:OutputStream?
        try {
            outputStream = BufferedOutputStream(FileOutputStream(file));
            src.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            if (!src.isRecycled) {
                src.recycle();
            }
        } catch (e:FileNotFoundException) {
            e.printStackTrace();
        }
        //再更新图库
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            values.put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file))
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
            val contentResolver: ContentResolver = contentResolver
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return
            try {
                outputStream = contentResolver.openOutputStream(uri)
                val fileInputStream = FileInputStream(file)
                FileUtils.copy(fileInputStream, outputStream!!)
                fileInputStream.close()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/jpeg")
            ) { _: String?, _: Uri? -> }
        }
    }
}


 internal fun getSuffix(file: File?): String? {
    if (file == null || !file.exists() || file.isDirectory) {
        return null
    }
    val fileName = file.name
    if (fileName == "" || fileName.endsWith(".")) {
        return null
    }
    val index = fileName.lastIndexOf(".")
    return if (index != -1) {
        fileName.substring(index + 1).lowercase(Locale.US)
    } else {
        null
    }
}

 internal fun getMimeType(file: File?): String? {
    val suffix = getSuffix(file) ?: return "file/*"
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix) ?: "file/*"
}

