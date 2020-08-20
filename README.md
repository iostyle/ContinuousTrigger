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
|getTriggerInstance|通过主键获取实例|
|saveTriggerInstance|缓存实例，无需手动调用，构造时传入主键即可|
|removeTriggerInstance|从缓存中移除实例，可选是否销毁|
|clearTriggers|清空所有缓存实例|

## Version Log
* V 1.0.5
   - 支持缓存，通过主键可在任何位置获取实例进行操作
* V 1.0.4
   - 支持DSL语法
* V 1.0.3 
   - 节点级别添加阻塞模式控制 
   - 移除response方法 降低代码侵入性

## Tip
下面是Kotlin及Java使用时的示例代码，因为模拟了应用场景所以代码量比较多，核心代码只有注册和绑定仅此而已

## Kotlin
```kotlin
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
        //name为可选参数 设置name后通过getTriggerInstance(name)获取实例
        trigger = (ContinuousTrigger.Builder("myTrigger") with t0 with t1 with t2).create()

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

        //在任何位置可以根据名字获取实例
        getTriggerInstance("myTrigger")?.attach("test2", object : Trigger.Strike {
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

## Java
```java
	Trigger t0 = new Trigger();
        t0.setId("test1");
        t0.setTimeout(2000);

        Trigger t1 = new Trigger();
        t1.setId("test2");
        t1.setChokeMode(true);

        Trigger t2 = new Trigger();
        t2.setId("test3");
        t2.setTimeout(2000);

        //name为可选参数 用于在任意位置获取trigger实例
        trigger = new ContinuousTrigger.Builder("myTrigger")
                .with(t0)
                .with(t1)
                .with(t2)
                .create();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (trigger != null)
                            trigger.attach("test1", new Trigger.Strike() {
                                @Override
                                public void strike() {
                                    Log.e("trigger", "test1");
                                }
                            });
                    }
                });

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UtilKt.getTriggerInstance("myTrigger").attach("test2", new Trigger.Strike() {
                            @Override
                            public void strike() {
                                Log.e("trigger", "test2");
                                new AlertDialog.Builder(JavaMainActivity.this).setMessage("test2")
                                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                if (trigger != null)
                                                    trigger.next();
                                            }
                                        }).show();
                            }
                        });
                    }
                });

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (trigger != null)
                            trigger.attach("test3", new Trigger.Strike() {
                                @Override
                                public void strike() {
                                    Log.e("trigger", "test3");
                                    UtilKt.removeTriggerInstance("myTrigger");
                                }
                            });
                    }
                });
            }
        }).start();
```
