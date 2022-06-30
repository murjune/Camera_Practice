package org.techtown.seminar3

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.techtown.seminar3.databinding.ActivityMainBinding
import java.io.FileOutputStream
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val cameraPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            var flag = true
            it.forEach {
                if (!it.value) {
                    flag = false
                }
            }
            if (flag) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.resolveActivity(packageManager)
                cameraLauncher.launch(intent)
            }
        }
    private val galleyPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                galleryLauncher.launch("image/*")
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri: Uri?
            if (result.resultCode == RESULT_OK && result.data != null) {
                val bitmap = (result.data!!.extras?.get("data") as Bitmap?)
                bitmap.let { curBitmap ->
                    val fileName = newFileName()
                    uri = saveImageFile(fileName, "image/jpeg", curBitmap!!)
                    Log.d(TAG, "MainActivity -  cameraLauncher - uri: $uri")
                    binding.ivAttached.setImageURI(uri)
                    return@registerForActivityResult
                }
                Log.e(TAG, "MainActivity - cameraLauncher - bitmap: $bitmap")
            } else if (result.resultCode == RESULT_OK) {
                contentResolver.delete(uri!!, null, null)
                Toast.makeText(this, "사진 선택을 취소하셨습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "MainActivity - cameraLauncher에서 알 수 없는 오류 발생")
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null)
                if (Build.VERSION.SDK_INT < 28) {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    binding.ivAttached.setImageBitmap(bitmap)
                } else {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    binding.ivAttached.setImageBitmap(bitmap)
                }
            else {
                Toast.makeText(this, "사진 선택을 취소하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        changeProfileImage()
        setContentView(binding.root)
    }

    private fun saveImageFile(fileName: String, mimeType: String, bitmap: Bitmap): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri: Uri? =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        try {
            uri?.let {
                var descriptor = contentResolver.openFileDescriptor(uri, "w")
                descriptor?.let {
                    val fos = FileOutputStream(descriptor.fileDescriptor)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.close()
                    return uri
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveImageFile: ${e.localizedMessage}")
        }
        return null
    }

    fun newFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val fileName = sdf.format(System.currentTimeMillis()) // 파일 이름 중복을 막기위해 현재시간으로
        return fileName
    }

    private fun changeProfileImage() {

        binding.btnGalleryImg.setOnClickListener {
            galleyPermissionLauncher.launch(STORAGE_READ_PERMISSION)
        }

        binding.btnCameraImg.setOnClickListener {
            cameraPermissionLauncher.launch(PERMISSION_REQUESTED)
        }
    }

    companion object {
        private const val TAG = "로그"
        private const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
        private const val STORAGE_WRITE_PERMISSION =
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val STORAGE_READ_PERMISSION =
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE

        private val PERMISSION_REQUESTED: Array<String> = arrayOf(
            CAMERA_PERMISSION,
            STORAGE_WRITE_PERMISSION,
            STORAGE_READ_PERMISSION
        )
    }
}
