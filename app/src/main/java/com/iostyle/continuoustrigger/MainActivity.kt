package com.iostyle.continuoustrigger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.iostyle.trigger.ContinuousTrigger
import com.iostyle.trigger.Trigger
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private var trigger: ContinuousTrigger? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        //可是使用Builder 也可以直接实例化
//        trigger = ContinuousTrigger.Builder()
//                .with(
//                        Trigger().also {
//                            it.id = "test1"
//                            it.timeout = 2000
//                        }
//                )
//                .with(
//                        Trigger().also {
//                            it.id = "test2"
//                            it.timeout = 2000
//                        }
//                )
//                .with(
//                        Trigger().also { it.id = "test3" }
//                ).create()

        trigger = ContinuousTrigger()
                .register(
                        Trigger().also {
                            it.id = "test1"
                            it.timeout = 2000
                        })
                .register(
                        Trigger().also {
                            it.id = "test2"
                            it.timeout = 2000
                        })
                .register(
                        Trigger().also {
                            it.id = "test3"
                        })

        GlobalScope.launch {
//            delay(2500)
            withContext(Dispatchers.Main) {
                trigger?.attach("test1", object : Trigger.Strike {
                    override fun strike() {
                        trigger?.response()
                        AlertDialog.Builder(this@MainActivity).setMessage("test1").setOnDismissListener {
                            trigger?.next()
                        }.show()
                    }
                })
            }
        }


        GlobalScope.launch {
            delay(6000)
            withContext(Dispatchers.Main) {
                trigger?.attach("test2", object : Trigger.Strike {
                    override fun strike() {
                        trigger?.response()
                        AlertDialog.Builder(this@MainActivity).setMessage("test2").setOnDismissListener {
                            trigger?.next()
                        }.show()
                    }
                })
            }
        }

        trigger?.attach("test3", object : Trigger.Strike {
            override fun strike() {
                trigger?.response()
                AlertDialog.Builder(this@MainActivity).setMessage("test3").setOnDismissListener {
                    trigger?.next()
                }.show()
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        trigger?.clear()
    }
}
