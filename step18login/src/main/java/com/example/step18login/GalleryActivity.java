package com.example.step18login;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.step18login.databinding.ActivityGalleryBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityGalleryBinding binding;

    SharedPreferences pref;
    String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // View binding 을 이용해서 화면 구성하기
        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_gallery);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        // 우 하단의 floating action 버튼을 눌렀을때 동작할 리스너 등록하기
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Snackbar 에 메시지를 길게 띄우기
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_gallery);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // SharedPreferences 객체의 참조 값 얻어와서 필드에 저장하기
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        // 저장된 session id 가 있는지 읽어와 본다. (없다면 기본 값 " ")
        sessionId = pref.getString("sessionId", "");

        // 갤러리 1 페이지의 데이터 요청 task 실행하기
        new GalleryListTask().execute(1);
    }

    class GalleryListTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... integers) {
            // 갤러리 목록 요청 URL
            String requestUrl = AppConstants.BASE_URL+"/gallery/list/pageNum="+integers[0];
            // 서버가 http 요청에 대해서 응답하는 문자열을 누적할 객체
            StringBuilder builder = new StringBuilder();
            HttpURLConnection conn = null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            try{
                // URL 객체 생성
                URL url = new URL(requestUrl);
                // HttpURLConnection 객체의 참조 값 얻어오기
                conn = (HttpURLConnection)url.openConnection();
                if(conn != null){ // 연결이 되었다면
                    conn.setConnectTimeout(20000); // 응답을 기다리는 최대 대기 시간
                    conn.setRequestMethod("GET"); // Default 설정
                    conn.setUseCaches(false); // 캐쉬 사용 여부
                    // App 에 저장된 session id 가 있다면 요청할때 쿠키로 같이 보내기
                    if(!sessionId.equals("")) {
                        // JSESSIONID = xxx 형식의 문자열을 쿠키로 보내기
                        conn.setRequestProperty("Cookie", sessionId);
                    }

                    // 응답 코드를 읽어온다.
                    int responseCode = conn.getResponseCode();

                    if(responseCode == 200){ // 정상 응답이라면...
                        // 서버가 출력하는 문자열을 읽어오기 위한 객체
                        isr = new InputStreamReader(conn.getInputStream());
                        br = new BufferedReader(isr);
                        // 반복문 돌면서 읽어오기
                        while(true){
                            // 한줄씩 읽어들인다.
                            String line = br.readLine();
                            // 더이상 읽어올 문자열이 없으면 반복문 탈출
                            if(line==null)break;
                            // 읽어온 문자열 누적 시키기
                            builder.append(line);
                        }
                    }else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) { // 로그인이 안된 상태라면
                        // 예외를 발생시켜서 try ~ catch ~ finally 절이 정상 수행되도록 한다.
                        throw new RuntimeException("로그인이 필요합니다.");
                    }
                }
                // 서버가 응답한 쿠키 목록을 읽어온다.
                List<String> cookList = conn.getHeaderFields().get("Set-Cookie");
                if(cookList != null){
                    // 반복문 돌면서
                    for(String tmp : cookList){
                        // session id 가 들어 있는 쿠키를 찾아내서
                        if(tmp.contains("JSESSIONID")){
                            // session id 만 추출해서
                            String sessionId = tmp.split(";")[0];
                            // SharedPreferences 를 편집할 수 있는 객체를 활용해서
                            SharedPreferences.Editor editor = pref.edit();
                            // sessionId 라는 키 값으로 session id 값을 저장한다.
                            editor.putString("sessionId", sessionId);
                            editor.apply(); // apply() 는 비동기로 저장하기 때문에 실행의 흐름이 잡혀있지 않다.(지연이 없음)
                            // 필드에도 담는다.
                            GalleryActivity.this.sessionId = sessionId;
                        }
                    }
                }

            }catch(Exception e){ // 예외가 발생하면
                Log.e("GalleryListTask", e.getMessage());
            }finally {
                try{
                    if(isr!=null)isr.close();
                    if(br!=null)br.close();
                    if(conn!=null)conn.disconnect();
                }catch(Exception e){}
            }
            // StringBuilder 객체에 담긴 문자열을 리턴해준다.
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String s) { // 위에서 return 된 값이 String s 에 들어간다.
            super.onPostExecute(s);
            // 응답된 문자열을 토스트 메세지로 띄워보기
            Toast.makeText(GalleryActivity.this, s, Toast.LENGTH_LONG).show();
        }
    }
}