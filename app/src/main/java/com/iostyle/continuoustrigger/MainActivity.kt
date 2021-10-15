package com.iostyle.continuoustrigger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.iostyle.trigger.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private var trigger: ContinuousTrigger? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        /**
         * 链式调用写法
         */
//        trigger = ContinuousTrigger.Builder()
//            .with(
//                Trigger().apply {
//                    id = "test1"
//                    timeout = 2000
//                }
//            )
//            .with(
//                Trigger().apply {
//                    id = "test2"
//                    // 应用于dialog的阻塞模式
//                    chokeMode = true
//                }
//            )
//            .with(
//                Trigger().apply {
//                    id = "test3"
//                    timeout = 2000
//                }
//            )
//            .create()

        /**
         * DSL写法
         */
//        val t0 = Trigger().apply {
//            id = "test1"
//            timeout = 2000
//        }
//        val t1 = Trigger().apply {
//            id = "test2"
//            // 应用于dialog的阻塞模式
//            chokeMode = true
//        }
//        val t2 = Trigger().apply {
//            id = "test3"
//            timeout = 2000
//        }
//        //name为可选参数 设置name后通过getTriggerInstance(name)获取实例
//        trigger = (ContinuousTrigger.Builder("myTrigger") with t0 with t1 with t2).create()
//
//        GlobalScope.launch {
//            delay(1500)
//            withContext(Dispatchers.Main) {
//                trigger?.attach("test1", object : Trigger.Strike {
//                    override fun strike() {
//                        Log.e("trigger", "test1")
//                    }
//                })
//            }
//        }
//
//        //在任何位置可以根据名字获取实例
//        getTriggerInstance("myTrigger")?.attach("test2", object : Trigger.Strike {
//            override fun strike() {
//                Log.e("trigger", "test2")
//                AlertDialog.Builder(this@MainActivity).setMessage("test2")
//                    .setOnDismissListener {
//                        trigger?.next()
//                    }.show()
//            }
//        })
//
//        GlobalScope.launch {
//            delay(6000)
//            withContext(Dispatchers.Main) {
//                trigger?.attach("test3", object : Trigger.Strike {
//                    override fun strike() {
//                        Log.e("trigger", "test3")
//                        removeTriggerInstance("myTrigger")
//                    }
//                })
//            }
//        }

        /**
         * 灵活绑定写法
         */
        saveTriggerInstance("adjustTrigger")

        getTriggerInstance("adjustTrigger")?.run {
            adjustAttach("t1", object : Trigger.Strike {
                override fun strike() {
                    Log.e("trigger", "t1")
                    AlertDialog.Builder(this@MainActivity).setMessage("t1")
                        .setOnDismissListener {
                            next()
                        }.show()
                }
            }, true)
        }

        getTriggerInstance("adjustTrigger")?.run {
            adjustAttach(
                "t2", object : Trigger.Strike {
                    override fun strike() {
                        Log.e("trigger", "t2")
                        GlobalScope.launch {
                            delay(1000)
                            next()
                        }
                    }
                }, true
            )
        }

        getTriggerInstance("adjustTrigger")?.adjustAttach(
            "t3",
            object : Trigger.Strike {
                override fun strike() {
                    Log.e("trigger", "t3")
                }
            },
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        trigger?.clear()
        clearTriggers()
    }
}
