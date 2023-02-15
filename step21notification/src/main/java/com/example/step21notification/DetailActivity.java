package com.example.step21notification;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 전달된 Intent 객체
        Intent intent = getIntent();
        // Intent 객체에 "msg" 라는 키 값으로 전달된 문자열 얻어내기
        String msg = intent.getStringExtra("msg");
        // TextView 에 출력해보기
        TextView textView = findViewById(R.id.textView);
        textView.setText(msg);

        Button cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(v->{
            // Intent 객체에 담긴 알림의 아이디 얻어내기
            int notiId = intent.getIntExtra("notiId", 0);
            // 알림의 아이디를 이용해서 알림 취소하기
            NotificationManagerCompat.from(this).cancel(notiId);
        });
    }
}