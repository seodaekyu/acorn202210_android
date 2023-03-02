package com.example.step24fileio;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    EditText inputMsg, console;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 필요한 UI 의 참조 값 얻어오기
        inputMsg = findViewById(R.id.inputMsg);
        Button saveBtn = findViewById(R.id.saveBtn);
        Button saveBtn2 = findViewById(R.id.saveBtn2);
        // 버튼에 리스너 등록
        saveBtn.setOnClickListener(this);

        saveBtn2.setOnClickListener(this);

        // 버튼의 참조 값 얻어와서 리스너 등록
        Button readBtn = findViewById(R.id.readBtn);
        Button readBtn2 = findViewById(R.id.readBtn2);
        readBtn.setOnClickListener(this);
        readBtn2.setOnClickListener(this);
        // EditText 객체의 참조 값 얻어와서 필드에 저장
        console = findViewById(R.id.console);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveBtn:
                //saveToInternal();
                saveToInternal2();
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장 했습니다.")
                        .setNeutralButton("확인", null)
                        .create()
                        .show();
                break;
            case R.id.saveBtn2:
                saveToExternal();
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장 했습니다.")
                        .setNeutralButton("확인", null)
                        .create()
                        .show();
                break;
            case R.id.readBtn:
                //readFromInternal();
                readFromInternal2();
                break;
            case R.id.readBtn2:
                readFromExternal();
                break;
        }
    }

    // 내부 저장 장치로 부터 읽어들이기(첫번째 방법)
    public void readFromInternal() {
        // EditText 에 이미 출력된 내용 삭제
        console.setText("");
        try{
            // memo2.txt 파일로 부터 읽어 들일 수 있는 스트림 객체
            FileInputStream fis = openFileInput("memo2.txt");
            // 반복문 돌면서
            while(true) {
                // 문자 코드를 하나씩 읽어들인다.
                int code = fis.read();
                // 만일 다 읽었다면 반복문 탈출
                if(code == -1) break;
                // 문자 코드에 해당하는 char 얻어내기
                char ch = (char)code;
                // char 를 String 으로 변환해서 EditText 에 출력하기
                console.append(Character.toString(ch));
            }
        }catch(Exception e) {
            Log.e("readFromInternal()", e.getMessage());
        }
    }

    // 내부 저장 장치로 부터 읽어들이기(두번째 방법)
    public  void readFromInternal2() {
        console.setText("");
        try{
            // 내부 저장 장치의 memo2.txt 파일을 access 할 수 있는 File 객체 생성
            File file = new File(getFilesDir(), "memo2.txt");
            // 문자열을 읽어들일 수 있는 객체로 포장
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            // 반복문 돌면서 문자열 한줄씩 읽어들이기
            while(true) {
                String line = br.readLine();
                // 더이상 읽을게 없다면 반복문 탈출
                if(line == null)break;
                // 읽은 문자열 한 줄을 개행 기호와 함께 출력하기
                console.append(line+"\n");
            }
        }catch(Exception e) {
            Log.e("readFromInternal2", e.getMessage());
        }
    }

    // 외부 저장 장치로 부터 읽어들이기
    public void readFromExternal() {
        console.setText("");
        try{
            // 외부 저장 장치의 절대 경로 얻어내기
            String path = getExternalFilesDir(null).getAbsolutePath();
            File file = new File(path+"/memo.txt");
            // 문자열을 읽어들일 수 있는 객체로 포장
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            // 반복문 돌면서 문자열 한줄씩 읽어들이기
            while(true) {
                String line = br.readLine();
                // 더이상 읽을게 없다면 반복문 탈출
                if (line == null) break;
                // 읽은 문자열 한 줄을 개행 기호와 함께 출력하기
                console.append(line + "\n");
            }
        }catch(Exception e) {
            Log.e("readFromExternal()", e.getMessage());

        }
    }

    // 외부 저장 장치에 저장하기
    public void saveToExternal(){
        // 입력한 문자열 읽어오기
        String msg = inputMsg.getText().toString();
        // 외부 저장 장치의 폴더를 가리키는 File 객체
        File externalDir = getExternalFilesDir(null);
        // 해당 폴더의 절대 경로를 얻어낸다.
        String absolutePath = externalDir.getAbsolutePath();
        Log.d("absolutePath", absolutePath);
        // 텍스트 파일을 만들기 위한 파일 객체 생성
        File file = new File(absolutePath + "/memo.txt");

        try{
            FileWriter fw = new FileWriter(file, true);
            fw.append(msg+"\n");
            fw.flush();
            fw.close();
        }catch(Exception e) {
            Log.e("saveToExternal()", e.getMessage());
        }
    }

    // 내부 저장 장치에 저장하기
    public void saveToInternal(){
        // 입력한 문자열을 읽어온다.
        String msg = inputMsg.getText().toString();
        try{
            // 파일을 저장하기 위한 디렉토리 만들기
            File dir = new File(getFilesDir(), "myDir");
            if(!dir.exists()) {
                dir.mkdir();
            }
            // 해당 디렉토리에 파일을 만들기 위한 File 객체
            File file = new File(dir, "memo.txt");
            FileWriter fw = new FileWriter(file, true);
            fw.append(msg+"\n");
            fw.flush();
            fw.close();
        }catch(Exception e){
            Log.e("saveToInternal()", e.getMessage());
        }

    }

    // 내부 저장 장치에 저장하기2
    public void saveToInternal2(){
        // 입력한 문자열을 읽어온다.
        String msg = inputMsg.getText().toString();
        try{
            FileOutputStream fos = openFileOutput("memo2.txt", MODE_APPEND);
            PrintWriter pw = new PrintWriter(fos);
            pw.println(msg+"\n");
            pw.flush();
            pw.close();
        }catch(Exception e){
            Log.e("saveToInternal()", e.getMessage());
        }

    }

}