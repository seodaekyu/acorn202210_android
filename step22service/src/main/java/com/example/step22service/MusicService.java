package com.example.step22service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/*
    - Service 는 Android 4 대 Component 중 하나다.
    - Service 추상 클래스를 상속받아서 만든다.
    - UI 없이 Activity 와는 별개로 백그라운드에서 동작이 가능하다.
    - Service 를 활성화 시키기 위해서는 Intent 객체가 필요하다.
 */
public class MusicService extends Service {
    // 필드
    MediaPlayer mp;

    // 생성자
    public MusicService() {
        Log.e("MusicService", "MusicService()");
    }
    // 서비스가 활성화(서비스 객체가 생성) 될때 최초 한번만 호출되는 메소드
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("MusicService", "onCreate()");
        // 음악 로딩하기
        mp = MediaPlayer.create(this, R.raw.mp3piano);
    }
    // 서비스가 시작될 때 호출되는 메소드
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MusicService", "onStartCommand()");
        // 음악재생
        mp.start();
        /*
            서비스는 원칙적으로 백그라운드에서 계속 실행되는 component 이지만
            운영체제가 자원이 부족하면 임의로 비활성화 시켰다가
            자원의 여유가 생기면 해당 서비스를 다시 시작 시켜주기도 한다.
         */

        // 운영체제가 강제로 종료시켜도 다시 시작되지 않도록
        return START_NOT_STICKY;
    }

    // 음악을 재생하고 일시 정지하는 메소드 추가
    public void playMusic() {
        mp.start();
    }
    public void pauseMusic() {
        mp.pause();
    }

    @Override
    public void onDestroy() {
        Log.e("MusicService", "onDestroy()");
        // 중지 및 자원 해제
        mp.stop();
        mp.release();
        super.onDestroy();
    }

    // 액티비티(혹은 다른 component) 에서 서비스에 연결되면 호출되는 메소드
    @Override
    public IBinder onBind(Intent intent) {

        // 필드에 있는 Binder 객체를 리턴해준다.
        return binder;
    }

    // 필드에 바인드 객체의 참조 값 넣어두기
    final IBinder binder = new LocalBinder();

    // Binder 클래스를 상속 받아서 LocalBinder 클래스를 정의 한다.
    public class LocalBinder extends Binder {
        // MusicService 객체의 참조 값을 리턴해주는 메소드
        public MusicService getService() {
            return MusicService.this;
        }
    }
}