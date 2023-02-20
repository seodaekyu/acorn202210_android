package com.example.step23mp3player;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.step23mp3player.databinding.ActivityLoginBinding;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    String sessionId;
    SharedPreferences pref;
    String id; //로그인 성공시 로그인된 아이디를 저장할 필드
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //화면 구성하기
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //필요한 객체의 참조값 얻어와서 필드에 저장
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        //저장된 session id 가 있다면 읽어와 본다.
        sessionId=pref.getString("sessionId", "");

        binding.loginBtn.setOnClickListener(v->{
            //입력한 아이디 비밀번호를 읽어와서
            String id=binding.inputId.getText().toString();
            String pwd=binding.inputPwd.getText().toString();
            //Map 에 담는다.
            Map<String, String> map=new HashMap<>();
            map.put("id", id);
            map.put("pwd", pwd);

            new LoginTask().execute(map);
        });
        binding.resetBtn.setOnClickListener(v->{
            binding.inputId.setText("");
            binding.inputPwd.setText("");
        });
    }
    class LoginTask extends AsyncTask<Map<String, String>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Map<String, String>... params) {
            Map<String, String> param=params[0];
            String queryString="";
            if(param!=null){//요청 파리미터가 존재 한다면
                //서버에 전송할 데이터를 문자열로 구성하기
                StringBuffer buffer=new StringBuffer();
                Set<String> keySet=param.keySet();
                Iterator<String> it=keySet.iterator();
                boolean isFirst=true;
                //반복문 돌면서 map 에 담긴 모든 요소를 전송할수 있도록 구성한다.
                while(it.hasNext()){
                    String key=it.next();
                    String arg=null;
                    if(isFirst){
                        arg=key+"="+param.get(key);
                        isFirst=false;
                    }else{
                        arg="&"+key+"="+param.get(key);
                    }
                    buffer.append(arg);
                }
                queryString=buffer.toString();
            }
            //서버가 http 요청에 대해서 응답하는 문자열을 누적할 객체
            StringBuilder builder=new StringBuilder();
            HttpURLConnection conn=null;
            InputStreamReader isr=null;
            BufferedReader br=null;
            PrintWriter pw=null;
            //결과값을 담을 Map Type 객체
            Map<String,Object> map=new HashMap<String,Object>();
            //로그인 성공여부
            boolean isSuccess=false;
            try{
                //URL 객체 생성
                URL url=new URL(AppConstants.BASE_URL+"/music/login");
                //HttpURLConnection 객체의 참조값 얻어오기
                conn=(HttpURLConnection)url.openConnection();
                if(conn!=null){//연결이 되었다면
                    conn.setConnectTimeout(20000); //응답을 기다리는 최대 대기 시간
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setUseCaches(false);//케쉬 사용 여부

                    if(sessionId != null) {
                        conn.setRequestProperty("Cookie", sessionId);
                    }

                    //전송하는 데이터에 맞게 값 설정하기
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //폼전송과 동일
                    //출력할 스트림 객체 얻어오기
                    OutputStreamWriter osw=
                            new OutputStreamWriter(conn.getOutputStream());
                    //문자열을 바로 출력하기 위해 osw 객체를 PrintWriter 객체로 감싼다
                    pw=new PrintWriter(osw);
                    //서버로 출력하기
                    pw.write(queryString);
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
                        //출력받은 문자열 전체 얻어내기
                        String str=builder.toString();
                        Log.d("서버에서 응답한 문자열", str);
                        //출력받은 문자열 전체 얻어내기
                        JSONObject obj=new JSONObject(builder.toString());
                        isSuccess=obj.getBoolean("isSuccess");
                        if(isSuccess){
                            id=obj.getString("id");
                        }
                    }
                    List<String> cookList=conn.getHeaderFields().get("Set-Cookie");
                    if(cookList != null){
                        for(String tmp : cookList){
                            if(tmp.contains("JSESSIONID")){
                                String sessionId=tmp.split(";")[0];
                                SharedPreferences.Editor editor=pref.edit();
                                editor.putString("sessionId", sessionId);
                                editor.apply();
                                LoginActivity.this.sessionId=sessionId;
                            }
                        }
                    }
                }
            }catch(Exception e){//예외가 발생하면
                Log.e("LoginTask", e.getMessage());
            }finally {
                try{
                    if(pw!=null)pw.close();
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
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("알림")
                        .setMessage(id+" 님 로그인 되었습니다.")
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();//액티비티 종료
                            }
                        })
                        .create()
                        .show();
            }else{
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("알림")
                        .setMessage("아이디 혹은 비밀 번호가 틀려요")
                        .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                binding.inputId.setText("");
                                binding.inputPwd.setText("");
                            }
                        })
                        .create()
                        .show();
            }
        }
    }
}



