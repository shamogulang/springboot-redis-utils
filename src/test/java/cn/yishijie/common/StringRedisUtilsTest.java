package cn.yishijie.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class StringRedisUtilsTest {

    @Autowired
    private StringRedisUtils stringRedisUtils;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    public String getTodayYyyyMmDd(){
        LocalDate now = LocalDate.now();
        DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
        return yyyyMMdd.format(now);
    }

    public String getYyyyMm(){
        LocalDate now = LocalDate.now();
        DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMM");
        return yyyyMMdd.format(now);
    }

    public String getYyyy(){
        LocalDate now = LocalDate.now();
        DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyy");
        return yyyyMMdd.format(now);
    }

    public int weekNum(){
        LocalDate now = LocalDate.now();
        System.out.println(now.toString());
        int i = now.get(WeekFields.of(DayOfWeek.MONDAY,1).weekOfYear());
        System.out.println(i);
        return i;
    }


    @Test
    public void testRedission(){
        RMap<String, Boolean> redission = redissonClient.getMap("memory:rule:last:year:today:"+getTodayYyyyMmDd());
        redission.expire(2, TimeUnit.DAYS);
        Boolean s = redission.putIfAbsent("456", true);
        System.out.println(s);

        RMap<String, Boolean> map = redissonClient.getMap("memory:rule:month:" + getYyyyMm());
        boolean expire = map.expire(153, TimeUnit.DAYS);
        Boolean aBoolean = map.putIfAbsent("456", true);
        System.out.println(aBoolean);


        RMap<String, Boolean> map1 = redissonClient.getMap("memory:rule:week:" + getYyyy() + ":" + weekNum());
        map1.expire(153, TimeUnit.DAYS);
        Boolean o = map1.putIfAbsent("456", true);
        System.out.println(o);

        RMap<String, Boolean> map2 = redissonClient.getMap("memory:rule:weekend:" + getYyyy() + ":" + weekNum());
        map2.expire(153, TimeUnit.DAYS);
        Boolean o1 = map2.putIfAbsent("456", true);
        System.out.println(o1);
    }

    @Test
    public void setString() {
        String key = "setString-key";
        String value = "setString-value";
        stringRedisUtils.setString(key,value,0,null);

        String result = stringRedisTemplate.boundValueOps(key).get();

        //删除测试的key
        stringRedisTemplate.delete(key);
        //断言，如果不相等，提示错误
        Assert.assertEquals(value,result);
    }

    @Test
    public void getString() {
        String key = "getString-key";
        String value = "getString-value";
        stringRedisTemplate.boundValueOps(key).set(value);

        String result = stringRedisUtils.getString(key);
        //删除测试的key
        stringRedisTemplate.delete(key);
        //断言，如果不想等，提示错误
        Assert.assertEquals(value,result);
    }

    @Test
    public void delete() {
        String key = "delete-key";
        String value = "delete-value";
        stringRedisTemplate.boundValueOps(key).set(value);

        stringRedisUtils.delete(Arrays.asList(key));

        String result = stringRedisTemplate.boundValueOps(key).get();

        Assert.assertNull(result);
    }

    @Test
    public void increaseString() {
        String key = "increaseString-key";

        stringRedisTemplate.boundValueOps(key).set("1");

        Long result = stringRedisUtils.increaseString(key,10L);

        stringRedisTemplate.delete(key);

        Assert.assertEquals(11L,result.longValue());


    }

    @Test
    public void multiSetHash() {
        String key = "multiSetHash-key";

        String key1 = "multiSetHash-key1";
        String key2 = "multiSetHash-key2";
        String key3 = "multiSetHash-key3";

        String value1 = "multiSetHash-value1";
        String value2 = "multiSetHash-value2";
        String value3 = "multiSetHash-value3";

        Map<String,String> tuple = new HashMap<>();
        tuple.put(key1,value1);
        tuple.put(key2,value2);
        tuple.put(key3,value3);

        stringRedisUtils.multiSetHash(key,tuple,0,null);

        List<Object> resultObj = stringRedisTemplate.boundHashOps(key).multiGet(Arrays.asList(key1,key2,key3));

        stringRedisTemplate.delete(key);
        Assert.assertNotNull(resultObj);

        for(int i = 0;i < resultObj.size();i++){
            Assert.assertEquals("multiSetHash-value"+(i+1),String.valueOf(resultObj.get(i)));
        }
    }

    @Test
    public void mutilDelHash() {
        String key = "mutilDelHash-key";

        String key1 = "mutilDelHash-key1";
        String key2 = "mutilDelHash-key2";
        String key3 = "mutilDelHash-key3";

        String value1 = "mutilDelHash-value1";
        String value2 = "mutilDelHash-value2";
        String value3 = "mutilDelHash-value3";

        Map<String,String> tuple = new HashMap<>();
        tuple.put(key1,value1);
        tuple.put(key2,value2);
        tuple.put(key3,value3);

        stringRedisTemplate.boundHashOps(key).putAll(tuple);

        stringRedisUtils.mutilDelHash(key,Arrays.asList(key1,key2));

        List<Object> resultObj = stringRedisTemplate.boundHashOps(key).multiGet(Arrays.asList(key1,key2));

        stringRedisTemplate.delete(key);
        Assert.assertNotNull(resultObj);

        for(int i = 0;i < resultObj.size();i++){
            Assert.assertNull(resultObj.get(i));
        }
    }

    @Test
    public void mutilGetHash() {
        String key = "mutilGetHash-key";

        String key1 = "mutilGetHash-key1";
        String key2 = "mutilGetHash-key2";
        String key3 = "mutilGetHash-key3";

        String value1 = "mutilGetHash-value1";
        String value2 = "mutilGetHash-value2";
        String value3 = "mutilGetHash-value3";

        Map<String,String> tuple = new HashMap<>();
        tuple.put(key1,value1);
        tuple.put(key2,value2);
        tuple.put(key3,value3);

        stringRedisUtils.multiSetHash(key,tuple,0,null);

        List<Object> resultObj = stringRedisUtils.mutilGetHash(key,Arrays.asList(key1,key2,key3));

        stringRedisTemplate.delete(key);

        for(int i = 0;i < resultObj.size();i++){
            Assert.assertEquals(String.valueOf(resultObj.get(i)), "mutilGetHash-value"+(i+1));
        }
    }
}