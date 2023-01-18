package com.example.kotlin_test

// 함수 type 을 매개변수로 전달 받는 함수
fun useFunc(f:()->Unit){
    // 인자로 전달 받은 함수 호출하기
    f()
}

// 인터페이스 정의하기
interface Drill{
    fun hole()
}

fun useDrill(d:Drill){
    d.hole()
}

fun main() {
    // 익명클래스를 사용하여 인터페이스의 참조 값을 사용하여 코드를 만들 수 있다.
    useDrill(object:Drill{
        override fun hole() {
            println("구멍을 뚫어요")
        }

    })

    // 원래 모양
    useFunc(fun(){
        println("익명함수 호출됨! 1")
    })

    // fun() 생략한 모양
    useFunc({
        println("익명함수 호출됨! 2")
    })

    // 위의 코드를 더 간단히 한 최종 모양
    useFunc {
        println("익명함수 호출됨! 3")
    }

}