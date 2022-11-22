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
 * @param context 上下文
 * @param file 用来保存到外部共享目录的Picture文件夹下的已经存在的媒体文件
 *
 * 安卓10以上通过uri插入
 * 安卓10一下直接通知扫盘
 *
 */
fun savePhotoIntoPicturesFromInnerStorage(context: Context,file: File):Boolean{
    context.apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            values.put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(file))
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val contentResolver: ContentResolver = contentResolver
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return false
            try {
                val outputStream = contentResolver.openOutputStream(uri)
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
    return true
}


/**
 * 保存到相册
 *
 * @param src  源图片
 * @param file 要保存到的文件
 */
fun savePhotoAlbum(context:Context,src:Bitmap?, file:File):Boolean{
    if(src == null){
        return false
    }
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
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val contentResolver: ContentResolver = contentResolver
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return false
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
    return true
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

