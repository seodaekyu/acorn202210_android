package com.example.step25imagecapture.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
    - Http 요청을 할때 서버가 응답하는 쿠키를 모두 읽어서 저장하고 싶다
    - 다음번 Http 요청을 할때 저장된 쿠키를 모두 보내고 싶다
    - 쿠키 값이 수정되어서 응답되면 저장되어 있는 쿠키를 수정해야 한다.
    - 그러면 쿠키를 SQLiteDataBase 를 활용해서 관리하면 빠르게 처리 할수 있지 않을까?
 */
public class MyHttpUtil {
    //필드
    private Context context;
    private DBHelper dbHelper;
    //생성자
    public MyHttpUtil(Context context){
        this.context=context;
        //DBHelper 객체의 참조값을 얻어내서 필드에 저장해 둔다.
        dbHelper=new DBHelper(context, "CookieDB.sqlite", null, 1);
    }
    //이 유틸리티를 사용하는 곳에서 구현해야 하는 인터페이스
    public interface RequestListener{
        public void onSuccess(int requestId, String data);
        public void onFail(int requestId, Map<String, Object> result);
    }

    class DBHelper extends SQLiteOpenHelper{

        public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
        //해당 DBHelper 를 처음 사용할때 호출되는 메소드 (new DBHelper() 를 처음 호춯할때)
        @Override
        public void onCreate(SQLiteDatabase db) {
            //테이블을 만들면 된다.
            String sql="CREATE TABLE board_cookie (cookie_name TEXT PRIMARY KEY, cookie TEXT)";
            db.execSQL(sql);
        }
        //DB 를 리셋(업그래이드)할때 호출되는 메소드
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //업그래이드할 내용을 작성하면 된다.
            db.execSQL("DROP TABLE IF EXISTS board_cookie"); //만일 테이블이 존재하면 삭제한다.
            //다시 만들어 질수 있도록 onCreate() 메소드를 호출한다.
            onCreate(db);
        }
    }
    /*
        GET 방식 요청을 하는 메소드
     */
    public void sendGetRequest(int requestId, String requestUrl, Map<String, String> params,
                               RequestListener listener){
        //GET 방식 요청을 할 비동기 Task 객체를 생성해서
        GetRequestTask task=new GetRequestTask();
        //필요한 값을 넣어주고
        task.setRequestId(requestId);
        task.setRequestUrl(requestUrl);
        task.setListener(listener);
        //비동기 Task 를 실행한다.
        task.execute(params);
    }
    /*
        POST 방식 요청을 하는 메소드
    */
    public void sendPostRequest(int requestId, String requestUrl, Map<String, String> params,
                                RequestListener listener){
        //POST 방식 요청을 할 비동기 Task 객체를 생성해서
        PostRequestTask task=new PostRequestTask();
        //필요한 값을 넣어주고
        task.setRequestId(requestId);
        task.setRequestUrl(requestUrl);
        task.setListener(listener);
        //비동기 Task 를 실행한다.
        task.execute(params);
    }
    /*
        파일업로드 요청을 하는 메소드
     */
    public void fileUploadRequest(int requestId, String requestUrl, Map<String, String> params,
                                  RequestListener listener, File file){
        FileUploadTask task=new FileUploadTask();
        //필요한 값을 넣어주고
        task.setRequestId(requestId);
        task.setRequestUrl(requestUrl);
        task.setListener(listener);
        task.setFile(file);
        //비동기 Task 를 실행한다.
        task.execute(params);
    }
    private class FileUploadTask extends AsyncTask<Map<String, String>, Void, String>{
        //필요한 필드 구성
        private int requestId;
        private String requestUrl;
        private RequestListener listener;
        private File file;
        // 전송되는 파일의 파라미터명 설정(프로젝트 상황에 맞게 변경해서 사용해야한다.)
        private final String FILE_PARAM_NAME = "image";

        private final String boundary;
        private static final String LINE_FEED = "\r\n"; //개행기호 설정
        private String charset;

        //생성자
        public FileUploadTask(){
            // 경계선은 사용할때 마다 다른 값을 사용하도록 time milli 를 조합해서 사용한다. (캐쉬방지)
            boundary = "===" + System.currentTimeMillis() + "===";
            charset="utf-8";
        }

        public void setRequestId(int requestId) {
            this.requestId = requestId;
        }

        public void setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public void setListener(RequestListener listener) {
            this.listener = listener;
        }

        public void setFile(File file) {
            this.file = file;
        }

        @Override
        protected String doInBackground(Map<String, String>... maps) {
            Map<String, String> param=maps[0];
            //서버가 http 요청에 대해서 응답하는 문자열을 누적할 객체
            StringBuilder builder=new StringBuilder();
            HttpURLConnection conn=null;
            InputStreamReader isr=null;
            PrintWriter pw=null;
            OutputStream os=null;
            FileInputStream fis=null;
            BufferedReader br=null;

            try{
                //URL 객체 생성
                URL url=new URL(requestUrl);
                //HttpURLConnection 객체의 참조값 얻어오기
                conn=(HttpURLConnection)url.openConnection();
                if(conn!=null){//연결이 되었다면
                    conn.setConnectTimeout(20000); //응답을 기다리는 최대 대기 시간
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setUseCaches(false);//케쉬 사용 여부
                    //저장된 쿠키가 있다면 읽어내서 쿠키도 같이 보내기
                    SQLiteDatabase db=dbHelper.getReadableDatabase();
                    String sql="SELECT cookie FROM board_cookie";
                    //select 된 결과를 Cursor 에 담아온다.
                    Cursor cursor=db.rawQuery(sql, null);
                    while(cursor.moveToNext()){
                        String cookie=cursor.getString(0);
                        conn.addRequestProperty("Cookie", cookie);
                    }

                    //전송하는 데이터에 맞게 값 설정하기
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    conn.setRequestProperty("User-Agent", "CodeJava Agent");
                    //인터냇을 통해서 서버로 출력할수 있는 스트림 객체의 참조값 얻어오기
                    os=conn.getOutputStream();
                    //출력할 스트림 객체 얻어오기
                    pw=new PrintWriter(new OutputStreamWriter(os, charset));
                    //-------------- 전송 파라미터 추가  ------------------
                    if(param!=null){//요청 파리미터가 존재 한다면

                        Set<String> keySet=param.keySet();
                        Iterator<String> it=keySet.iterator();

                        //반복문 돌면서 map 에 담긴 모든 요소를 전송할수 있도록 구성한다.
                        while(it.hasNext()){
                            String key=it.next();
                            pw.append("--" + boundary).append(LINE_FEED);
                            pw.append("Content-Disposition: form-data; name=\"" + key + "\"")
                                    .append(LINE_FEED);
                            pw.append("Content-Type: text/plain; charset=" + charset).append(
                                    LINE_FEED);
                            pw.append(LINE_FEED);
                            pw.append(param.get(key)).append(LINE_FEED);
                            pw.flush();
                        }
                    }
                    //------------- File Field ------------------
                    //이미 필드에 업로드할 File 객체의 참조값이 있기 때문에 필드의 값을 사용하면 된다.
                    String filename=file.getName(); //파일명
                    pw.append("--" + boundary).append(LINE_FEED);
                    pw.append("Content-Disposition: form-data; name=\"" + FILE_PARAM_NAME + "\"; filename=\"" + filename + "\"")
                            .append(LINE_FEED);
                    pw.append("Content-Type: " + URLConnection.guessContentTypeFromName(filename))
                            .append(LINE_FEED);
                    pw.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                    pw.append(LINE_FEED);
                    pw.flush();
                    //파일에서 읽어들일 스트림 객체 얻어내기
                    fis = new FileInputStream(file);
                    //byte 알갱이를 읽어들일 byte[] 객체 (한번에 4 kilo byte 씩 읽을수 있다)
                    byte[] buffer = new byte[4096];

                    //반복문 돌면서
                    while(true){
                        //byte 를 읽어들이고 몇 byte 를 읽었는지 리턴 받는다.
                        int readedByte=fis.read(buffer);
                        //더이상 읽을게 없다면 반복문 탈출
                        if(readedByte==-1)break;
                        //읽은 만큼큼 출력하기
                        os.write(buffer, 0, readedByte);
                        os.flush();
                    }

                    pw.append(LINE_FEED);
                    pw.flush();
                    pw.append(LINE_FEED).flush();
                    pw.append("--" + boundary + "--").append(LINE_FEED);
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
                    }
                    //서버가 응답한 쿠키 목록을 읽어온다.
                    List<String> cookList=conn.getHeaderFields().get("Set-Cookie");
                    //만일 쿠키가 존대 한다면
                    if(cookList != null){
                        //반복문 돌면서 DB 에 저장한다.
                        //새로 응답된 쿠키라면 insert, 이미 존재하는 쿠키라면 update
                        SQLiteDatabase db2=dbHelper.getWritableDatabase();
                        for(String cookie:cookList){
                            //쿠키의 이름
                            String cookie_name=cookie.split("=")[0];
                            //쿠키의 이름을 String[] 에 담고
                            String[] arg={cookie_name};
                            //해당 쿠키가 이미 존재하는지 select 해 본다.
                            Cursor cursor2=db2.rawQuery("SELECT * FROM board_cookie WHERE cookie_name=?", arg);
                            //select 된 row 의 갯수
                            int selectRow=cursor2.getCount();
                            if(selectRow == 0){//새로운 쿠키이면 저장
                                Object[] args={cookie_name, cookie};
                                db2.execSQL("INSERT INTO board_cookie (cookie_name, cookie) VALUES(?, ?)", args);
                            }else{//이미 존재하는 쿠키이면 수정
                                Object[] args={cookie, cookie_name};
                                db2.execSQL("UPDATE board_cookie SET cookie=? WHERE cookie_name=?", args);
                            }
                        }
                        // .close() 해야지만 실제로 반영된다.
                        db2.close();
                    }
                }
            }catch(Exception e){//예외가 발생하면
                Log.e("fileUploadTask", e.getMessage());
            }finally {
                try{
                    if(pw!=null)pw.close();
                    if(isr!=null)isr.close();
                    if(br!=null)br.close();
                    if(fis!=null) isr.close();
                    if(os!=null)os.close();
                    if(conn!=null)conn.disconnect();
                }catch(Exception e){}
            }
            //응답 받은 json 문자열 리턴하기
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            listener.onSuccess(requestId, s);
        }
    }


    private class GetRequestTask extends AsyncTask<Map<String, String>, Void, String>{
        //필요한 필드 구성
        private int requestId;
        private String requestUrl;
        private RequestListener listener;

        public void setRequestId(int requestId) {
            this.requestId = requestId;
        }

        public void setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public void setListener(RequestListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(Map<String, String>... maps) {
            //maps 배열의 0 번방에 GET 방식 요청 파라미터가 들어 있다.
            //파라미터가 없으면 null 이 전달될 예정
            Map<String, String> param = maps[0];
            if(param!=null){//요청 파리미터가 존재 한다면
                //서버에 전송할 데이터를 문자열로 구성하기
                StringBuffer buffer=new StringBuffer();
                //Map 에 존재하는 key 값을 Set 에 담아오기
                Set<String> keySet=param.keySet();
                Iterator<String> it=keySet.iterator();
                boolean isFirst=true;
                //반복문 돌면서 map 에 담긴 모든 요소를 전송할수 있도록 구성한다.
                while(it.hasNext()){
                    String key=it.next();
                    String arg=null;
                    //파라미터가 한글일 경우 깨지지 않도록 하기 위해.
                    String encodedValue=null;
                    try {
                        encodedValue= URLEncoder.encode(param.get(key), "utf-8");
                    } catch (UnsupportedEncodingException e) {}
                    if(isFirst){
                        arg="?"+key+"="+encodedValue;
                        isFirst=false;
                    }else{
                        arg="&"+key+"="+encodedValue;
                    }
                    buffer.append(arg);
                }
                String data=buffer.toString();
                //GET 방식 요청 파라미터를 요청 url 뒤에 연결한다.
                requestUrl +=data;
            }
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
                if(conn!=null){
                    conn.setConnectTimeout(20000); //응답을 기다리는 최대 대기 시간
                    conn.setRequestMethod("GET");//Default 설정
                    conn.setUseCaches(false);//케쉬 사용 여부
                    //저장된 쿠키가 있다면 읽어내서 쿠키도 같이 보내기
                    SQLiteDatabase db=dbHelper.getReadableDatabase();
                    String sql="SELECT cookie FROM board_cookie";
                    //select 된 결과를 Cursor 에 담아온다.
                    Cursor cursor=db.rawQuery(sql, null);
                    while(cursor.moveToNext()){
                        String cookie=cursor.getString(0);
                        conn.addRequestProperty("Cookie", cookie);
                    }
                    //응답 코드를 읽어온다. (200, 404, 500 등등의 값)
                    int responseCode=conn.getResponseCode();
                    if(responseCode==200){
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
                    }else if(responseCode==301 || responseCode==302 || responseCode==303){
                        //리다일렉트 요청할 경로를 얻어내서
                        String location=conn.getHeaderField("Location");
                        //해당 경로로 다시 요청을 해야 한다.

                    }else if(responseCode >= 400 && responseCode < 500){
                        //요청 오류인 경우에 이 요청은 실패!

                    }else if(responseCode == 500){
                        //서버의 잘못된 동작으로 인한 요청 실패!

                    }
                }
                //서버가 응답한 쿠키 목록을 읽어온다.
                List<String> cookList=conn.getHeaderFields().get("Set-Cookie");
                //만일 쿠키가 존재 한다면
                if(cookList != null){
                    //반복문 돌면서 DB 에 저장한다.
                    //새로 응답된 쿠키라면 insert, 이미 존재하는 쿠키라면 update
                    SQLiteDatabase db=dbHelper.getWritableDatabase();
                    for(String cookie:cookList){
                        //쿠키의 이름
                        String cookie_name=cookie.split("=")[0];
                        //쿠키의 이름을 String[] 에 담고
                        String[] arg={cookie_name};
                        //해당 쿠키가 이미 존재하는지 select 해 본다.
                        Cursor cursor=db.rawQuery("SELECT * FROM board_cookie WHERE cookie_name=?", arg);
                        //select 된 row 의 갯수
                        int selectRow=cursor.getCount();
                        if(selectRow == 0){//새로운 쿠키이면 저장
                            Object[] args={cookie_name, cookie};
                            db.execSQL("INSERT INTO board_cookie (cookie_name, cookie) VALUES(?, ?)", args);
                        }else{//이미 존재하는 쿠키이면 수정
                            Object[] args={cookie, cookie_name};
                            db.execSQL("UPDATE board_cookie SET cookie=? WHERE cookie_name=?", args);
                        }
                    }
                    // .close() 해야지만 실제로 반영된다.
                    db.close();
                }

            }catch(Exception e){
                Log.e("MyHttpUtil.sendGetRequest()", e.getMessage());
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //RequestListener 객체에(액티비티 or 서비스 or 프레그먼트 .. ) 넣어주기
            listener.onSuccess(requestId, s);
        }
    }


    private class PostRequestTask extends AsyncTask<Map<String, String>, Void, String>{
        //필요한 필드 구성
        private int requestId;
        private String requestUrl;
        private RequestListener listener;

        public void setRequestId(int requestId) {
            this.requestId = requestId;
        }

        public void setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public void setListener(RequestListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(Map<String, String>... maps) {
            //maps 배열의 0 번방에 GET 방식 요청 파라미터가 들어 있다.
            //파라미터가 없으면 null 이 전달될 예정
            Map<String, String> param = maps[0];
            //query 문자열을 누젓 시킬 StringBuffer
            StringBuffer buffer=new StringBuffer();
            if(param!=null){//요청 파리미터가 존재 한다면
                //서버에 전송할 데이터를 문자열로 구성하기
                //Map 에 존재하는 key 값을 Set 에 담아오기
                Set<String> keySet=param.keySet();
                Iterator<String> it=keySet.iterator();
                boolean isFirst=true;
                //반복문 돌면서 map 에 담긴 모든 요소를 전송할수 있도록 구성한다.
                while(it.hasNext()){
                    String key=it.next();
                    String arg=null;
                    //파라미터가 한글일 경우 깨지지 않도록 하기 위해.
                    String encodedValue=null;
                    try {
                        encodedValue= URLEncoder.encode(param.get(key), "utf-8");
                    } catch (UnsupportedEncodingException e) {}
                    if(isFirst){
                        arg=key+"="+encodedValue;
                        isFirst=false;
                    }else{
                        arg="&"+key+"="+encodedValue;
                    }
                    //query 문자열을 StringBuffer 에 누적 시키기
                    buffer.append(arg);
                }
            }
            //post 방식으로 전송할때 사용할 query문자열
            String queryString=buffer.toString();

            //서버가 http 요청에 대해서 응답하는 문자열을 누적할 객체
            StringBuilder builder=new StringBuilder();
            HttpURLConnection conn=null;
            InputStreamReader isr=null;
            BufferedReader br=null;
            PrintWriter pw=null;
            try{
                //URL 객체 생성
                URL url=new URL(requestUrl);
                //HttpURLConnection 객체의 참조값 얻어오기
                conn=(HttpURLConnection)url.openConnection();
                if(conn!=null){
                    conn.setConnectTimeout(20000); //응답을 기다리는 최대 대기 시간
                    conn.setRequestMethod("POST");//POST 방식
                    conn.setUseCaches(false);//케쉬 사용 여부
                    //저장된 쿠키가 있다면 읽어내서 쿠키도 같이 보내기
                    SQLiteDatabase db=dbHelper.getReadableDatabase();
                    String sql="SELECT cookie FROM board_cookie";
                    //select 된 결과를 Cursor 에 담아온다.
                    Cursor cursor=db.rawQuery(sql, null);
                    while(cursor.moveToNext()){
                        String cookie=cursor.getString(0);
                        conn.addRequestProperty("Cookie", cookie);
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

                    //응답 코드를 읽어온다. (200, 404, 500 등등의 값)
                    int responseCode=conn.getResponseCode();
                    if(responseCode==200){
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
                    }else if(responseCode==301 || responseCode==302 || responseCode==303){
                        //리다일렉트 요청할 경로를 얻어내서
                        String location=conn.getHeaderField("Location");
                        //해당 경로로 다시 요청을 해야 한다.

                    }else if(responseCode >= 400 && responseCode < 500){
                        //요청 오류인 경우에 이 요청은 실패!

                    }else if(responseCode == 500){
                        //서버의 잘못된 동작으로 인한 요청 실패!

                    }
                }
                //서버가 응답한 쿠키 목록을 읽어온다.
                List<String> cookList=conn.getHeaderFields().get("Set-Cookie");
                //만일 쿠키가 존재 한다면
                if(cookList != null){
                    //반복문 돌면서 DB 에 저장한다.
                    //새로 응답된 쿠키라면 insert, 이미 존재하는 쿠키라면 update
                    SQLiteDatabase db=dbHelper.getWritableDatabase();
                    for(String cookie:cookList){
                        //쿠키의 이름
                        String cookie_name=cookie.split("=")[0];
                        //쿠키의 이름을 String[] 에 담고
                        String[] arg={cookie_name};
                        //해당 쿠키가 이미 존재하는지 select 해 본다.
                        Cursor cursor=db.rawQuery("SELECT * FROM board_cookie WHERE cookie_name=?", arg);
                        //select 된 row 의 갯수
                        int selectRow=cursor.getCount();
                        if(selectRow == 0){//새로운 쿠키이면 저장
                            Object[] args={cookie_name, cookie};
                            db.execSQL("INSERT INTO board_cookie (cookie_name, cookie) VALUES(?, ?)", args);
                        }else{//이미 존재하는 쿠키이면 수정
                            Object[] args={cookie, cookie_name};
                            db.execSQL("UPDATE board_cookie SET cookie=? WHERE cookie_name=?", args);
                        }
                    }
                    // .close() 해야지만 실제로 반영된다.
                    db.close();
                }

            }catch(Exception e){
                Log.e("MyHttpUtil.sendGetRequest()", e.getMessage());
            }finally {
                try{
                    if(pw!=null)pw.close();
                    if(isr!=null)isr.close();
                    if(br!=null)br.close();
                    if(conn!=null)conn.disconnect();
                }catch(Exception e){}
            }
            //응답받은 문자열을 리턴한다.
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //RequestListener 객체에(액티비티 or 서비스 or 프레그먼트 .. ) 넣어주기
            listener.onSuccess(requestId, s);
        }
    }
}