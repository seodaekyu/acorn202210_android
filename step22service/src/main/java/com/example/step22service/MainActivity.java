package com.example.step22service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    // 서비스의 참조 값을 저장할 필드
    MusicService service;
    // 서비스에 연결되었는지 여부
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 서비스 시작 버튼
        Button startBtn = findViewById(R.id.startBtn);
        // 서비스 종료 버튼
        Button stopBtn = findViewById(R.id.stopBtn);
        // 서비스 일시 정지 버튼
        Button pauseBtn = findViewById(R.id.pauseBtn);
        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);

        // 음악 재생 버튼을 눌렀을떄 감시할 리스너 등록
        Button playBtn = findViewById(R.id.playBtn);
        playBtn.setOnClickListener(this);

        // 액티비티 종료 버튼을 눌렀을때 감시할 리스너 등록
        Button endBtn = findViewById(R.id.endBtn);
        endBtn.setOnClickListener(this);

    }
    // 서비스에 연결한다.
    @Override
    protected void onStart() {
        super.onStart();

        // MusicService 에 연결할 인텐트 객체
        Intent intent = new Intent(this, MusicService.class);
        // 액티비티의 bindService() 메소드를 이용해서 연결한다.
        // 만일 서비스가 시작이 되지 않았으면 서비스 객체를 생성해서
        // 시작할 준비가 된 서비스에 바인딩이 된다.
        bindService(intent, sConn, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isConnected) {
            // 서비스 바인딩 해제
            unbindService(sConn);
            isConnected = false;
        }
    }

    // 서비스 연결 객체를 필드로 선언한다.
    ServiceConnection sConn = new ServiceConnection() {
        // 서비스에 연결되었을떄 호출되는 메소드
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            // IBinder 객체를 원래 type 으로 casting
            MusicService.LocalBinder lBinder = (MusicService.LocalBinder)binder;
            // MusicService 의 참조 값을 필드에 저장
            service = lBinder.getService();
            // 연결되었다고 표시
            isConnected = true;
        }
        // 서비스와 연결해제 되었을때 호출되는 메소드
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 연결 해제 되었다고 표시
            isConnected = false;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startBtn:
                // MusicService 를 시작 시킨다.
                Intent intent = new Intent(this, MusicService.class);
                // 액티비티의 메소드를 이동해서 서비스를 시작시킨다.
                startService(intent);
                break;
            case R.id.stopBtn:
                // MusicService 를 비활성화 시키기위한 객체
                Intent intent2 = new Intent(this, MusicService.class);
                // 액티비티의 메소드를 이동해서 서비스 종료 시키기
                stopService(intent2);
                break;
            case R.id.pauseBtn:
                // 필드에 저장되어 있는 MusicService 객체의 참조 값을 이용해서 메소드 호출
                service.pauseMusic();
                break;
            case R.id.playBtn:
                // 필드에 저장되어 있는 MusicService 객체의 참조 값을 이용해서 메소드 호출
                service.playMusic();
                break;
            case R.id.endBtn:
                finish(); // 액티비티 종료
                break;
        }
    }
    // 액티비티가 종료되기 직전에 호출되는 메소드
    @Override
    protected void onDestroy() {
        Log.e("MainActivity", "onDestroy()");
        // 종료 되기 전에 작업은 super.onDestroy() 호출하기 전에 한다.
        super.onDestroy();
    }
}