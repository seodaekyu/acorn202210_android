package com.example.step17example;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.step17example.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    view binding 사용하는 방법

    1. build.gradle 파일에 아래의 설정 추가

    buildFeatures{
        viewBinding = true
    }
    2. 우 상단에 sync now 링크를 눌러서 설정 적용
    3. layout xml 문서의 이름대로 클래스가 자동으로 만들어진다.
       예를 들어 activity_main.xml 문서면 ActivityMainBinding 클래스
                activity_sub.xml 문서면  ActivitySubBinding  클래스
 */
public class MainActivity extends AppCompatActivity implements Util.RequestListener, AdapterView.OnItemLongClickListener {
    // 필드
    List<TodoDto> list; // onCreate 에서만 사용되는게 아니기때문에 필드로 만들어놓고 사용 (usages 클릭해보자)
    TodoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView(R.layout.activity_main); 바인딩을 할때는 사용 안함
        // R.layout.activity_main.xml 문서를 전개해서 View 를 만들기
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        // 전개된 layout 에서 root 를 얻어 화면 구성을 한다. (여기서는 LinearLayout 이다.)
        setContentView(binding.getRoot());

        // ListView 의 참조 값 얻어오기
        // ListView listView = binding.listView;
        // binding 을 할때는 위 코드를 안쓰고 바로 참조 가능하다 binding.listView.setAdapter(adapter)

        // 어댑터에 넣어줄 모델 목록
        list = new ArrayList<>();
        // ListView 에 연결할 어댑터 객체 생성
        adapter = new TodoAdapter(this, R.layout.listview_cell, list); // this 는 (MainActivity)
        // ListView 에 어댑터 연결하기
        binding.listView.setAdapter(adapter);

        // Button 에 리스너 등록하기
        binding.addBtn.setOnClickListener(v->{
            // EditText 에 입력한 문자열을 읽어와서
            String content = binding.inputText.getText().toString();
            // 입력한 문자열을 Map 에 담는다.
            Map<String, String> map = new HashMap<>();
            map.put("content", content);
            // 원격지 웹서버에 post 방식으로 전송
            Util.sendPostRequest(AppConstants.REQUEST_TODO_INSERT,
                    AppConstants.BASE_URL+"/todo/insert",
                    map,
                    this);
        });

        // ListView 를 오래 클릭했을때 실행할 리스너 등록
        binding.listView.setOnItemLongClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // MainActivity 가 활성화 되는 시점에 원격지 서버의 데이터를 받아와서 ListView 에 출력하기
        Util.sendGetRequest(AppConstants.REQUEST_TODO_LIST,
                AppConstants.BASE_URL+"/todo/list",
                null,
                this);
    }

    @Override
    public void onSuccess(int requestId, Map<String, Object> result) {
        // 응답된 JSON 문자열 읽어오기
        String jsonStr = (String) result.get("data");

        // 만일 할일 추가 요청에 대한 응답이 성공이면?
        if(requestId == AppConstants.REQUEST_TODO_INSERT) {
            // jsonStr 은 {"isSuccess" : true} 형식의 JSON 문자열이 응답된다.
            Log.d("MainActivity onSuccess()", jsonStr);
            // 성공이면 목록을 다시 요청해서 UI 가 업데이트 되도록 한다.
            Util.sendGetRequest(AppConstants.REQUEST_TODO_LIST,
                    AppConstants.BASE_URL+"/todo/list",
                    null,
                    this);
        }else if(requestId == AppConstants.REQUEST_TODO_LIST) {
            // 기존 목록은 일단 삭제
            list.clear();
            // jsonStr 은 [{"num":x, "content":"xxx", "regdate":"xxx"}, {}, ... ] 형식의 JSON
            try{
                JSONArray arr = new JSONArray(jsonStr);
                // JSONArray 객체의 방의 갯수 만큼 반복문 돌면서
                for(int i=0; i<arr.length(); i++) {
                    // JSONObject 객체를 하나씩 얻어낸다.
                    JSONObject tmp = arr.getJSONObject(i); // 배열 안에 뭐가 들어있는지 확인하여 Object, boolean 등 활용하기
                    // JSONObject 에는 할일 하나가 들어 있다. => TodoDto 로 변경하면 된다.
                    TodoDto dto = new TodoDto();
                    dto.setNum(tmp.getInt("num"));
                    dto.setContent(tmp.getString("content"));
                    dto.setRegdate(tmp.getString("regdate"));
                    // TodoDto 객체를 List 에 누적시킨다.
                    list.add(dto);
                }
                // 모두 누적시켰다면 모델이 변경됐다고 어댑터에 알려서 ListView 가 업데이트 되도록 한다.
                adapter.notifyDataSetChanged();
            }catch (Exception e) {
                Log.e("MainActivity onSuccess()", e.getMessage());
            }
        }else if(requestId == AppConstants.REQUEST_TODO_DELETE) {
            // 성공이면 목록을 다시 요청해서 UI 가 업데이트 되도록 한다.
            Util.sendGetRequest(AppConstants.REQUEST_TODO_LIST,
                    AppConstants.BASE_URL+"/todo/list",
                    null,
                    this);
        }
    }

    @Override
    public void onFail(int requestId, Map<String, Object> result) {
        // 만일 할일 추가 요청에 대한 응답이 실패라면?
        if(requestId == AppConstants.REQUEST_TODO_INSERT) {

        }else if(requestId == AppConstants.REQUEST_TODO_LIST) {

        }
    }
    // ListView 의 cell 을 오랫동안 클릭하면 실행되는 메소드
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        new AlertDialog.Builder(this)
                .setTitle("알림")
                .setMessage("삭제 하시겠습니까?")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                            view 는 클릭한 cell 의 View
                            position 은 클릭한 cell 의 인덱스
                            id 는 클릭한 cell 의 모델 primary key 값
                         */
                        // 삭제할 할일의 primary key 값 Map 에 담고
                        Map<String, String> map = new HashMap<>();
                        map.put("num", Long.toString(id));
                        // Util 을 이용해서 삭제
                        Util.sendPostRequest(AppConstants.REQUEST_TODO_DELETE,
                                AppConstants.BASE_URL+"/todo/delete",
                                map,
                                MainActivity.this); // 여기서 this 만 쓰면 interface.onClick 을 가르키기때문에 써줘야함
                    }
                })
                .setNegativeButton("아니요", null)
                .create()
                .show();

        return false;
    }
}