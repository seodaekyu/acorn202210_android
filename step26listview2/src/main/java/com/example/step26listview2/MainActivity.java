package com.example.step26listview2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.example.step26listview2.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements MemberAdapter.ItemClickListener {

    private TextView mTextView;
    private ActivityMainBinding binding;
    MemberAdapter adapter;
    List<MemberDto> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // sample model
        members = new ArrayList<>();
        members.add(new MemberDto(1, "김구라", "노량진"));
        members.add(new MemberDto(2, "해골", "노량진2"));
        members.add(new MemberDto(3, "원숭이", "노량진3"));
        members.add(new MemberDto(4, "주뎅이", "노량진4"));
        members.add(new MemberDto(5, "덩어리", "노량진5"));

        WearableRecyclerView recyclerView = binding.recyView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        // 어댑터 객체를 생성해서
        adapter = new MemberAdapter(members);
        // RecyclerView 에 연결
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void clicked(int index) {
        // Model 을 변경하고
        members.get(index).setName(index+" clicked!");
        // 어댑터에 Model 이 변경되었다고 알린다.
        adapter.notifyDataSetChanged();
    }
}