package com.example.step18login;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.step18login.databinding.ActivityLogoutBinding;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class LogoutActivity extends AppCompatActivity {
    ActivityLogoutBinding binding;
    String sessionId;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        sessionId = pref.getString("sessionId", "");

        // 로그아웃 Task 실행하기
        new LogoutTask().execute(AppConstants.BASE_URL+"/logout");
    }

    class LogoutTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            // 로그아웃 체크 url
            String requestUrl = strings[0];
            // 서버가 http 요청에 대해서 응답하는 문자열을 누적할 객체
            StringBuilder builder = new StringBuilder();
            HttpURLConnection conn = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            boolean isSuccess = false;
            try{
                // URL 객체 생성
                URL url = new URL(requestUrl);
                // HttpURLConnection 객체의 참조값 얻어오기
                conn = (HttpURLConnection)url.openConnection();
                if(conn!=null){ // 연결이 되었다면
                    conn.setConnectTimeout(20000); // 응답을 기다리는 최대 대기 시간
                    conn.setRequestMethod("GET"); // Default 설정
                    conn.setUseCaches(false); // 캐쉬 사용 여부

                    if(sessionId != null) {
                        conn.setRequestProperty("Cookie", sessionId);
                    }

                    // 응답 코드를 읽어온다.
                    int responseCode = conn.getResponseCode();

                    if(responseCode==200){ // 정상 응답이라면...
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
                    }
                }
                List<String> cookList = conn.getHeaderFields().get("Set-Cookie");
                if(cookList != null){
                    for(String tmp : cookList){
                        if(tmp.contains("JSESSIONID")){
                            String sessionId = tmp.split(";")[0];
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("sessionId", sessionId);
                            editor.apply();
                            LogoutActivity.this.sessionId = sessionId;
                        }
                    }
                }
                // 출력받은 문자열 전체 얻어내기
                JSONObject obj = new JSONObject(builder.toString());
                isSuccess = obj.getBoolean("isSuccess");
            }catch(Exception e){ // 예외가 발생하면
                Log.e("LoginCheckTask", e.getMessage());
            }finally {
                try{
                    if(isr!=null)isr.close();
                    if(br!=null)br.close();
                    if(conn!=null)conn.disconnect();
                }catch(Exception e){}
            }

            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            if(isSuccess){
                new AlertDialog.Builder(LogoutActivity.this)
                        .setTitle("알림")
                        .setMessage("로그 아웃 되었습니다")
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish(); // 액티비티 종료
                            }
                        })
                        .create()
                        .show();
            }
        }
    }
}