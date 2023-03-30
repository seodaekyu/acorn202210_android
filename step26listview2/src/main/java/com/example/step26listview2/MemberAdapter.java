package com.example.step26listview2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<MemberDto> members;
    private ItemClickListener listener;
    // 어댑터 안에서 일어나는 특정 이벤트를 전달 받을 객체 type 을 미리 정의하기
    public interface ItemClickListener{
        public void clicked(int index);
    }

    // 생성자
    public MemberAdapter(List<MemberDto> members) {
        this.members = members;
    }
    // 리스너를 전달 받는 메소드
    public void setOnItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // cell.xml 문서를 전개해서 View 객체를 얻어낸다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell, parent, false);
        // ViewHolder 객체를 생성해서 리턴해 준다.
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberAdapter.ViewHolder holder, int position) {
        // position 에 해당하는 TextView 의 참조 값
        TextView textView = holder.getTextView();
        // TextView 에 회원의 이름 출력하기
        textView.setText(members.get(position).getName());
        // TextView 에 클릭 리스너 등록
        textView.setOnClickListener(v->{
            // ItemClickListener(여기서는 MainActivity 가 된다) 에 정보를 전달한다.
            if(listener != null) {
                listener.clicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    // ViewHolder 를 내부 클래스로 정의한다.
    public static class ViewHolder extends RecyclerView.ViewHolder{
        // 필드
        private TextView textView;
        // 생성자
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // TextView 의 참조 값을 얻어내서 필드에 저장
            textView = itemView.findViewById(R.id.textView);
        }
        // TextView getter 메소드
        public TextView getTextView() {
            return textView;
        }
    }
}
