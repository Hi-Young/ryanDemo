package com.geektime.concurrent.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @description:模拟redis缓存过期后失效
 */
public class CacheTimeOutDelayedQueueTest {

    volatile static Map redis = new ConcurrentHashMap();

    public static void main(String[] args) {
        DelayQueue<CacheObject> delayQueue = new DelayQueue<CacheObject>();
        CacheObject cache = new CacheObject("缓存0",10000);
        redis.put("缓存0","a");
        delayQueue.put(cache);

        CacheObject cache1 = new CacheObject("缓存1",5000);
        delayQueue.put(cache1);
        redis.put("缓存1","b");

        CacheObject cache2 = new CacheObject("缓存2",8000);
        delayQueue.put(cache2);
        redis.put("缓存2","c");

        System.out.println("message:--->入队完毕");


        Thread thread = new Thread(()-> {
            while (redis.size() != 0){
                for (int i = 0; i < 3; i++) {
                    String o = (String) redis.get("缓存" + i);
                    System.out.println("缓存"+i+"->从缓存中获取到"+o);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        while( delayQueue.size() > 0 ){
            try {
                CacheObject cache0 = null;
                cache0 = delayQueue.take();
                System.out.println("缓存:"+cache0.getKey()+"过期,清除缓存");
                redis.remove(cache0.getKey());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

     static class CacheObject implements Delayed {
        //延迟时间
        private final long delay;
        //到期时间
        private final long expire;
        //缓存key
        private final String key;
        //创建时间
        private final long now;

        public long getDelay() {
            return delay;
        }

        public long getExpire() {
            return expire;
        }

        public String getKey() {
            return key;
        }

        public long getNow() {
            return now;
        }

        /**
         * @param key 缓存的key
         * @param delay 延期时间
         */
        public CacheObject(String key , long delay) {
            this.delay = delay;
            this.key = key;
            expire = System.currentTimeMillis() + delay;    //到期时间 = 当前时间+延迟时间
            now = System.currentTimeMillis();
        }

        /**
         * @param msg
         */
        public CacheObject(String msg){
            this(msg,1000);
        }

        public CacheObject(){
            this(null,1000);
        }

        /**
         * 获得延迟时间   用过期时间-当前时间,时间单位毫秒
         * @param unit
         * @return
         */
        public long getDelay(TimeUnit unit) {
            return unit.convert(this.expire
                    - System.currentTimeMillis() , TimeUnit.MILLISECONDS);
        }

        /**
         * 用于延迟队列内部比较排序  当前时间的延迟时间 - 比较对象的延迟时间
         * 越早过期的时间在队列中越靠前
         * @param delayed
         * @return
         */
        public int compareTo(Delayed delayed) {
            return (int) (this.getDelay(TimeUnit.MILLISECONDS)
                    - delayed.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public String toString() {
            return "MovieTiket{" +
                    "delay=" + delay +
                    ", expire=" + expire +
                    ", msg='" + key + '\'' +
                    ", now=" + now +
                    '}';
        }
    }


}

