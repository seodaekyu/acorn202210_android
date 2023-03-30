package com.example.step26wear;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.step26wear.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 바인딩 객체를 얻어내고
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // 바인딩 객체를 이용해서 화면 구성을 하고
        setContentView(binding.getRoot());
        // TextView 의 참조 값을 얻어와서 필드에 저장
        mTextView = binding.text;

        // 버튼에 리스너 등록하기
        binding.myBtn.setOnClickListener(v->{
            // TextView 조작하기
            binding.text.setText("Clicked!");
        });
    }
}