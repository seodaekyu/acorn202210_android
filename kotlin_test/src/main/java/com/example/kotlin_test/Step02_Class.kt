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

/*
    Java 에서 아래와 같은 모양의 클래스를 kotlin 에서 코딩하면 ...

    class Person{
        public String name;
        public Person(String name){
            this.name=name;
        }
    }
 */

class Person constructor(name:String){ // 클래스명 옆에 선언하는 생성자를 primary 생성자라고 한다.
    // 필드 선언
    var name:String

    init{
        this.name=name
    }
}

// 위의 클래스를 조금 간단히 선언하면 아래와 같다
class Person2(var name:String)
// var or val 을 생성자의 인자에 선언하면 전달받은 값이 자동으로 같은 이름의 필드가 만들어지고 값이 들어간다

fun main() {
    // 클래스를 이용해서 객체 생성 in java => new MyCar( )
    var c1 = MyCar()
    var c2 = YourCar()
    c2.drive()

    AirPlane()

    var p1 = Person("김구라")
    println(p1.name)

    val p2 = Person2("해골")
    println(p2.name)
}