package com.example.step25imagecapture;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.step25imagecapture.util.MyHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MyHttpUtil.RequestListener{
    ImageView imageView;
    //저장된 이미지의 전체 경로
    String imagePath;
    //필요한 필드
    String sessionId, id;
    SharedPreferences pref;
    //최초 사진을 찍었는지 여부
    boolean isTakePicured=false;
    // 업로드할 File 객체의 참조값을 저장할 필드
    File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //사진을 출력할 ImageView 의 참조값 필드에 저장하기
        imageView=findViewById(R.id.imageView);
        Button takePicture=findViewById(R.id.takePicture);
        takePicture.setOnClickListener(v->{
            //사진을 찍고 싶다는 Intent 객체 작성하기
            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //운영체제에 해당 인턴트를 처리할수 있는 App 을 실행시켜 달라고 하고 결과 값도 받아올수 있도록 한다.
            startActivityForResult(intent, 0);
        });
        Button takePicutre2=findViewById(R.id.takePicture2);
        takePicutre2.setOnClickListener(v->{
            //사진을 찍고 싶다는 Intent 객체 작성하기
            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //외부 저장 장치의 절대 경로
            String absolutePath=getExternalFilesDir(null).getAbsolutePath();
            //파일명 구성
            String fileName= UUID.randomUUID().toString()+".jpg";
            //생성할 이미지의 전체 경로
            imagePath=absolutePath+"/"+fileName;
            //이미지 파일을 저장할 File 객체
            photoFile=new File(imagePath);
            //File 객체를 Uri 로 포장을 한다.
            //Uri uri= Uri.fromFile(photoFile);
            Uri uri=FileProvider.getUriForFile(this,
                    "com.example.step25imagecapture.fileprovider",
                    photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, 1);
        });
        EditText inputCaption=findViewById(R.id.inputCaption);
        //업로드 버튼에 대한 동작
        Button uploadBtn=findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(v->{
            //입력한 caption 과 찍은 사진 파일을 서버에 업로드 한다.
            String caption=inputCaption.getText().toString();
            //서버에 전송할 요철 파라미터를 Map 에 담고
            Map<String, String> map=new HashMap<>();
            map.put("caption", caption);
            //비동기 테스크를 이용해서 전송한다.
            //new UploadTask().execute(map);
            new MyHttpUtil(this).fileUploadRequest(2,
                    AppConstants.BASE_URL+"/api/gallery/insert",
                    map, this, photoFile);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        new MyHttpUtil(this).sendGetRequest(1, AppConstants.BASE_URL+"/music/logincheck",null, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //만일 위에서 요청한 요청과 같고 결과가 성공적이라면
        if(requestCode == 0 && resultCode == RESULT_OK){
            //data Intent 객체에 결과값(섬네일 이미지 데이터) 가 들어 있다.
            Bitmap image=(Bitmap)data.getExtras().get("data");
            //ImageView 에 출력하기
            imageView.setImageBitmap(image);
        }else if(requestCode == 1 && resultCode == RESULT_OK){
            //만일 여기가 실행된다면 imagePath 경로에 이미지 파일이 성공적으로 만들어진 것이다
            //Bitmap image=BitmapFactory.decodeFile(imagePath);
            //imageView.setImageBitmap(image);
            fitToImageView(imageView, imagePath);
        }
    }
    //이미지 뷰의 크기에 맞게 이미지를 출력하는 메소드
    public static void fitToImageView(ImageView imageView, String absolutePath){
        //출력할 이미지 뷰의 크기를 얻어온다.
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absolutePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(absolutePath, bmOptions);
        /* 사진이 세로로 촬영했을때 회전하지 않도록 */
        try {
            ExifInterface ei = new ExifInterface(absolutePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                // etc.
            }
        }catch(IOException ie){
            Log.e("####", ie.getMessage());
        }
        imageView.setImageBitmap(bitmap);
    }
    //Bitmap 이미지 회전시켜서 리턴하는 메소드
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    // MyHttpUtil을 이용해서 작업한 내용이 성공이면 호출되는 메소드
    @Override
    public void onSuccess(int requestId, String data) {
        switch (requestId){
            case 1: // 로그인 체크 요청인 경우
                try{
                    JSONObject obj = new JSONObject(data);
                    boolean isLogin = obj.getBoolean("isLogin");
                    //만일 로그인 하지 않았다면
                    if(!isLogin){
                        //로그인 액티비티로 이동
                        Intent intent=new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }else if(isLogin && !isTakePicured){//만일 로그인을 했고 아직 사진을 찍은 상태가 아니라면
                        //사진을 찍고 싶다는 Intent 객체 작성하기
                        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //외부 저장 장치의 절대 경로
                        String absolutePath=getExternalFilesDir(null).getAbsolutePath();
                        //파일명 구성
                        String fileName= UUID.randomUUID().toString()+".jpg";
                        //생성할 이미지의 전체 경로
                        imagePath=absolutePath+"/"+fileName;
                        //이미지 파일을 저장할 File 객체
                        photoFile=new File(imagePath);
                        //File 객체를 Uri 로 포장을 한다.
                        //Uri uri= Uri.fromFile(photoFile);
                        Uri uri=FileProvider.getUriForFile(MainActivity.this,
                                "com.example.step25imagecapture.fileprovider",
                                photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(intent, 1);
                        //사진을 이미 찍었다고 표시 한다.
                        isTakePicured=true;
                    }
                }catch (JSONException je){
                    Log.e("onSuccess()", je.getMessage());
                }
                break;
            case 2: // 사진 업로드 요청인 경우
                //s 는 {"isSuccess":true} or {"isSuccess":false} 형식의 문자열이다.
                try {
                    JSONObject obj = new JSONObject(data);
                    boolean isSuccess=obj.getBoolean("isSuccess");
                    if(isSuccess){
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("알림")
                                .setMessage("업로드 했습니다.")
                                .setNeutralButton("확인", (dialog, which) -> {
                                    //액티비티를 종료 시켜서 GalleryListActivity 가 다시 활성화 되도록한다.
                                    MainActivity.this.finish();
                                })
                                .create()
                                .show();
                    }else{
                        Toast.makeText(MainActivity.this, "실패!", Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException je){
                    Log.e("onSuccess()", je.getMessage());
                    Toast.makeText(MainActivity.this, "응답된 문자열이 json 문자열이 아닙니다.", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void onFail(int requestId, Map<String, Object> result) {

    }
}