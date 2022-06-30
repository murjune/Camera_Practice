package org.techtown.seminar3

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.techtown.seminar3.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var photo_over_Q_Uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        changeProfileImage()
        setContentView(binding.root)
    }

    private val galleyPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d(TAG, "MainActivity - galleyPermissionLauncher called")
            if (isGranted) {
                Log.d(TAG, "MainActivity - galleyPermissionLauncher - granted")
                galleryLauncher.launch("image/*")
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "MainActivity - cameraLauncher called")
            if (result.resultCode == RESULT_OK && result.data != null) {
                binding.ivAttached.setImageURI(photo_over_Q_Uri)
            } else if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "사진 선택을 취소하셨습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "MainActivity - cameraLauncher에서 알 수 없는 오류 발생")
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Log.d(TAG, "MainActivity - galleryLauncher called")
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

    /**
     * 카메라 호출할 Intent 호출
     */
    fun getPictureIntent_Shared_over_Q(context: Context): Intent {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$timeStamp.jpeg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        // URI형식: ex) contents://media/external/images/media/1008
        photo_over_Q_Uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ) ?: Uri.EMPTY

        val fullPhotointent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photo_over_Q_Uri)
        }
        return fullPhotointent
    }

    private fun changeProfileImage() {
        Log.d(TAG, "MainActivity - changeProfileImage() called")
        binding.btnGalleryImg.setOnClickListener {
            galleyPermissionLauncher.launch(STORAGE_READ_PERMISSION)
        }

        binding.btnCameraImg.setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        photo_over_Q_Uri = Uri.EMPTY
        val fullSizePictureIntent =
            getPictureIntent_Shared_over_Q(applicationContext).apply {
                resolveActivity(packageManager)
            }
        cameraLauncher.launch(fullSizePictureIntent)
    }

    companion object {
        private const val TAG = "로그"
        private const val STORAGE_WRITE_PERMISSION =
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        private const val STORAGE_READ_PERMISSION =
            android.Manifest.permission.READ_EXTERNAL_STORAGE

        private val PERMISSION_REQUESTED: Array<String> = arrayOf(
            STORAGE_READ_PERMISSION
        )
    }
}
