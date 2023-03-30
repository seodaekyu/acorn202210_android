package com.example.step26listview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyAdapter extends RecyclerView.Adapter<RecyAdapter.ViewHolder> {
    // 모델
    private List<String> localDataSet;

    // 생성자의 인자로 모델을 전달 받아서 필드에 저장
    public RecyAdapter(List<String> dataSet) {
        localDataSet = dataSet;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // res/layout/cell.xml 문서를 전개해서 View 객체를 만들고
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell, parent, false);
        // 해당 View 객체를 이용해서 ViewHolder 객체를 생성해서 리턴한다.
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // ViewHolder 가 가지고 있는 TextView 의 참조 값을 얻어와서
        // position 에 해당하는 데이터를 출력한다.
        holder.getTextView().setText(localDataSet.get(position));
        holder.getTextView().setOnClickListener(v->{
            Log.d("Clicked!", "position"+position);
        });
    }

    @Override
    public int getItemCount() {
        // 모델의 개수 리턴
        return localDataSet.size();
    }

    // ViewModel 내부 클래스
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }
    }

}
