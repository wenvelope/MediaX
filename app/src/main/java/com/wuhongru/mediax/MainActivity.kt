package com.wuhongru.mediax

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.wuhongru.mylibrary.savePhotoAlbum
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Thread{
            val bitmap = getBitmap(this,R.drawable.ic_baseline_3d_rotation_24)
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"1.png")
            if(!file.exists()){
                Log.e("wuhongru","bucunzai")
                file.createNewFile()
            }
            if(bitmap==null){
                Log.e("wuhongru","bitmap==null")
            }
            val s =savePhotoAlbum(this,bitmap,file)
            Log.e("wuhongru",s.toString())
        }.start()
    }

    fun getBitmap(context: Context, vectorDrawableId: Int): Bitmap? {
        val drawable = VectorDrawableCompat.create(context.getResources(), vectorDrawableId, null)
            ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }
}