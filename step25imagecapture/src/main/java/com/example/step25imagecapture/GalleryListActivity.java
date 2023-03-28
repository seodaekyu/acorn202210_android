package com.example.step25imagecapture;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.step25imagecapture.databinding.ActivityGalleryListBinding;
import com.example.step25imagecapture.util.MyHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GalleryListActivity extends AppCompatActivity implements View.OnClickListener,
        MyHttpUtil.RequestListener {
    ActivityGalleryListBinding binding;
    //서버에서 받아온 갤러리 목록을 저장할 객체
    List<GalleryDto> list=new ArrayList<>();
    GalleryAdapter adapter;
    //진행중 알림을 띄우기 위한 객체
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //바인딩 객체의 참조값을 필드에 저장
        binding=ActivityGalleryListBinding.inflate(getLayoutInflater());
        //바인딩 객체를 이용해서 화면 구성
        setContentView(binding.getRoot());

        //진행중 알림을 구성한다.
        progress=new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("다운로드중...");

        //ListView 에 연결할 아답타 객체 생성
        adapter=new GalleryAdapter(this, R.layout.listview_cell, list);
        //ListView 에 아답타 연결하기
        binding.listView.setAdapter(adapter);
        //버튼에 리스너 등록하기
        binding.takePicBtn.setOnClickListener(this);
        binding.refreshBtn.setOnClickListener(this);
        //ListView 에 cell 을 클릭했을때 동작할 리스너 등록
        binding.listView.setOnItemClickListener((parent, view, position, id) -> {
            //position 은 클릭한 cell 의 인덱스 값이다.
            GalleryDto dto=list.get(position); //자세히 보여줄 GalleryDto 정보
            Intent intent=new Intent(this, DetailActivity.class);
            intent.putExtra("dto", dto);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        new MyHttpUtil(this).sendGetRequest(1,
                AppConstants.BASE_URL+"/api/gallery/list", null, this);
        //프로그래스 다이얼로그를 띄운다.
        progress.show();
    }

    //버튼을 눌렀을때 호출되는 메소드
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.takePicBtn:
                //사진을 찍어서 올리는 액티비티를 실행한다.
                Intent intent=new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.refreshBtn:
                //목록을 다시 받아온다.
                new MyHttpUtil(this).sendGetRequest(1,
                        AppConstants.BASE_URL+"/api/gallery/list", null, this);
                //프로그래스 다이얼로그를 띄운다.
                progress.show();
                break;
        }
    }

    @Override
    public void onSuccess(int requestId, String data) {
        //여기는 UI 스레드
        // data 는 [{},{},...]  형식의 문자열이기 때문에 JSONArray 객체를 생성한다.
        list.clear();
        try {
            JSONArray arr = new JSONArray(data);
            for(int i=0; i<arr.length(); i++){
                // i 번째 JSONObject 객체를 참조
                JSONObject tmp=arr.getJSONObject(i);
                int num=tmp.getInt("num");
                String writer=tmp.getString("writer");
                String caption=tmp.getString("caption");
                String imagePath=tmp.getString("imagePath");
                String regdate=tmp.getString("regdate");
                GalleryDto dto=new GalleryDto();
                dto.setNum(num);
                dto.setWriter(writer);
                dto.setCaption(caption);
                //http://xxx/xxx/resources/upload/xxx.jpg 형식의 문자열 구성해서 넣기
                dto.setImagePath(AppConstants.BASE_URL+imagePath);
                dto.setRegdate(regdate);
                //ArrayList 객체에 누적 시키기
                list.add(dto);
            }
            //모델이 바뀌었다고 아답타에 알려서 ListView 가 업데이트 되도록 한다.
            adapter.notifyDataSetChanged();
        }catch (JSONException je){
            Log.e("onPoseExecute()", je.getMessage());
        }
        progress.dismiss();
    }

    @Override
    public void onFail(int requestId, Map<String, Object> result) {
        progress.dismiss();
    }

}