package com.example.step25imagecapture;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.step25imagecapture.databinding.ActivityGalleryListBinding;

import java.util.ArrayList;
import java.util.List;

public class GalleryListActivity extends AppCompatActivity implements View.OnClickListener{
    ActivityGalleryListBinding binding;
    // 서버에서 받아온 갤러리 목록을 저장할 객체
    List<GalleryDto> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 바인딩 객체의 참조 값을 필드에 저장
        binding = ActivityGalleryListBinding.inflate(getLayoutInflater());
        // 바인딩 객체를 이용해서 화면 구성
        setContentView(binding.getRoot());


        // ListView 에 연결할 어댑터 객체 생성
        GalleryAdapter adapter = new GalleryAdapter(this, R.layout.listview_cell, list);
        // ListView 에 어댑터 연결하기
        binding.listView.setAdapter(adapter);
        // 버튼에 리스너 등록하기
        binding.takePicBtn.setOnClickListener(this);
        binding.refreshBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 원격지 서버로 부터 갤러리 목록을 받아오는 요청을 한다.

    }

    // 버튼을 눌렀을때 호출되는 메소드
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.takePicBtn:

                break;
            case R.id.refreshBtn:

                break;
        }
    }
}