package com.example.step07fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/*
    [ Fragment ]

    - 액티비티의 제어하에 존재하는 mini Controller
    - 재활용을 염두에 두고 만드는 경우가 많다.
    - 재활용이라는 것은 여러개의 액티비티에서 활용된다는 의미

    [ Fragment 만드는 방법 ]
    1. Fragment 클래스를 상속 받는다.
    2. 프레그먼트의 layout xml 문서를 만든다.
    3. onCreateView( ) 메소드를 오버라이딩 한다.
 */
public class MyFragment extends Fragment implements View.OnClickListener{
    // 필드
    TextView textView;
    int count=0;

    // layout 으로 사용할 View 를 만들어서 리턴해 주어야한다.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. fragment_my.xml 문서를 전개해서 View 를 만든 다음
        View view = inflater.inflate(R.layout.fragment_my, container, false);
        // 만든 View 에서 TextView 의 참조 값을 얻어낸다.
        textView = view.findViewById(R.id.textView);
        textView.setOnClickListener(this);

        // 2. 해당 View 를 리턴해준다.
        return view;
    }
    // TextView 를 클릭하면 호출되는 메소드
    @Override
    public void onClick(View view) {
        // 카운트 값을 1 증가 시키고
        count++;
        // 정수를 문자열로 만들어서 TextView 에 출력하기
        textView.setText(Integer.toString(count));

        /*
            만일 count 값이 10의 배수이면 이 fragment 를 제어하고 있는 액티비티에 해당 정보를 알려보자!
            - 액티비티에 특정 메소드를 호출하면서 문자열 전달하기
            - 단, 특정 액티비티의 의존성은 없어야 된다.
         */

        // 이 fragment 를 제어하고 있는 액티비티의 참조 값 얻어내기
        FragmentActivity fa = getActivity();

        // 혹시 액티비티가 MyFragmentListener 인터페이스를 구현하지 않았을수도 있기 때문에 type 을 확인해서 casting 한다.
        // instanceof 연산자가 fa가 MyFragmentListener type 인지 확인해준다.
        if (count % 10 == 0 && fa instanceof MyFragmentListener) {
            MyFragmentListener ma = (MyFragmentListener) getActivity();
            ma.sendMsg(count+" 입니다. 액티비티님!");
        }
    }

    // 액티비티에서 특정 시점에 호출할 예정인 메소드
    public void reset(){
        count=0;
        textView.setText("0");
    }

    // 이 fragment 에서 전달하는 메세지를 받을 액티비티에 구현할 인터페이스를 클래스 안에 정의 하기
    public interface MyFragmentListener{
        public void sendMsg(String msg);
    }
}
