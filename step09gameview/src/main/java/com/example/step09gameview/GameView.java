package com.example.step09gameview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    // 필드
    Bitmap backImg; // 배경 이미지
    int width, height; // 화면의 폭과 높이 (GameView 가 차지하고 있는 화면의 폭과 높이)

    // 드래곤의 이미지를 저장할 배열
    Bitmap[] dragonImgs = new Bitmap[4];
    // 드래곤 이미지 인덱스
    int dragonIndex = 0;
    // 카운트를 셀 필드
    int count;
    // 유닛(드래곤, 적) 의 크기를 저장할 필드
    int unitSize;
    // 드래곤의 좌표를 저장할 필드(가운데 기준)
    int dragonX, dragonY;
    // 배경이미지의 y 좌표
    int back1Y, back2Y;
    // 미사일 객체를 저장할 List
    List<Missile> missList = new ArrayList<>();
    // 미사일의 크기
    int missSize;
    // 미사일 이미지를 담을 배열
    Bitmap[] missImgs = new Bitmap[3];
    // 미사일의 속도
    int speedMissile;
    // 적 이미지를 저장할 배열
    Bitmap[][] enemyImgs = new Bitmap[2][2];
    // Enemy 객체를 저장할 List
    List<Enemy> enemyList = new ArrayList<>();
    // 적의 x 좌표를 저장할 배열
    int[] enemyX = new int[5];
    // 랜덤한 숫자를 얻어낼 Random 객체
    Random ran = new Random();
    // 적이 만들어 진 이후 count
    int postCount;
    // 점수
    int point;


    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    // 초기화 메소드
    public void init(){
        // 원본 배경 이미지 읽어 들이기
        Bitmap backImg = BitmapFactory.decodeResource(getResources(), R.drawable.backbg);
        // 배경 이미지를 view 의 크기에 맞게 조절해서 필드에 저장
        this.backImg = Bitmap.createScaledBitmap(backImg, width, height, false);
        // 드래곤 이미지를 로딩해서 사이즈를 조절하고 배열에 저장한다.
        Bitmap dragonImg1 = BitmapFactory.decodeResource(getResources(), R.drawable.unit1);
        Bitmap dragonImg2 = BitmapFactory.decodeResource(getResources(), R.drawable.unit2);
        Bitmap dragonImg3 = BitmapFactory.decodeResource(getResources(), R.drawable.unit3);
        dragonImg1 = Bitmap.createScaledBitmap(dragonImg1, unitSize, unitSize, false);
        dragonImg2 = Bitmap.createScaledBitmap(dragonImg2, unitSize, unitSize, false);
        dragonImg3 = Bitmap.createScaledBitmap(dragonImg3, unitSize, unitSize, false);
        dragonImgs[0] = dragonImg1;
        dragonImgs[1] = dragonImg2;
        dragonImgs[2] = dragonImg3;
        dragonImgs[3] = dragonImg2;

        // 미사일 이미지 로딩
        Bitmap missImg1 = BitmapFactory.decodeResource(getResources(),R.drawable.mi1);
        Bitmap missImg2 = BitmapFactory.decodeResource(getResources(),R.drawable.mi2);
        Bitmap missImg3 = BitmapFactory.decodeResource(getResources(),R.drawable.mi3);
        // 미사일 이미지 크기 조절
        missImg1 = Bitmap.createScaledBitmap(missImg1,missSize, missSize, false);
        missImg2 = Bitmap.createScaledBitmap(missImg2,missSize, missSize, false);
        missImg3 = Bitmap.createScaledBitmap(missImg3,missSize, missSize, false);
        // 미사일 이미지를 배열에 넣어두기
        missImgs[0] = missImg1;
        missImgs[1] = missImg2;
        missImgs[2] = missImg3;

        // 적 이미지 로딩
        Bitmap enemyImg1 = BitmapFactory.decodeResource(getResources(), R.drawable.silver1);
        Bitmap enemyImg2 = BitmapFactory.decodeResource(getResources(), R.drawable.silver2);
        Bitmap enemyImg3 = BitmapFactory.decodeResource(getResources(), R.drawable.gold1);
        Bitmap enemyImg4 = BitmapFactory.decodeResource(getResources(), R.drawable.gold2);

        // 적 이미지 사이즈 조절
        enemyImg1 = Bitmap.createScaledBitmap(enemyImg1,unitSize, unitSize, false);
        enemyImg2 = Bitmap.createScaledBitmap(enemyImg2,unitSize, unitSize, false);
        enemyImg3 = Bitmap.createScaledBitmap(enemyImg3,unitSize, unitSize, false);
        enemyImg4 = Bitmap.createScaledBitmap(enemyImg4,unitSize, unitSize, false);

        // 적 이미지 배열에 저장
        enemyImgs[0][0] = enemyImg1; //0행 0열 silver1
        enemyImgs[0][1] = enemyImg2; //0행 1열 silver2
        enemyImgs[1][0] = enemyImg3; //1행 0열 gold1
        enemyImgs[1][1] = enemyImg4; //1행 1열 gold2

        // 적의 x 좌표를 구해서 배열에 저장한다.
        for(int i=0; i<5; i++) {
            enemyX[i] = i*unitSize + unitSize/2;
        }

        // Handler 객체에 메세지를 보내서 화면이 주기적으로 갱신되는 구조로 바꾼다.
        handler.sendEmptyMessageDelayed(0,20);
    }


    // View 가 활성화될때 최초 한번 호출되고 View 의 사이즈가 바뀌면 다시 호출된다.
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // view 가 차지하고 있는 폭과 높이가 px 단위로 w, h 에 전달된다.
        width = w;
        height = h;

        // unitSize 는 화면 폭의 1/5 로 설정
        unitSize = w/5;
        // 드래곤의 초기 좌표 부여
        dragonX = w/2;
        dragonY = height-unitSize/2;

        // 배경이미지의 초기 좌표
        back1Y = 0;
        back2Y = -height;

        // 미사일의 크기는 드래곤의 크기의 1/4
        missSize = unitSize/4;

        // 미사일의 속도는 화면의 높이/50
        speedMissile = h/50;

        // 초기화 메소드 호출
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 배경이미지 2개 그리기
        canvas.drawBitmap(backImg, 0, back1Y, null);
        canvas.drawBitmap(backImg, 0, back2Y, null);

        // 미사일 그리기
        for(Missile tmp:missList) {
            canvas.drawBitmap(missImgs[0], tmp.x-missSize/2, tmp.y-missSize*2, null);
        }

        // 적 그리기
        for(Enemy tmp:enemyList) {

            if(tmp.isFall) { // 추락상태인 적
                // canvas 의 정상 상태(변화를 가하지 않은 상태)를 임시 저장한다.
                canvas.save();
                // 적의 위치로 평행이동
                canvas.translate(tmp.x, tmp.y);
                canvas.rotate(tmp.angle);
                Bitmap scaled = Bitmap.createScaledBitmap(enemyImgs[tmp.type][tmp.imageIndex],
                        tmp.size, tmp.size, false);

                // 적을 원점에 그린다.
                canvas.drawBitmap(scaled, 50-unitSize/2, -unitSize/2, null);

                // 저장했던 상태로 되돌린다.
                canvas.restore();
            }else{ // 정상인 적
                canvas.drawBitmap(enemyImgs[tmp.type][tmp.imageIndex], tmp.x-unitSize/2, tmp.y-unitSize/2, null);
            }

        }

        // 글자를 출력하기 위한 Paint 객체
        Paint textP = new Paint();
        textP.setColor(Color.YELLOW);
        textP.setTextSize(50);
        // 점수 출력하기 .drawText( 출력할 문자열, 좌하단의 x, 좌하단 y, Paint 객체 )
        canvas.drawText("Point : "+point, 10, 60, textP);

        // 드래곤 그리기
        canvas.drawBitmap(dragonImgs[dragonIndex], dragonX-unitSize/2, dragonY-unitSize/2, null);


        // ---- 배경이미지 관련 처리 ----
        back1Y += 10;
        back2Y += 10;

        // 만일 배경 1의 좌표가 아래로 벗어나면
        if(back1Y >= height) {
            // 배경 1의 상단으로 다시 보낸다.
            back1Y =- height;
            // 배경 2와 오차가 생기지 않게 하기위해 복원하기
            back2Y = 0;
        }
        // 만일 배경 2의 좌표가 아래로 벗어나면
        if(back2Y >= height) {
            // 배경 1과 오차가 생기지 않게 하기위해 복원하기
            back2Y =- height;
            back1Y = 0;
        }

        count++;
        if(count%10 == 0) {
            // ---- 드래곤 애니메이션 관련 처리
            dragonIndex++;
            if(dragonIndex == 4) { // 만일 존재하지 않는 인덱스라면
                dragonIndex = 0; // 다시 처음으로 되돌리기
            }
        }

        missileService(); // 미사일 관련 처리 메소드 호출
        enemyService(); // 적 관련 처리 메소드 호출
        checkStrike(); // 미사일 적중 관련 처리 메소드 호출
    }

    // 적과 미사일의 충돌 검사하기
    public void checkStrike(){
        for(int i=0; i<missList.size(); i++){
            // i 번째 미사일 객체
            Missile m = missList.get(i);
            for(int j=0; j<enemyList.size(); j++){
                // j 번째 적 객체
                Enemy e = enemyList.get(j);
                // i 번째 미사일이 j 번째 적의 사각형 영역안에 있는지 여부
                boolean isStrike = m.x > e.x - unitSize/2 &&
                        m.x < e.x + unitSize/2 &&
                        m.y > e.y - unitSize/2 &&
                        m.y < e.y + unitSize/2;

                if(isStrike && !e.isFall) { // 현재 추락중인 적은 무시하기
                    // 적 에너지를 줄이고
                    e.energy -= 50;
                    // 미사일을 없앤다.
                    m.isDead = true;
                    // 만일 적의 에너지가 0 이하라면 적기가 제거 되도록 한다.
                    if(e.energy <= 0) {
                        // e.isDead = true; // 적기는 완전히 추락된 이후에 제거되게 한다.
                        e.isFall=true; // 바로 제거되는 대신 적이 추락 상태가 되도록 한다.
                        // 점수 올리기
                        point += e.type == 0 ? 100 : 200;
                    }
                }
            }
        }
    }

    // 적 관련 처리
    public void enemyService(){

        if (count % 5 == 0) {
            // 반복문 돌면서
            for(Enemy tmp:enemyList) {
                // 모든 적의 이미지 인덱스를 1 증가 시킨다.
                tmp.imageIndex++;
                if(tmp.imageIndex==2) { // 만일 존재하지 않은 인덱스라면
                    // 다시 처음으로 돌리기
                    tmp.imageIndex=0;
                }
            }
        }

        postCount++;
        int ranNum = ran.nextInt(100);
        if (ranNum == 10 && postCount > 10) {
            postCount = 0;
            // 임의의 시점에 적 5마리가 만들어 지도록 해보기
            for(int i=0; i<5; i++) {
                Enemy tmp = new Enemy();
                tmp.x = enemyX[i]; // x 좌표는 배열에 미리 준비된 x 좌표
                tmp.y = unitSize/2; // 임시의 y 좌표
                tmp.type = ran.nextInt(2); // 적 type 은 0 또는 1 랜덤 부여
                tmp.energy = tmp.type == 0 ? 50 : 100;
                tmp.size = unitSize; // 적의 초기 크기
                // 만든 적을 적 목록에 담기
                enemyList.add(tmp);
            }
        }

        // 적 움직이기
        for(Enemy tmp:enemyList) {
            // 만일 추락중인 적은
            if(tmp.isFall) {
                // 크기를 줄이고
                tmp.size -= 1;
                // 회전 값을 증가시킨다.
                tmp.angle += 10;
                // 만일 크기가 0보다 작아진다면
                if (tmp.size <= 0) {
                    // 배열에서 제거될 수 있도록 표시한다.
                    tmp.isDead = true;
                }
            }else{
                // 적 y 좌표를 증가시키고
                tmp.y += speedMissile/2;
            }

            // 아래쪽으로 화면을 벗어났다면?
            if(tmp.y > height+unitSize/2) {
                // 배열에서 제거될 수 있도록 표시한다.
                tmp.isDead=true;
            }
        }

        // 적 체크해서 배열에서 삭제할 적 제거하기
        for(int i=enemyList.size()-1; i>0; i--) {
            Enemy tmp = enemyList.get(i);
            if(tmp.isDead) {
                enemyList.remove(i);
            }
        }
    }

    // 미사일 관련 처리
    public void missileService(){
        // 미사일 만들기
        if(count%10 == 0) {
            missList.add(new Missile(dragonX, dragonY));
        }

        // 미사일 움직이기
        for(Missile tmp:missList) {
            tmp.y -= speedMissile;
            // 만일 위쪽 화면으로 벗어났다면?
            if(tmp.y < -missSize/2){
                tmp.isDead=true; // 배열에서 제거될 수 있도록 표시해둔다.
            }
        }

        // 미사일 객체를 모두 체크해서 배열에서 제거할 객체는 제거하기(단, 반복문을 역순으로 돌아야한다)
        for(int i =missList.size()-1; i>=0; i--) {
            // i 번째 Missile 객체를 얻어와서
            Missile tmp = missList.get(i);
            // 만일 제거할 미사일 객체라면?
            if(tmp.isDead) {
                // List 에서 i 번째 아이템을 제거한다.
                missList.remove(i);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 터치한 곳의 좌표를 드래곤의 x 좌표에 반영하기
        dragonX = (int)event.getX();
        return true;
    }

    Handler handler = new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {
            // 화면 갱신하기
            invalidate();
            // handler 객체에 빈 메세지를 20/1000 초(초당 200번) 이후에 다시 보내기
            handler.sendEmptyMessageDelayed(0,20);
        }
    };
}
