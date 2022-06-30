package org.techtown.seminar3

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
import androidx.core.content.FileProvider
import org.techtown.seminar3.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var photoURI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        changeProfileImage()
        setContentView(binding.root)
    }
    /**
     * 카메라 권한 체크하는 launcher
     */
    private val cameraPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            var flag = true
            // 권한 체크
            it.forEach {
                if (!it.value) {
                    flag = false
                }
            }
            // 권한이 허용됐을 경우
            if (flag) {
                // 사진을 캡처하는 인텐트를 호출하는 코드
                photoURI = Uri.EMPTY
                val fullSizePictureIntent = getPictureIntent(applicationContext).apply {
                    // applicationContext 대신 this도 OK
                    resolveActivity(packageManager)
                }
                cameraLauncher.launch(fullSizePictureIntent)
            }
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
                binding.ivAttached.setImageURI(photoURI)
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
     * 2) 카메라 호출할 Intent 호출
     */
    fun getPictureIntent(context: Context): Intent {
        val fullSizeCaptureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 1) File 생성 - 촬영 사진이 저장 될
        // photoFile 경로 = /storage/emulated/0/Android/data/패키지명/files/Pictures/
        val photoFile: File? = try {
            createImageFile(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        } catch (ex: IOException) {
            // Error occurred while creating the File
            ex.printStackTrace()
            null
        }
        // 2) 생성된 File로 부터 Uri 생성 (by FileProvider)
        // URI 형식 EX) content://com.example.img.fileprovider/camera_images/20220630_0811_124213.jpg
        photoFile?.let {
            photoURI = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                it
            ) // BuildConfig.APPLICATION_ID: "org.techtown.seminar3"
            // "org.techtown.seminar3.fileprovider"가 인증, 파일로부터 uri가져오기

            // 3) 생성된 Uri를 Intent에 Put
            fullSizeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }
        return fullSizeCaptureIntent
    }

    /**
     * 1) 이미지 넣을 앱 내 파일 만들기
     */
    @Throws(IOException::class)
    private fun createImageFile(storageDir: File?): File {
        Log.d(TAG, "MainActivity - createImageFile() called")
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // Pictures 디렉토리에 2022_****.jpeg형태로 빈파일 저장
        return File.createTempFile(
            "$timeStamp _", /* prefix */
            ".jpeg", /* suffix */
            storageDir /* directory */
        ).apply {
            Log.d(TAG, "Created File AbsolutePath : $absolutePath")
        }
    }

    private fun changeProfileImage() {
        Log.d(TAG, "MainActivity - changeProfileImage() called")
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
