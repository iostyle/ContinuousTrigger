![](https://github.com/iostyle/ImageRepo/blob/master/icon_continuous_trigger.png)
# ContinuousTrigger [![](https://jitpack.io/v/iostyle/ContinuousTrigger.svg)](https://jitpack.io/#iostyle/ContinuousTrigger)
用于按序执行一系列任务，可随时绑定（如多接口返回），可对每个步骤设置超时及响应时间。 

可以将项目clone下来，里面有demo可以修改及测试，喜欢请Star⭐️

Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
		implementation 'com.github.iostyle:ContinuousTrigger:x.y.z'
	}
  
## Attribute & Method
|attribute|description|
|:--:|--|
|id|唯一标识符，用来注册/绑定触发器|
|timeout|超时时间，当前节点超时时间内没有attach将跳转到下一节点，默认值为-1|
|chokeMode|阻塞模式，开启阻塞模式后需要在**你的业务逻辑**处理完成后手动调用next|

|method|description|
|:--:|--|
|with|Builder模式构造注册|
|create|Builder模式创建实例并初始化|
|register|实例化方式有序注册|
|attach|根据ID绑定触发器|
|next|下一步(阻塞模式下需手动调用)|
|cancel|根据ID取消对应触发器，如果是当前节点则自动执行下一个|
|~~response~~|~~响应并关闭超时线程~~(V1.0.3版本移除)|
|clear|清空|

## Version Log
* V 1.0.3 
   - 节点级别添加阻塞模式控制 
   - 移除response方法 降低代码侵入性
* V 1.0.4
   - 支持DSL语法

## Example
```kotlin
	    /**
         * 链式调用写法
         */
        trigger = ContinuousTrigger.Builder()
            .with(
                Trigger().apply {
                    id = "test1"
                    timeout = 2000
                }
            )
            .with(
                Trigger().apply {
                    id = "test2"
                    // 应用于dialog的阻塞模式
                    chokeMode = true
                }
            )
            .with(
                Trigger().apply {
                    id = "test3"
                    timeout = 2000
                }
            )
            .create()

        /**
         * DSL写法
         */
        val t0 = Trigger().apply {
            id = "test1"
            timeout = 2000
        }
        val t1 = Trigger().apply {
            id = "test2"
            // 应用于dialog的阻塞模式
            chokeMode = true
        }
        val t2 = Trigger().apply {
            id = "test3"
            timeout = 2000
        }
        
        trigger = (ContinuousTrigger.Builder() with t0 with t1 with t2).create()

        GlobalScope.launch {
            delay(1500)
            withContext(Dispatchers.Main) {
                trigger?.attach("test1", object : Trigger.Strike {
                    override fun strike() {
                        Log.e("trigger", "test1")
                    }
                })
            }
        }

        trigger?.attach("test2", object : Trigger.Strike {
            override fun strike() {
                Log.e("trigger", "test2")
                AlertDialog.Builder(this@MainActivity).setMessage("test2")
                    .setOnDismissListener {
                        trigger?.next()
                    }.show()
            }
        })

        GlobalScope.launch {
            delay(6000)
            withContext(Dispatchers.Main) {
                trigger?.attach("test3", object : Trigger.Strike {
                    override fun strike() {
                        Log.e("trigger", "test3")
                    }
                })
            }
        }
```
