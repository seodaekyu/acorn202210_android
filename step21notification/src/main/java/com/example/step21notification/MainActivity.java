package com.example.step21notification;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/*
    알림을 띄우기 위해서 필요한 작업
    1. 알림 채널을 만들어야 한다.
    2. 사용자가 직접 알림을 허용하도록 유도해야 한다.
    3. AndroidManifset.xml 에 알림 permission 설정이 있어야 한다.
 */

public class MainActivity extends AppCompatActivity {
    //알림 채널의 이름 정하기
    public static final String CHANNEL_NAME = "com.example.step21notification.MY_CHANNEL";
    //필요한 필드
    EditText inputMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputMsg = findViewById(R.id.inputMsg);
        // Auto Cancel 버튼을 눌렀을때 동작
        Button notiBtn = findViewById(R.id.notiBtn);
        notiBtn.setOnClickListener(v -> {
            makeAutoCancelNoti();
        });

        // 수동 취소 버튼을 눌렀을때 동작
        Button notiBtn2 = findViewById(R.id.notiBtn2);
        notiBtn2.setOnClickListener(v->{
            makeManualCancelNoti();
        });

        // 만일 알림이 가능한 상태가 아니라면(알림 채널이 만들어져 있지 않다면)
        if(!NotificationManagerCompat.from(this).areNotificationsEnabled()){
            // 알림 채널을 만든다.
            createNotificationChannel();
        }
    }

    // 수동으로 취소하는 알림을 띄우는 메소드
    public void makeManualCancelNoti(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 필요한 목록을 배열에 담는다.
            String[] permissions = {Manifest.permission.POST_NOTIFICATIONS};
            // 배열을 전달해서 해당 권한을 부여하도록 요청한다.
            ActivityCompat.requestPermissions(this,
                    permissions,
                    0); // 요청의 아이디
            return;
        }
        // 입력한 문자열을 읽어온다.
        String msg = inputMsg.getText().toString();
        // createNotificationChannel();
        // 알림을 클릭했을때 활성화시킬 액티비티 정보를 담고 있는 Intent 객체
        Intent intent = new Intent(this, DetailActivity.class);
        // 액티비티를 실행하는데 새로 운 TASK 에서 실행되도록 한다. (기존에 onStop() 에 머물러 있다면 제거하고 새로 시작)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // 인텐트에 msg 라는 키 값으로 String type 을 담는다. 새로 시작된 액티비티에서 읽어낼 수 있다.
        intent.putExtra("msg", msg);

        // 알림의 아이디 얻어내기
        int currentId = (int) (System.currentTimeMillis() / 1000);
        // 알림의 아이디를 Intent 객체에 담기
        intent.putExtra("notiId", currentId);

        // 인텐트 전달자 객체
        PendingIntent pendingIntent = PendingIntent.getActivity(this, currentId, intent, PendingIntent.FLAG_MUTABLE);

        // 띄울 알림을 구성하기
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_NAME)
                .setSmallIcon(android.R.drawable.star_on) // 알림의 아이콘
                .setContentTitle("알림을 취소해 주세요") // 알림의 제목
                .setContentText(msg) // 알림의 내용
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 알림의 우선순위
                .setContentIntent(pendingIntent) // 인텐트 전달자 객체
                .setAutoCancel(false); // 자동 취소 되는 알림인지 여부

        // 알림 만들기
        Notification noti = builder.build();

        // 알림 매니저를 이용해서 알림을 띄운다.
        NotificationManagerCompat.from(this).notify(currentId, noti);
    }

    // 자동으로 취소하는 알림을 띄우는 메소드
    public void makeAutoCancelNoti(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 필요한 목록을 배열에 담는다.
            String[] permissions = {Manifest.permission.POST_NOTIFICATIONS};
            // 배열을 전달해서 해당 권한을 부여하도록 요청한다.
            ActivityCompat.requestPermissions(this,
                    permissions,
                    0); // 요청의 아이디
            return;
        }
        // 입력한 문자열을 읽어온다.
        String msg = inputMsg.getText().toString();
        // createNotificationChannel();
        // 알림을 클릭했을때 활성화시킬 액티비티 정보를 담고 있는 Intent 객체
        Intent intent = new Intent(this, DetailActivity.class);
        // 액티비티를 실행하는데 새로 운 TASK 에서 실행되도록 한다. (기존에 onStop() 에 머물러 있다면 제거하고 새로 시작)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // 인텐트에 msg 라는 키 값으로 String type 을 담는다. 새로 시작된 액티비티에서 읽어낼 수 있다.
        intent.putExtra("msg", msg);

        // 알림의 아이디 얻어내기
        int currentId = (int) (System.currentTimeMillis() / 1000);

        // 인텐트 전달자 객체
        PendingIntent pendingIntent = PendingIntent.getActivity(this, currentId, intent, PendingIntent.FLAG_MUTABLE);

        // 띄울 알림을 구성하기
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_NAME)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now) // 알림의 아이콘
                .setContentTitle("얘들아 나야~") // 알림의 제목
                .setContentText(msg) // 알림의 내용
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 알림의 우선순위
                .setContentIntent(pendingIntent) // 인텐트 전달자 객체
                .setAutoCancel(true); // 자동 취소 되는 알림인지 여부

        // 알림 만들기
        Notification noti = builder.build();

        // 알림 매니저를 이용해서 알림을 띄운다.
        NotificationManagerCompat.from(this).notify(currentId, noti);
    }


    // 앱의 사용자가 알림을 직접 관리 할수 있도록 알림 채널을 만들어야한다.
    public void createNotificationChannel(){
        // 알림 채널을 지원하는 기기인지 확인해서
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //알림 채널을 만들기

            // sample data
            String name = "삼성카드";
            String text = "광고!";
            // 알림 채널 객체를 얻어내서
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_NAME, name, NotificationManager.IMPORTANCE_DEFAULT);
            // 채널의 설명을 적고
            channel.setDescription(text);
            // 알림 매니저 객체를 얻어내서
            NotificationManager notiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            // 알림 채널을 만든다.
            notiManager.createNotificationChannel(channel);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 0:
                // 권한을 부여 했다면
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // 자동 취소 알림 띄우기
                    makeAutoCancelNoti();
                }else{ // 권한을 부여 하지 않았다면
                    Toast.makeText(this, "알림을 띄울 권한이 필요합니다.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}










