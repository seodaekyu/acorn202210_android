package com.example.step25imagecapture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GalleryAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<GalleryDto> list;
    private int layoutRes;
    private Context context;

    // 생성자
    public GalleryAdapter(Context context, int layoutRes, List<GalleryDto> list) {
        this.layoutRes = layoutRes;
        this.list = list;
        this.context = context;
        // 레이아웃 전개자 객체의 참조 값을 얻어내서 필드에 저장
        inflater = LayoutInflater.from(context);
    }
    // 전체 아이템의 개수 리턴
    @Override
    public int getCount() {
        return list.size();
    }
    // position 에 해당하는 아이템 리턴
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }
    // position 에 해당하는 아이템의 아이디(PK)
    @Override
    public long getItemId(int position) {
        return list.get(position).getNum();
    }
    // position 에 해당하는 View 를 리턴
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = inflater.inflate(layoutRes, parent, false);
        }
        // position 에 해당하는 GalleyDto 를 얻어내서
        GalleryDto dto = list.get(position);
        ImageView imageView = convertView.findViewById(R.id.imageView);
        TextView textWriter = convertView.findViewById(R.id.writer);
        TextView textCaption = convertView.findViewById(R.id.caption);
        TextView textRegdate = convertView.findViewById(R.id.regdate);

        textWriter.setText(dto.getWriter());
        textCaption.setText(dto.getCaption());
        textRegdate.setText(dto.getRegdate());
        // Glide 를 활용해서 imageView 에 이미지 출력하기
        Glide.with(context)
                .load(dto.getImagePath())
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageView);
        return convertView;
    }
}
