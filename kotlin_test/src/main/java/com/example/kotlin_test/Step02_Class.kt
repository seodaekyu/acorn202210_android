package com.example.kotlin_test

// 클래스 정의하기
class MyCar

class YourCar{
    // 멤버 함수
    fun drive(){
        println("달려요!")
    }
}
// 대표(primary) 생성자는 클래스명 우측에 선언한다.
// 생성자의 모양은 constructor 에서 정하고 예약어 생략도 가능하다
class AirPlane constructor(){
    // 생성자가 있을까?
    init{ // 생성자의 내용은 여기서
        println("AirPlane 클래스의 init!")
    }
}

// 생성자의 모양은 constructor 에서 정하고 예약어 생략도 가능하다
class AirPlane2(){
    // 생성자가 있을까?
    init{ // 생성자의 내용은 여기서
        println("AirPlane 클래스의 init!")
    }
}

// 인자로 전달 받을게 없다면 ( ) 생략가능 AirPlane 1, 2, 3 비교
class AirPlane3{
    // 생성자가 있을까?
    init{ // 생성자의 내용은 여기서
        println("AirPlane 클래스의 init!")
    }
}

fun main() {
    // 클래스를 이용해서 객체 생성 in java => new MyCar( )
    var c1 = MyCar()
    var c2 = YourCar()
    c2.drive()

    AirPlane()
}