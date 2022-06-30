# Camera-Gallery 연습을 위한 레포입니다.  

공식문서의 예제를 연습하는 저장소입니다.
---
[참고 문서: Camera ApI 공식문서](https://developer.android.com/guide/topics/media/camera?hl=ko)  

[참고문서2: 사진촬영 공식문서](https://developer.android.com/training/camera/photobasics?hl=ko)  

[참고 블로그](https://bbang-work-it-out.tistory.com/23?category=1053431)  

--- 
# 1. 이론 정리
## 매니페스트 설정
- 카메라 권한 - 애플리케이션이 기기 카메라를 사용할 권한을 요청해야 합니다  
>  기존 카메라 앱을 호출하여 카메라를 사용하고 있는 경우, 애플리케이션이 이 권한을 요청하지 않아도 됩니다.
- 카메라 기능 - 애플리케이션은 다음과 같은 카메라 기능 사용도 선언해야 합니다.  
- 저장 권한 - 애플리케이션이 이미지나 동영상을 기기의 외부 저장소(SD 카드)에 저장할 경우, 이 역시 매니페스트에 지정해야 합니다.  
```xml
<uses-permission android:name="android.permission.CAMERA" />  <-- 카메라 권한
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> <- 저장소 쓰기권한
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /> <- 저장소 읽기권한
<uses-permission android:name="android.hardware.camera" android:required= "true"/> <- 카메라 기능 사용하겠다 명시
```
만약, 카메라 기능을 사용하지 않을 경우, 이 내용을 매니페스트에 명시해야한다!!  
> 카메라 기능이 없는 기기에서 아래 설정을 안할 경우 앱이 죽을 것이다.. 
```xml
<uses-feature android:name="android.hardware.camera" android:required="false" />
```
## 1. 카메라 앱으로 사진 촬영
```kotlin
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
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.resolveActivity(packageManager)
                cameraLauncher.launch(intent)
            }
        }
```
## 2. 미리보기 이미지 가져오기(썸네일)
- Intent의 "data" 키값으로 아래 `extras`에 작은 Bitmap으로 사진을 인코딩한다.
- 다음 코드는 이미지를 가져와서 ImageView에 표시하는 방법을 보여준다.  

즉, intent의 extra에 썸네일 크기의 사진 데이터를 담는 것이다.
> [참고] : "data"에서 가져온 미리보기 이미지는 아이콘으로 사용하기에는 좋지만, 원본 크기의 이미지를 처리하려면 추가 작업이 필요하다  
> 썸네일 용도다.  

```kotlin
private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri: Uri?
            // result.data : intent
            if (result.resultCode == RESULT_OK && result.data != null) {
                val bitmap = (result.data!!.extras?.get("data") as Bitmap?)
                }
        }
```
## 3. 원본 이미지 가져오기(Full- Image)
Android 카메라 애플리케이션은 저장할 파일을 받으면 원본 크기의 사진을 저장합니다.  
카메라 앱이 사진을 저장할 정규화된 파일 이름을 제공해야 합니다.
일반적으로 사용자가 기기 카메라로 캡처한 사진은 기기의 공용 외부 저장소에 저장되므로 모든 앱에서 액세스할 수 있습니다
- 'getExternalStoragePublicDirectory()' : 
사진을 공유하기 위한 적절한 디렉터리는 `DIRECTORY_PICTURES`를 인수로 사용하여 `getExternalStoragePublicDirectory()`에서 제공  
이 메서드에서 제공하는 디렉터리는 `모든 앱에서 공유`하기 때문에 이 디렉터리를 읽고 쓰려면 아래의 권한이 필요하다.  
> 쓰기 권한은 암시적으로 읽기를 허용
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
- 'getExternalFilesDir()' :
- 사진을 앱 이외에는 비공개로 두려면 대신 getExternalFilesDir()에서 제공하는 디렉터리를 사용  
Android 4.4부터는 이 디렉터리를 다른 앱에서 액세스할 수 없으므로 `쓰기권한`이 필요 없으며 다음과 같이 `maxSdkVersion 속성`을 추가하여  
Android 이전 버전에서만 권한이 요청되도록 선언할 수 있다.
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
android:maxSdkVersion="18" />
```
- `getExternalFilesDir()`사용시 사용자가 앱을 제거할 때 삭제됩니다
- `getExternalStoragePublicDirectory()`사용시에는 사용자가 앱을 삭제해도 Dir들이 남아 있다.
### 1) 과정
- 1.  촬영 이미지을 저장
2. 촬영 이미지가 저장될 빈 File을 미리 만들어둔다.  
3. 그 File의 URI를 Intent에 실어 카메라를 호출
4. 촬영을 하게 되면 해당 File에 데이터가 써지고, URI를 통해 이미지를 읽어와 사용   

Android10(Q)에서부터 `Scoped Storage`가 적용되면서 공용 공간에 접근할 수 있는 API 들이 deprecated 됐고,
대신 `MediaStore API` 를 사용해야한다!!  
> 더이상 `File 절대경로`를 통한 공용 공간 접근은 불가하고, `MediaStore`를 통해서 접근 해야 한다.  
- 그래서 MediaStore란??  
```
정의: Media Provider 와 application 간의 contract  
안드로이드 시스템에서 제공하는 기능이며 Media Data들을 Indexing 해서 미디어 DB로 관리합니다

# MediaStore에 저장할 때
MediaStroe를 이용해 데이터 저장을 할 시 저장 권한은 필요하지 않습니다.
단, 'Downloads' 폴더를 제외하곤, (MIME타입이 아니라)파일 타입에 따라 저장 할 수 있는 폴더가 정해져 있다.
  
# MediaStore을 읽을 때
저장이 아닌 읽을 때는 READ_EXTERNAL_STORAGE 권한이 필요합니다
```
어느 저장소를 사용할 것인지 결정하는 것이 좋다.  
- `공용 공간` 선택 시 : Scoped Storage를 고려해야하며 Q 미만에서 'WRITE_EXTERNAL_STORAGE' 권한 필요
- `앱 전용 공간` 선택 시 : Android 4.3(Api level 18) 까지는 'WRITE_EXTERNAL_STORAGE' 권한이 필요하며 4.4 부터는 권한이 필요 없음

이제 대충 이론에 대해 정리했으니 실습과정을 살펴봅시다!!(정리 너무 어렵다..)  
이제부터가 진짜 시작입니다..  

# 2. 앱 전용 공간에서 카메라 촬영
위에서 한 번 정리했던, 앱 전용 공간을 위해서 `getExternalFilesDir()`가 제공하는 외부저장소를 사용할 것이다.  
1. `context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)` : 외부 저장소
2. path = `/storage/emulated/0/Android/data/패키지명/files/Pictures/` <- 절대경로를 사용했던 기존의 방식과 다르다.
3. 파일의 URI는 `FileProvider`를 통해 생성

## 1) 세팅 
### 1. 퍼미션 선언, FileProviedr 선언
```xml
<manifest>
    ...
    <!-- 앱 전용 공간에 저장이 필요 할 경우 'getExternalFilesDir()'-->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="18"/> <-- 4.3 버전 까지는 퍼미션 필요
    ...
    <application>
        <provider
            android:authorities="org.techtown.seminar3.fileprovider" <- 패키지명 쓰기
            android:name="androidx.core.content.FileProvider" <- 요놈을 default로 써주기
            android:exported="false" <- public일 이유가 없지 아무래도?
            android:grantUriPermissions="true"> <- temp권한을 얻어야하기 때무네 true
            <meta-data android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths"/>
    </application>
    ...
</manifest>
```  
FileProvider 의 기본 기능을 확장하고 싶다면, FileProvider 를 상속하고 `android:name` 에 fully qualified name 을 적어주면 된다.  
다음에, FileProvider에 대해 공부할 때, 정리하자!!

### 2. FileProvider를 위한 filepaths.xml 만들기
- **FileProvider 는 미리 설정한 폴더에 있는 file 들에 대해서만 URI 를 생성할 수 있다.**
- 폴더 설정은 `storage area` 와 `path` 를 xml 에 적어주는 것으로 가능하다. 이는 <paths> element 를 통해 한다.
```xml
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path
        name="camera_images"
        path="Pictures/"/>
</paths>
``` 
- `name attribute` 는 Uri path segment 로 실제 path 를 감추는 효과를 지닌다.
- `path attribute` 는 실제 path 를 명시해준다. 이 녀석은 `directory` 이지 특정 파일이 아니다.
- 위 xml 은 private file 영역(`Context.getFilesDir()`)의 `*/images/` 라는 디렉토리에 한정이 된다.
- paths 태그 안의 files-path 는 복수개가 올 수 있다.  

- 좀 더 알아보기
```xml
<paths>

    <!--<file-path> =  내부, 앱 스토리지 / getFileDir -->
    <!--<cache-path> =  내부, 앱 캐시 스토리지 / getCacheDir -->
    <!--<external-files-path> = 외부, 앱 전용 스토리지 file / getExternalFilesDir 사용시-->
    <!--<external-cache-path> = 외부, 앱 전용 스토리지 cache / getExternalCacheDir 사용시-->
    <!--<external-path> = 외부, 공용 저장소 사용 시 / Environment.getExternalStorageDirectory-->

    // 사진은 'getExternalFilesDir' 아래 'Pictures' 폴더 아래 저장되어 있을 예정
    <external-files-path name="cameraImg" path="Pictures/"/>

</paths> 
```
### 3. FileProvider 정리
어떤 앱이 image를 사용하는 것에 대해 명시를 안해주면 카메라앱이 제대로 동작이 안되게끔 막아놨음.(보안 상의 이유 때문)
그래서 파일 프로바이더로 그 제한을 일부 풀어주는 것이다.   
따라서, 인가받을 `file-path`를 xml단에 정의하고, file path 를 FileProvider 의 meta-data 에 넣어주어 명시해주는 것이다.  

- FileProvider 는 ContentProvider 의 subclass 로 `secure 한 file share` 를 관장  (보안을 위해 나온 녀석)
- FileProvider 의 기본 기능은 `file 에 대해 content URI` 를 생성
- 이를 통하면 file:/// 형태의 uri 대신 `content:// 형태의 uri` 를 사용
- content URI 는 read, write access 를 `임시 permission` 으로 부여할 수 있다.


## 2) 카메라 호출 과정
1. 앱 내부 저장소에 빈 파일을 생성
2. FileProvider를 이용해 빈 파일의 Uri를 얻음
3. 얻은 photoURI를 Intent의 'MediaStore.EXTRA_OUTPUT' 속성 value값으로 put
4. Intent를 이용해 카메라 호출

### 1. 앱 내부 저장소에 빈 파일을 생성
```kotlin
/**
 * 1) 이미지 넣을 앱 내 파일 만들기
 */
@Throws(IOException::class)
    private fun createImageFile(storageDir: File?): File {
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
```
### 2. 카메라 호출할 Intent 호출
- 1) File 생성 - 촬영 사진이 저장 될
- 2) 생성된 File로 부터 Uri 생성 (by FileProvider)
- 3) 생성된 Uri를 Intent에 Put
4) 생성한 Intent 반환
```kotlin
/**
     * 2) 카메라 호출할 Intent 호출
     */
    fun getPictureIntent(context: Context): Intent {
        val fullSizeCaptureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 1) File 생성 - 촬영 사진이 저장 될
        // photoFile 경로 = /storage/emulated/0/Android/data/패키지명/files/Pictures/
        val photoFile: File? = try {
            // 1번에서 만든 createImageFile(context: Context) 사용해서 저장소에서 빈 파일 만들어서 할당하기 
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
```
### 3. Intent를 이용해 카메라 호출
- 권한 체크 후, isGranted == true이면 카메라 호출  
```kotlin
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
        // 사진을 캡처하는 인텐트를 호출하는 코드
        if (flag) {
            photoURI = Uri.EMPTY
            val fullSizePictureIntent = getPictureIntent(applicationContext).apply {
                // applicationContext 대신 this도 OK
                resolveActivity(packageManager)
            }
            cameraLauncher.launch(fullSizePictureIntent)
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
```
### 정리
- 앱 내부 저장소에 파일 생성  
- 내부 저장소 파일 FileProvider로 uri 가져오기  
- 생성된 uri Intent에 담고, Camera 실행  
- Camera로 캡쳐한 사진 파일에 담고, uri값으로 사진 imageView에 넣기  

