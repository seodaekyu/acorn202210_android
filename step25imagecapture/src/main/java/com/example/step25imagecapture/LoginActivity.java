package com.example.step25imagecapture;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.step25imagecapture.databinding.ActivityLoginBinding;
import com.example.step25imagecapture.util.MyHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements MyHttpUtil.RequestListener{
    ActivityLoginBinding binding;
    String id; //로그인 성공시 로그인된 아이디를 저장할 필드
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //화면 구성하기
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginBtn.setOnClickListener(v->{
            //입력한 아이디 비밀번호를 읽어와서
            String id=binding.inputId.getText().toString();
            String pwd=binding.inputPwd.getText().toString();
            //Map 에 담는다.
            Map<String, String> map=new HashMap<>();
            map.put("id", id);
            map.put("pwd", pwd);

            new MyHttpUtil(this)
                    .sendPostRequest(1, AppConstants.BASE_URL+"/music/login", map, this);
        });
        binding.resetBtn.setOnClickListener(v->{
            binding.inputId.setText("");
            binding.inputPwd.setText("");
        });
    }

    @Override
    public void onSuccess(int requestId, String data) {
        Log.d("data", data);
        boolean isSuccess=false;
        try{
            //data 는 {"isSuccess":true} or {"isSuccess":false} 형식의 문자열이다.
            JSONObject obj=new JSONObject(data);
            //로그인 성공여부 얻어내기
            isSuccess=obj.getBoolean("isSuccess");
            if(isSuccess){
                id=obj.getString("id");
            }
        }catch (JSONException je){
            Log.e("onSuccess()", je.getMessage());
        }

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

    @Override
    public void onFail(int requestId, Map<String, Object> result) {

    }

}