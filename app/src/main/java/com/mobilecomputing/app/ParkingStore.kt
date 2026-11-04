package com.mobilecomputing.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

data class ParkingSpot(
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val note: String,
    val hasPhoto: Boolean
)

// Park yerini SharedPreferences'ta, fotoğrafı uygulamanın kendi klasöründe saklar.
class ParkingStore(context: Context) {
    private val prefs = context.getSharedPreferences("parking", Context.MODE_PRIVATE)
    private val photoFile = File(context.filesDir, "car.jpg")

    fun load(): ParkingSpot? {
        if (!prefs.contains("lat")) return null
        return ParkingSpot(
            latitude = prefs.getString("lat", "0")!!.toDouble(),
            longitude = prefs.getString("lng", "0")!!.toDouble(),
            time = prefs.getLong("time", 0L),
            note = prefs.getString("note", "")!!,
            hasPhoto = photoFile.exists()
        )
    }

    fun save(latitude: Double, longitude: Double, note: String, photo: Bitmap?): ParkingSpot {
        if (photo != null) {
            photoFile.outputStream().use { photo.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        } else {
            photoFile.delete()
        }
        prefs.edit()
            .putString("lat", latitude.toString())
            .putString("lng", longitude.toString())
            .putLong("time", System.currentTimeMillis())
            .putString("note", note)
            .apply()
        return load()!!
    }

    fun loadPhoto(): Bitmap? =
        if (photoFile.exists()) BitmapFactory.decodeFile(photoFile.path) else null

    fun clear() {
        prefs.edit().clear().apply()
        photoFile.delete()
    }
}