# 3. 공용 공간에서 카메라 촬영  
- 공용 공간을 위해서 `getExternalStoragePublicDirectory()`가 제공하는 외부저장소에 이미지 파일 저장  
- path = `"/storage/emulated/0/Pictures/"` == `"sdcard/Pictures/"`  
- Q = Android 10 = ApI 29
- Q이상 : 파일의 URI는 MediaStore API를 이용해 획득  
- Q미만일 경우: 파일의 URI는 FileProvider를 통해 획득  
- Q미만의 경우: WRITE_EXTERNAL_STORAGE 권한 필요

## 1) 세팅 - 이건 API28일 때만 해당하는 부분
- 내가 구현한 예제는 FileProvider를 사용안하고, MediaStore API를 사용해서 파일 Uri를 얻었으므로 요 부분은 생략해도 된다~

- 앱 전용 섹션에서 다뤘던 내용과 거의 동일
- filepaths.xml
```xml
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path
        name="cameraImgShared"
        path="Pictures/"/>
</paths>
    <!-- 사진은 'Environment.getExternalStorageDirectory' 아래 'Pictures' 폴더 아래 저장-->
```
- manifest
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
android:maxSdkVersion="28"/> <- 버전 9.0까지는 퍼미션 필요~ 

<provider
    android:authorities="org.techtown.seminar3.fileprovider"
    android:name="androidx.core.content.FileProvider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths"/>
