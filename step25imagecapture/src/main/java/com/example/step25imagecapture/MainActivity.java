package com.example.step25imagecapture;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    //저장된 이미지의 전체 경로
    String imagePath;
    //필요한 필드
    String sessionId, id;
    SharedPreferences pref;

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
            File photoFile=new File(imagePath);
            //File 객체를 Uri 로 포장을 한다.
            //Uri uri= Uri.fromFile(photoFile);
            Uri uri=FileProvider.getUriForFile(this,
                    "com.example.step25imagecapture.fileprovider",
                    photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, 1);
        });

        EditText inputCaption = findViewById(R.id.inputCaption);

        //업로드 버튼에 대한 동작
        Button uploadBtn=findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(v->{
            //입력한 caption 과 찍은 사진 파일을 서버에 업로드 한다.
            String caption = inputCaption.getText().toString();
            // 서버에 전송할 요청 파라미터를 Map 에 담고
            Map<String, String> map = new HashMap<>();
            map.put("caption", caption);
            // 비동기 테스크 이용해서 전송한다.
            new UploadTask().execute(map);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        pref= PreferenceManager.getDefaultSharedPreferences(this);
        sessionId=pref.getString("sessionId", "");
        //로그인 했는지 체크하기
        new LoginCheckTask().execute(AppConstants.BASE_URL+"/music/logincheck");
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
    //로그인 여부를 체크하는 작업을 할 비동기 task
    class LoginCheckTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            //로그인 체크 url
            String requestUrl=strings[0];
            //서버가 http 요청에 대해서 응답하는 문자열을 누적할 객체
            StringBuilder builder=new StringBuilder();
            HttpURLConnection conn=null;
            InputStreamReader isr=null;
            BufferedReader br=null;
            boolean isLogin=false;
            try{
                //URL 객체 생성
                URL url=new URL(requestUrl);
                //HttpURLConnection 객체의 참조값 얻어오기
                conn=(HttpURLConnection)url.openConnection();
                if(conn!=null){//연결이 되었다면
                    conn.setConnectTimeout(20000); //응답을 기다리는 최대 대기 시간
                    conn.setRequestMethod("GET");//Default 설정
                    conn.setUseCaches(false);//케쉬 사용 여부
                    //App 에 저장된 session id 가 있다면 요청할때 쿠키로 같이 보내기
                    if(!sessionId.equals("")) {
                        // JSESSIONID=xxx 형식의 문자열을 쿠키로 보내기
                        conn.setRequestProperty("Cookie", sessionId);
                    }

                    //응답 코드를 읽어온다.
                    int responseCode=conn.getResponseCode();

                    if(responseCode==200){//정상 응답이라면...
                        //서버가 출력하는 문자열을 읽어오기 위한 객체
                        isr=new InputStreamReader(conn.getInputStream());
                        br=new BufferedReader(isr);
                        //반복문 돌면서 읽어오기
                        while(true){
                            //한줄씩 읽어들인다.
                            String line=br.readLine();
                            //더이상 읽어올 문자열이 없으면 반복문 탈출
                            if(line==null)break;
                            //읽어온 문자열 누적 시키기
                            builder.append(line);
                        }
                    }
                }
                //서버가 응답한 쿠키 목록을 읽어온다.
                List<String> cookList=conn.getHeaderFields().get("Set-Cookie");
                //만일 쿠키가 존대 한다면
                if(cookList != null){
                    //반복문 돌면서
                    for(String tmp : cookList){
                        //session id 가 들어 있는 쿠키를 찾아내서
                        if(tmp.contains("JSESSIONID")){
                            //session id 만 추출해서
                            String sessionId=tmp.split(";")[0];
                            //SharedPreferences 을 편집할수 있는 객체를 활용해서
                            SharedPreferences.Editor editor=pref.edit();
                            //sessionId 라는 키값으로 session id 값을 저장한다.
                            editor.putString("sessionId", sessionId);
                            editor.apply();//apply() 는 비동기로 저장하기 때문에 실행의 흐름이 잡혀 있지 않다(지연이 없음)
                            //필드에도 담아둔다.
                            MainActivity.this.sessionId=sessionId;
                        }
                    }
                }
                //출력받은 문자열 전체 얻어내기
                JSONObject obj=new JSONObject(builder.toString());
                /*
                    {"isLogin":false} or {"isLogin":true, "id":"kimgura"}
                    서버에서 위와 같은 형식의 json 문자열을 응답할 예정이다.
                 */
                Log.d("서버가 응답한 문자열", builder.toString());
                //로그인 여부를 읽어와서
                isLogin=obj.getBoolean("isLogin");
                //만일 로그인을 했다면
                if(isLogin){
                    //필드에 로그인된 아이디를 담아둔다.
                    id=obj.getString("id");
                }
            }catch(Exception e){//예외가 발생하면
                Log.e("LoginCheckTask", e.getMessage());
            }finally {
                try{
                    if(isr!=null)isr.close();
                    if(br!=null)br.close();
                    if(conn!=null)conn.disconnect();
                }catch(Exception e){}
            }
            //로그인 여부를 리턴하면 아래의 onPostExecute() 메소드에 전달된다.
            return isLogin;
        }

        @Override
        protected void onPostExecute(Boolean isLogin) {
            super.onPostExecute(isLogin);
            //만일 로그인 하지 않았다면
            if(!isLogin){
                //로그인 액티비티로 이동
                Intent intent=new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }

    class UploadTask extends AsyncTask<Map<String, String>, Void, String>{
        private String filePath;
        private String fileParamName;
        private final String boundary;
        private static final String LINE_FEED = "\r\n"; //개행기호 설정
        private String charset;

        //생성자
        public UploadTask(){
            // 경계선은 사용할때 마다 다른 값을 사용하도록 time milli 를 조합해서 사용한다. (캐쉬방지)
            boundary = "===" + System.currentTimeMillis() + "===";
            charset="utf-8";
            //서버에서 GalleryDto 의 image 라는 필드명과 일치(MultipartFile)
            fileParamName="image"; // <input type="file" name="image"/>
            //업로드할 파일이 어디 있는지 정보를 필드에 넣어준다.
            filePath=imagePath;
        }


        @Override
        protected String doInBackground(Map<String, String>... maps) {
            Map<String, String> param=maps[0];
            //서버가 http 요청에 대해서 응답하는 문자열을 누적할 객체
            StringBuilder builder=new StringBuilder();
            HttpURLConnection conn=null;
            InputStreamReader isr=null;
            PrintWriter pw=null;
            OutputStream os=null;
            FileInputStream fis=null;
            BufferedReader br=null;

            try{
                //URL 객체 생성
                URL url=new URL(AppConstants.BASE_URL+"/api/gallery/insert");
                //HttpURLConnection 객체의 참조값 얻어오기
                conn=(HttpURLConnection)url.openConnection();
                if(conn!=null){//연결이 되었다면
                    conn.setConnectTimeout(20000); //응답을 기다리는 최대 대기 시간
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setUseCaches(false);//케쉬 사용 여부

                    //App 에 저장된 session id 가 있다면 요청할때 쿠키로 같이 보내기
                    if(!sessionId.equals("")) {
                        // JSESSIONID=xxx 형식의 문자열을 쿠키로 보내기
                        conn.setRequestProperty("Cookie", sessionId);
                    }

                    //전송하는 데이터에 맞게 값 설정하기
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    conn.setRequestProperty("User-Agent", "CodeJava Agent");
                    //인터냇을 통해서 서버로 출력할수 있는 스트림 객체의 참조값 얻어오기
                    os=conn.getOutputStream();
                    //출력할 스트림 객체 얻어오기
                    pw=new PrintWriter(new OutputStreamWriter(os, charset));
                    //-------------- 전송 파라미터 추가  ------------------
                    if(param!=null){//요청 파리미터가 존재 한다면

                        Set<String> keySet=param.keySet();
                        Iterator<String> it=keySet.iterator();

                        //반복문 돌면서 map 에 담긴 모든 요소를 전송할수 있도록 구성한다.
                        while(it.hasNext()){
                            String key=it.next();
                            pw.append("--" + boundary).append(LINE_FEED);
                            pw.append("Content-Disposition: form-data; name=\"" + key + "\"")
                                    .append(LINE_FEED);
                            pw.append("Content-Type: text/plain; charset=" + charset).append(
                                    LINE_FEED);
                            pw.append(LINE_FEED);
                            pw.append(param.get(key)).append(LINE_FEED);
                            pw.flush();
                        }
                    }
                    //------------- File Field ------------------
                    File file=new File(filePath);
                    String filename=file.getName(); //파일명
                    pw.append("--" + boundary).append(LINE_FEED);
                    pw.append("Content-Disposition: form-data; name=\"" + fileParamName + "\"; filename=\"" + filename + "\"")
                            .append(LINE_FEED);
                    pw.append("Content-Type: " + URLConnection.guessContentTypeFromName(filename))
                            .append(LINE_FEED);
                    pw.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                    pw.append(LINE_FEED);
                    pw.flush();
                    //파일에서 읽어들일 스트림 객체 얻어내기
                    fis = new FileInputStream(file);
                    //byte 알갱이를 읽어들일 byte[] 객체 (한번에 4 kilo byte 씩 읽을수 있다)
                    byte[] buffer = new byte[4096];

                    //반복문 돌면서
                    while(true){
                        //byte 를 읽어들이고 몇 byte 를 읽었는지 리턴 받는다.
                        int readedByte=fis.read(buffer);
                        //더이상 읽을게 없다면 반복문 탈출
                        if(readedByte==-1)break;
                        //읽은 만큼큼 출력하기
                        os.write(buffer, 0, readedByte);
                        os.flush();
                    }

                    pw.append(LINE_FEED);
                    pw.flush();
                    pw.append(LINE_FEED).flush();
                    pw.append("--" + boundary + "--").append(LINE_FEED);
                    pw.flush();
                    //응답 코드를 읽어온다.
                    int responseCode=conn.getResponseCode();

                    if(responseCode==200){//정상 응답이라면...
                        //서버가 출력하는 문자열을 읽어오기 위한 객체
                        isr=new InputStreamReader(conn.getInputStream());
                        br=new BufferedReader(isr);
                        //반복문 돌면서 읽어오기
                        while(true){
                            //한줄씩 읽어들인다.
                            String line=br.readLine();
                            //더이상 읽어올 문자열이 없으면 반복문 탈출
                            if(line==null)break;
                            //읽어온 문자열 누적 시키기
                            builder.append(line);
                        }
                    }
                    //서버가 응답한 쿠키 목록을 읽어온다.
                    List<String> cookList=conn.getHeaderFields().get("Set-Cookie");
                    //만일 쿠키가 존대 한다면
                    if(cookList != null){
                        //반복문 돌면서
                        for(String tmp : cookList){
                            //session id 가 들어 있는 쿠키를 찾아내서
                            if(tmp.contains("JSESSIONID")){
                                //session id 만 추출해서
                                String sessionId=tmp.split(";")[0];
                                //SharedPreferences 을 편집할수 있는 객체를 활용해서
                                SharedPreferences.Editor editor=pref.edit();
                                //sessionId 라는 키값으로 session id 값을 저장한다.
                                editor.putString("sessionId", sessionId);
                                editor.apply();//apply() 는 비동기로 저장하기 때문에 실행의 흐름이 잡혀 있지 않다(지연이 없음)
                                //필드에도 담아둔다.
                                MainActivity.this.sessionId=sessionId;
                            }
                        }
                    }
                }
            }catch(Exception e){//예외가 발생하면
                Log.e("UploadTask", e.getMessage());
            }finally {
                try{
                    if(pw!=null)pw.close();
                    if(isr!=null)isr.close();
                    if(br!=null)br.close();
                    if(fis!=null) isr.close();
                    if(os!=null)os.close();
                    if(conn!=null)conn.disconnect();
                }catch(Exception e){}
            }
            //응답 받은 json 문자열 리턴하기
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}

