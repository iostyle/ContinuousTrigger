package com.iostyle.continuoustrigger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
        // 应用于dialog的阻塞模式
        trigger = ContinuousTrigger.Builder()
            .with(
                Trigger().also {
                    it.id = "test1"
                    it.timeout = 2000
                }
            )
            .with(
                Trigger().also {
                    it.id = "test2"
                    it.timeout = 2000
                }
            )
            .with(
                Trigger().also { it.id = "test3" }
            )
            .openChokeMode().create()

        GlobalScope.launch {
            delay(1500)
            withContext(Dispatchers.Main) {
                trigger?.attach("test1", object : Trigger.Strike {
                    override fun strike() {
                        trigger?.response()
                        AlertDialog.Builder(this@MainActivity).setMessage("test1")
                            .setOnDismissListener {
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
                        AlertDialog.Builder(this@MainActivity).setMessage("test2")
                            .setOnDismissListener {
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

        // 不需要阻塞的全自动模式
        val logTrigger = ContinuousTrigger.Builder()
            .with(
                Trigger().also {
                    it.id = "log1"
                    it.timeout = 2000
                }
            )
            .with(
                Trigger().also {
                    it.id = "log2"
                    it.timeout = 2000
                }
            )
            .with(
                Trigger().also { it.id = "log3" }
            )
            .create()

        logTrigger.attach("log3", object : Trigger.Strike {
            override fun strike() {
                Log.e("trigger","log3")
            }
        })

        GlobalScope.launch {
            delay(1500)
            logTrigger.attach("log1", object : Trigger.Strike {
                override fun strike() {
                    Log.e("trigger","log1")
                }
            })
        }

        GlobalScope.launch {
            delay(6000)
            logTrigger.attach("log2", object : Trigger.Strike {
                override fun strike() {
                    Log.e("trigger","log2")
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        trigger?.clear()
    }
}