</provider>
```

## 1) 카메라호출
- Q미만: 위의 앱 전용 공간 저장 예제와 동일(파일 위치만 다르다) - 요 부분은 거의 똑같기 때문에 패스~  
- Q이상: ContentResolver를 이용해 URI 획득 () - 요 부분을 예제로 다룸~
- 얻은 URI를 intent의 `Media.EXTRA_OUTPUT`속성 value값으로 put  
- Intent로 카메라 호출  
```kotlin
/**
 * Q이상 일 때, Camera 캡쳐 시 공용 저장소에 저장 후, Full 사진 보여주는 것 까지 하는 예제
 * - ContentResolver를 사용해서 간단하게 Uri획득
 */
private lateinit var photo_over_Q_Uri: Uri

private val cameraLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            binding.ivAttached.setImageURI(photo_over_Q_Uri)
        } else if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "사진 선택을 취소하셨습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "MainActivity - cameraLauncher에서 알 수 없는 오류 발생")
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
    // context.contentResolver로 contentValues -> uri로 변환
    photo_over_Q_Uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
    ) ?: Uri.EMPTY

    val fullPhotointent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
        putExtra(MediaStore.EXTRA_OUTPUT, photo_over_Q_Uri)
    }
    return fullPhotointent
}

private fun changeProfileImage() {
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
```
- 파일 프로바이더가 필요없다.
- 퍼미션 체크가 의무가 아니므로, 체크하는 부분이 없어졌다 :D
# 뒤늦게 안 사실
- Manifest에 카메라 권한을 딱히 추가 안해줘도 무방하다~~
- 앱 내 저장소도 Media Api 를 사용해서 구현할 수 있을 것 같음~ 이거 좀 고민해보자