package com.example.step11bottomnavi.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.step11bottomnavi.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        // NotificationsViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));
        // 람다식에서 매개변수를 중복으로 쓰는 불편함을 없애는 이중콜론 :: 연산자
        // textView::setText 인자로 전달받은 값을 textView 객체의 setText() 를 호출하면서 전달을 해라 라는 의미
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        textView.setOnClickListener(v->{
            notificationsViewModel.setmText("clicked");
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}