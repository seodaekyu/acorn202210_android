package com.example.step23mp3player;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity  {

    MediaPlayer mp;
    //재생 준비가 되었는지 여부
    boolean isPrepared=false;
    ImageButton playBtn;
    ProgressBar progress;
    TextView time;
    SeekBar seek;

    //서비스의 참조값을 저장할 필드
    MusicService service;
    //서비스에 연결되었는지 여부
    boolean isConnected;
    //서비스 연결객체
    ServiceConnection sConn=new ServiceConnection() {
        //서비스에 연결이 되었을때 호출되는 메소드
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //MusicService 객체의 참조값을 얻어와서 필드에 저장
            //IBinder 객체를 원래 type 으로 casting
            MusicService.LocalBinder lBinder=(MusicService.LocalBinder)binder;
            service=lBinder.getService();
            //연결되었다고 표시
            isConnected=true;
            //핸들러에 메세지 보내기
            handler.removeMessages(0); //만일 핸들러가 동작중에 있으면 메세지를 제거하고
            handler.sendEmptyMessageDelayed(0, 100); //다시 보내기
        }
        //서비스에 연결이 해제 되었을때 호출되는 메소드
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //연결 해제 되었다고 표시
            isConnected=false;
        }
    };

    //UI 를 주기적으로 업데이트 하기 위한 Handler
    Handler handler=new Handler(){
        /*
            이 Handler 에 메세지를 한번만 보내면 아래의 handleMessage() 메소드가
            1/10 초 마다 반복적으로 호출된다.
            handleMessage() 메소드는 UI 스레드 상에서 실행되기 때문에
            마음대로 UI 를 업데이트 할수가 있다.
         */
        @Override
        public void handleMessage(@NonNull Message msg) {

            if(service.isPrepared()){
                //전체 재생시간
                int maxTime=service.getMp().getDuration();
                progress.setMax(maxTime);
                seek.setMax(maxTime);
                //현재 재생 위치
                int currentTime=service.getMp().getCurrentPosition();
                //음악 재생이 시작된 이후에 주기적으로 계속 실행이 되어야 한다.
                progress.setProgress(currentTime);
                seek.setProgress(currentTime);
                //현재 재생 시간을 TextView 에 출력하기
                String info=String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes(currentTime),
                        TimeUnit.MILLISECONDS.toSeconds(currentTime)
                                -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS. toMinutes(currentTime)) );
                time.setText(info);
            }

            //자신의 객체에 다시 빈 메세제를 보내서 handleMessage() 가 일정시간 이후에 호출 되도록 한다.
            handler.sendEmptyMessageDelayed(0, 100); // 1/10 초 이후에
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TextView 의 참조값 얻어와서 필드에 저장
        time=findViewById(R.id.time);
        // %d 는 숫자, %s 문자
        String info=String.format("%d min, %d sec", 0, 0);
        time.setText(info);
        //ProgressBar 의 참조값 얻어오기
        progress=findViewById(R.id.progress);
        seek=findViewById(R.id.seek);

        //재생 버튼
        playBtn=findViewById(R.id.playBtn);
        //재생버튼을 눌렀을때
        playBtn.setOnClickListener(v->{
            //서비스의 initMusic() 메소드를 호출해서 음악이 재생 되도록 한다.
            service.initMusic("http://192.168.0.31:9000/boot07/resources/upload/mp3piano.mp3");
        });
        //일시 중지 버튼
        ImageButton pauseBtn=findViewById(R.id.pauseBtn);
        pauseBtn.setOnClickListener(v->{
            service.pauseMusic();
        });

        //알림체널만들기
        createNotificationChannel();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // MusicService 에 연결할 인텐트 객체
        Intent intent=new Intent(this, MusicService.class);
        //서비스 시작 시키기
        //startService(intent);
        // 액티비티의 bindService() 메소드를 이용해서 연결한다.
        // 만일 서비스가 시작이 되지 않았으면 서비스 객체를 생성해서
        // 시작할 준비만 된 서비스에 바인딩이 된다.
        bindService(intent, sConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isConnected){
            //서비스 바인딩 해제
            unbindService(sConn);
            isConnected=false;
        }
    }

    //앱의 사용자가 알림을 직접 관리 할수 있도록 알림 체널을 만들어야한다.
    public void createNotificationChannel(){
        //알림 체널을 지원하는 기기인지 확인해서
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //알림 체널을 만들기

            //셈플 데이터
            String name="Music Player";
            String text="Control";
            //알림체널 객체를 얻어내서
            //알림을 1/10 초마다 새로 보낼 예정이기 때문에 진동은 울리지 않도록 IMPORTANCE_LOW 로 설정한다
            NotificationChannel channel=
                    new NotificationChannel(AppConstants.CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            //체널의 설명을 적고
            channel.setDescription(text);
            //알림 메니저 객체를 얻어내서
            NotificationManager notiManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            //알림 체널을 만든다.
            notiManager.createNotificationChannel(channel);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 0:
                //권한을 부여 했다면
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{//권한을 부여 하지 않았다면
                    Toast.makeText(this, "알림을 띄울 권한이 필요합니다.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
