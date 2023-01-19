package com.example.step09gameview;

public class Enemy {
    public int x, y; // 적의 좌표
    public int type; // 적의 type 0 or 1
    public boolean isDead; // 배열에서 제거할지 여부
    public int energy; // 에너지
    public int imageIndex; // 적의 이미지 인덱스 (애니메이션 효과 추가)
    public boolean isFall; // 현재 추락하고 있는지 여부
    public int angle; // 현재 회전각
    public int size; // 현재 크기
}
