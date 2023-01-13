package com.example.step05example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView

/*
    extends AppCompatActivity 상속 => :AppCompatActivity
    implements View.OnClickListener 인터페이스 구현 => , View.OnClickListener
 */

class MainActivity2 : AppCompatActivity(), View.OnClickListener{
    // 필드
    // null 로 초기화 하기 위해서는 ? 가 필요하다
    var editText:EditText?=null
    var names:MutableList<String>?=null
    var adapter:ArrayAdapter<String>?=null
    // 위의 선언이 불편하다면 아래와 같이 뒤늦은 초기화도 가능하다
    lateinit var listView:ListView // 참조 값을 나중에 넣고싶으면 lateinit 예약어를 사용하면 된다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        editText = findViewById(R.id.editText)
        // findViewById<UI 의 type>
        val addBtn = findViewById<Button>(R.id.addBtn)
        addBtn.setOnClickListener(this)

        names= mutableListOf()

        adapter=ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                names!!) // (type) names => names as type
                         // names 에는 null 이 들어갈 수 없는데 필드에서 null 을 허용했기때문에  !! 라는 (not null) 을 추가해야한다
                         // 혹은 필드를 lateinit 으로 변환하면 !! 를 빼고 사용이 가능하다.
        /*
            [ in Java ]
            .setXXX( value )
            [ in kotlin ]
            .xxx = value 라는 형태가 많다.
         */
        listView.adapter=adapter

    }

    override fun onClick(p0: View?) {
        // editText?.text 는 editText 안에 값이 null 이 아니면 .text 를 참조하겠다는 의미
        val inputMsg:String = editText?.text.toString()
        names?.add(inputMsg)
        adapter?.notifyDataSetChanged()
        editText?.setText("")
    }
}