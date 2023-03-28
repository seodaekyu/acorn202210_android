package com.example.step25imagecapture;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.step25imagecapture.databinding.ActivityDetailBinding;
import com.example.step25imagecapture.util.MyHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity implements MyHttpUtil.RequestListener {

    ActivityDetailBinding binding;
    SharedPreferences pref;
    String sessionId;
    GalleryDto dto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ActivityDetailBinding 객체의 참조값 얻어내기
        binding=ActivityDetailBinding.inflate(getLayoutInflater());
        //화면 구성은 binding 객체를 활용해서 한다.
        setContentView(binding.getRoot());

        //DetailActivity 가 활성화 될때 전달 받은 Intent 객체의 참조값 얻어오기
        //GalleryListActivity 에서 생성한 Intent 객체이기 때문에
        // "dto" 라는 키값으로 GalleryDto 객체가 들어 있다.
        Intent intent=getIntent();
        dto=(GalleryDto)intent.getSerializableExtra("dto");

        //이미지 출력(Glide 를 활용)
        Glide.with(this)
                .load(dto.getImagePath())
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.imageView);
        //세부 정보 출력
        binding.writer.setText("writer:"+dto.getWriter());
        binding.caption.setText(dto.getCaption());
        binding.regdate.setText(dto.getRegdate());

        pref=PreferenceManager.getDefaultSharedPreferences(this);
        sessionId=pref.getString("sessionId", "");

        //삭제 버튼에 리스너 등록
        binding.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setMessage("삭제 하시겠습니까?")
                    .setPositiveButton("네", (dialog, which) -> {
                        //삭제할 갤러리 사진의 Primary Key 를 이용해서 삭제 작업을 진행한다.
                        int num=dto.getNum();

                    })
                    .setNegativeButton("아니요", null)
                    .create()
                    .show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onSuccess(int requestId, String data) {

    }

    @Override
    public void onFail(int requestId, Map<String, Object> result) {

    }


    //로그인 여부를 체크하는 작업을 할 비동기 task
    class LoginCheckTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
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
                            DetailActivity.this.sessionId=sessionId;
                        }
                    }
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
            //서버에서 응답받은 문자열을 리턴한다.
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String jsonStr) {
            super.onPostExecute(jsonStr);
            Log.d("jsonStr", jsonStr);
            try{
                //json 문자열을 이용해서 JSONObject 객체를 생성한다.
                JSONObject obj=new JSONObject(jsonStr);
                //로그인 여부
                boolean isLogin=obj.getBoolean("isLogin");
                if(isLogin){
                    //로그인된 아이디를 읽어와서
                    String id=obj.getString("id");
                    //갤러리 writer 와 비교해서 같으면 삭제 버튼을 보이게 한다.
                    if(id.equals(dto.getWriter())){
                        //삭제 버튼을 보이도록 한다.
                        binding.deleteBtn.setVisibility(View.VISIBLE);
                    }
                }
            }catch (JSONException je){
                Log.e("onPostExecute()", je.getMessage());
            }
        }
    }

}

