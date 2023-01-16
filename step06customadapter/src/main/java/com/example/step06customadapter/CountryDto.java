package com.example.step06customadapter;

import java.io.Serializable;

public class CountryDto implements Serializable {
    // 필드
    private int resId;
    private String name;
    private String content;

    // 디폴트 생성자
    public CountryDto(){}

    public CountryDto(int resId, String name, String content) {
        this.resId = resId; // 출력할 이미지의 리소스 아이디 R.id.austria 등등의 값
        this.name = name; // 나라의 이름
        this.content = content; // 나라에 대한 자세한 설명
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
