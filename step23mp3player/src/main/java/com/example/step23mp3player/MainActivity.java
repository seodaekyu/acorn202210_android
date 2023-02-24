package com.example.step23mp3player;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemClickListener,
        MusicService.OnMoveToListener{
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
    //Adapter 에 연결된 모델
    List<String> songs;
    //Adapter 의 참조값
    ArrayAdapter<String> adapter;
    SharedPreferences pref;
    String sessionId;
    String id;
    //재생음악 목록
    List<MusicDto> musicList=new ArrayList<>();
    //ListView 의 참조값을 저장할 필드
    ListView listView;
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
            service.setMusicList(musicList);
            //재생 위치가 다음곡으로 이동했을때 해당 이벤트를 감시할 리스너 등록
            service.setOnMoveToListener(MainActivity.this);
            //현재 재생 위치를 읽어와서
            int currentIndex=service.getCurrentIndex();
            listView.setItemChecked(currentIndex, true);
            adapter.notifyDataSetChanged();
            listView.smoothScrollToPosition(currentIndex);
            loadTitleImage(currentIndex);

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
            //서비스의 playMusic() 메소드를 호출해서 음악이 재생 되도록 한다.
            service.playMusic();
        });
        //일시 중지 버튼
        ImageButton pauseBtn=findViewById(R.id.pauseBtn);
        pauseBtn.setOnClickListener(v->{
            service.pauseMusic();
        });
        //뒤로 감기 버튼
        ImageButton rewBtn=findViewById(R.id.rewBtn);
        rewBtn.setOnClickListener(v->{
            service.rewMusic();
        });
        //앞으로 감기 버튼
        ImageButton ffBtn=findViewById(R.id.ffBtn);
        ffBtn.setOnClickListener(v->{
            service.ffMusic();
        });

        //알림체널만들기
        createNotificationChannel();
        //ListView 관련 작업
        listView=findViewById(R.id.listView);
        //셈플 데이터
        songs=new ArrayList<>();
        //ListView 에 연결할 아답타
        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, songs);
        listView.setAdapter(adapter);
        //ListView 에 아이템 클릭 리스너 등록
        listView.setOnItemClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // MusicService 에 연결할 인텐트 객체
        Intent intent=new Intent(this, MusicService.class);
        intent.setAction("Dummy Action");
        //서비스 시작 시키기
        //이미 서비스가 동작 중이라면 onStartCommand() 메소드만 다시 호출된다.
        startService(intent);
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        sessionId=pref.getString("sessionId", "");
        //로그인 했는지 체크하기
        new LoginCheckTask().execute(AppConstants.BASE_URL+"/music/logincheck");
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
    //ListView 의 cell 을 클릭하면 호출되는 메소드
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // position 은 클릭한 셀의 인덱스
        service.initMusic(position);
        loadTitleImage(position);
    }

    //타이틀 이미지를 로딩하는 메소드
    public void loadTitleImage(int index){
        //mp3 파일의 title 이미지를 얻어내는 작업
        MediaMetadataRetriever mmr=new MediaMetadataRetriever();
        String fileName=musicList.get(index).getSaveFileName();
        //mp3 파일 로딩
        mmr.setDataSource(AppConstants.MUSIC_URL+fileName);
        //이미지 data 를 byte[] 로 얻어내서
        byte[] imageData=mmr.getEmbeddedPicture();
        //만일 이미지 데이터가 있다면
        if(imageData != null) {
            // byte[] 을 활용해서 Bitmap 이미지를 얻어내고
            Bitmap image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            // Bitmap 이미지를 출력할 ImageView
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(image);
        }else{
            //기본 이미지를 출력한다든지 작업을 하면 된다.

        }
    }

    //MusicService 클래스 안에 정의된 OnMoveToListener 인터페이스를 구현해서 강제 오버라이드한 메소드
    @Override
    public void moved(int index) {
        //재생위치가 다음으로 이동했을때 호출되는 메소드로 만들 예정
        //ListView 의 selection 을 index 로 이동 시킨다
        listView.setItemChecked(index, true);
        //해당 인덱스로 부드럽게 스크롤 되게 한다.
        listView.smoothScrollToPosition(index);
        adapter.notifyDataSetChanged();
        //타이틀 이미지 바꾸기
        loadTitleImage(index);
    }

    //로그인 여부를 체크하는 작업을 할 비동기 task
    class LoginCheckTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            //로그인 체크 url
            String requestUrl=strings[0];
            //서버가 http 요청에 대해서 응답하는 문자열을 누적할 객체
            StringBuilder builder=new StringBuilder();
            HttpURLConnection conn=null;
            InputStreamReader isr=null;
            BufferedReader br=null;
            boolean isLogin=false;
            try{
                //URL 객체 생성
                URL url=new URL(requestUrl);
                //HttpURLConnection 객체의 참조값 얻어오기
                conn=(HttpURLConnection)url.openConnection();
                if(conn!=null){//연결이 되었다면
                    conn.setConnectTimeout(20000); //응답을 기다리는 최대 대기 시간
                    conn.setRequestMethod("GET");//Default 설정
                    conn.setUseCaches(false);//케쉬 사용 여부
                    //App 에 저장된 session id 가 있다면 요청할때 쿠키로 같이 보내기
                    if(!sessionId.equals("")) {
                        // JSESSIONID=xxx 형식의 문자열을 쿠키로 보내기
                        conn.setRequestProperty("Cookie", sessionId);
                    }
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
                    }
                }
                //서버가 응답한 쿠키 목록을 읽어온다.
                List<String> cookList=conn.getHeaderFields().get("Set-Cookie");
                //만일 쿠키가 존대 한다면
                if(cookList != null){
                    //반복문 돌면서
                    for(String tmp : cookList){
                        //session id 가 들어 있는 쿠키를 찾아내서
                        if(tmp.contains("JSESSIONID")){
                            //session id 만 추출해서
                            String sessionId=tmp.split(";")[0];
                            //SharedPreferences 을 편집할수 있는 객체를 활용해서
                            SharedPreferences.Editor editor=pref.edit();
                            //sessionId 라는 키값으로 session id 값을 저장한다.
                            editor.putString("sessionId", sessionId);
                            editor.apply();//apply() 는 비동기로 저장하기 때문에 실행의 흐름이 잡혀 있지 않다(지연이 없음)
                            //필드에도 담아둔다.
                            MainActivity.this.sessionId=sessionId;
                        }
                    }
                }
                //출력받은 문자열 전체 얻어내기
                JSONObject obj=new JSONObject(builder.toString());
                /*
                    {"isLogin":false} or {"isLogin":true, "id":"kimgura"}
                    서버에서 위와 같은 형식의 json 문자열을 응답할 예정이다.
                 */
                Log.d("서버가 응답한 문자열", builder.toString());
                //로그인 여부를 읽어와서
                isLogin=obj.getBoolean("isLogin");
                //만일 로그인을 했다면
                if(isLogin){
                    //필드에 로그인된 아이디를 담아둔다.
                    id=obj.getString("id");
                }
            }catch(Exception e){//예외가 발생하면
                Log.e("LoginCheckTask", e.getMessage());
            }finally {
                try{
                    if(isr!=null)isr.close();
                    if(br!=null)br.close();
                    if(conn!=null)conn.disconnect();
                }catch(Exception e){}
            }
            //로그인 여부를 리턴하면 아래의 onPostExecute() 메소드에 전달된다.
            return isLogin;
        }
        @Override
        protected void onPostExecute(Boolean isLogin) {
            super.onPostExecute(isLogin);
            //여기는 UI 스레드 이기 때문에 UI 와 관련된 작업을 할수 있다.
            //TextView 에 로그인 여부를 출력하기
            if(isLogin){
                TextView infoText=findViewById(R.id.infoText);
                infoText.setText(id+" 님 로그인중...");
                //재생목록 받아오기
                new MusicListTask().execute(AppConstants.BASE_URL+"/api/music/list");
                // 액티비티의 bindService() 메소드를 이용해서 연결한다.
                Intent intent=new Intent(MainActivity.this, MusicService.class);
                intent.setAction("Dummy Action");
                bindService(intent, sConn, Context.BIND_AUTO_CREATE);
            }else{
                //로그인 액티비티로 이동
                Intent intent=new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }
    //재생목록을 얻어올 작업을 할 비동기 task
    class MusicListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            //요청 url
            String requestUrl=strings[0];
            //서버가 http 요청에 대해서 응답하는 문자열을 누적할 객체
            StringBuilder builder=new StringBuilder();
            HttpURLConnection conn=null;
            InputStreamReader isr=null;
            BufferedReader br=null;
            try{
                //URL 객체 생성
                URL url=new URL(requestUrl);
                //HttpURLConnection 객체의 참조값 얻어오기
                conn=(HttpURLConnection)url.openConnection();
                if(conn!=null){//연결이 되었다면
                    conn.setConnectTimeout(20000); //응답을 기다리는 최대 대기 시간
                    conn.setRequestMethod("GET");//Default 설정
                    conn.setUseCaches(false);//케쉬 사용 여부
                    //App 에 저장된 session id 가 있다면 요청할때 쿠키로 같이 보내기
                    if(!sessionId.equals("")) {
                        // JSESSIONID=xxx 형식의 문자열을 쿠키로 보내기
                        conn.setRequestProperty("Cookie", sessionId);
                    }
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
                    }
                }
                //서버가 응답한 쿠키 목록을 읽어온다.
                List<String> cookList=conn.getHeaderFields().get("Set-Cookie");
                //만일 쿠키가 존대 한다면
                if(cookList != null){
                    //반복문 돌면서
                    for(String tmp : cookList){
                        //session id 가 들어 있는 쿠키를 찾아내서
                        if(tmp.contains("JSESSIONID")){
                            //session id 만 추출해서
                            String sessionId=tmp.split(";")[0];
                            //SharedPreferences 을 편집할수 있는 객체를 활용해서
                            SharedPreferences.Editor editor=pref.edit();
                            //sessionId 라는 키값으로 session id 값을 저장한다.
                            editor.putString("sessionId", sessionId);
                            editor.apply();//apply() 는 비동기로 저장하기 때문에 실행의 흐름이 잡혀 있지 않다(지연이 없음)
                            //필드에도 담아둔다.
                            MainActivity.this.sessionId=sessionId;
                        }
                    }
                }
            }catch(Exception e){//예외가 발생하면
                Log.e("MusicListTask", e.getMessage());
            }finally {
                try{
                    if(isr!=null)isr.close();
                    if(br!=null)br.close();
                    if(conn!=null)conn.disconnect();
                }catch(Exception e){}
            }
            //응답받은 문자열을 리턴한다.
            return builder.toString();
        }
        @Override
        protected void onPostExecute(String jsonStr) {
            super.onPostExecute(jsonStr);
            //여기는 UI 스레드
            // jsonStr 은 [{},{},...]  형식의 문자열이기 때문에 JSONArray 객체를 생성한다.
            songs.clear();
            musicList.clear();
            try {
                JSONArray arr = new JSONArray(jsonStr);
                for(int i=0; i<arr.length(); i++){
                    // i 번째 JSONObject 객체를 참조
                    JSONObject tmp=arr.getJSONObject(i);
                    int num=tmp.getInt("num");
                    String writer=tmp.getString("writer");
                    // "title" 이라는 키값으로 저장된 문자열 읽어오기
                    String title=tmp.getString("title");
                    String artist=tmp.getString("artist");
                    String orgFileName=tmp.getString("orgFileName");
                    String saveFileName=tmp.getString("saveFileName");
                    String regdate=tmp.getString("regdate");
                    //ListView 에 연결된 모델에 곡의 제목을 담는다
                    songs.add(title);
                    //음악하나의 자세한 정보를 MusicDto 에 담고
                    MusicDto dto=new MusicDto();
                    dto.setNum(num);
                    dto.setWriter(writer);
                    dto.setTitle(title);
                    dto.setArtist(artist);
                    dto.setOrgFileName(orgFileName);
                    dto.setSaveFileName(saveFileName);
                    dto.setRegdate(regdate);
                    //MusicDto 를 List 에 누적 시킨다.
                    musicList.add(dto);
                }
                adapter.notifyDataSetChanged();
            }catch (JSONException je){
                Log.e("onPoseExecute()", je.getMessage());
            }
        }
    }
}