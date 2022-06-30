- registerForActivityResult의 첫번째 인자 : ActivityResultContract<I, O>
인텐트 변환 객체, I: Launcher를 실행할 때 Input타입, O: callback으로부터 떨어지는 인수의 타입  
```kotlin
class CustomContract: ActivityResultContract<Intent, Long>(){
    
    // 인텐트 객체 생성
    override fun createIntent(context: Context, input: Intent): Intent {
        return input
    }
    
    // intent로부터 전달 받은 data를 뽑아 리턴
    override fun parseResult(resultCode: Int, intent: Intent?): Long{
        return intent?.getLongExtra("나는 key다", -1) ?: -1
    }
    
}
```
- registerForActivityResult의 두번째 인자 : ActivityResultCallback<O>
원래 Callback을 람다식으로 구성해서 intent와 resultCode 등을 접근해서 원래 OnActivityResult에서 데이터를 가져오듯 로직 구현
```kotlin
public interface ActivityResultCallback<O> {

    /**
     * Called when result is available

    void onActivityResult(@SuppressLint("UnknownNullness") O result);
}
```
