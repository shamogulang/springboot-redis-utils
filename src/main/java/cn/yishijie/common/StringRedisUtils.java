package cn.yishijie.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjianhui on 2020/01/19
 */
@Component
public class StringRedisUtils {

    Logger logger = LoggerFactory.getLogger(StringRedisUtils.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置string类型的缓存
     * @param key
     * @param value
     * @param timeout
     * @param timeUnit
     */
    public void setString(String key, String value, long timeout, TimeUnit timeUnit){
        try {
            if(timeout <= 0 || timeUnit == null){
                stringRedisTemplate.boundValueOps(key).set(value);
            }else {
                stringRedisTemplate.boundValueOps(key).set(value,timeout,timeUnit);
            }
        }catch (Exception e){
            logger.error("StringRedisUtils setString error,for details:",e);
        }
    }

    /**
     * 获取string类型的缓存
     * @param key
     * @return
     */
    public String getString(String key){
        String result = null;
        try {
            result = stringRedisTemplate.boundValueOps(key).get();
        }catch (Exception e){
            logger.error("StringRedisUtils getString error,for details:",e);
        }
        return result;
    }

    /**
     * 批量删除整体缓存（不区分类型）
     * @param keys
     */
    public void delete(Collection<String> keys){
        try {
            stringRedisTemplate.delete(keys);
        }catch (Exception e){
            logger.error("StringRedisUtils delete error,for details:",e);
        }
    }

    /**
     * 增加缓存中的值
     * @param key
     */
    public Long increaseString(String key,long delta){
        Long result = null;
        try {
            result = stringRedisTemplate.boundValueOps(key).increment(delta);
        } catch (Exception e) {
            logger.error("increaseString delString error,for details:",e);
        }
        return result;
    }

    /**
     * 批量设置hash结构
     * @param key
     * @param tuple
     * @param timeOut
     * @param timeUnit
     */
    public void multiSetHash(String key, Map<String,String> tuple,long timeOut,TimeUnit timeUnit){
        try {
            stringRedisTemplate.boundHashOps(key).putAll(tuple);
            if(timeOut > 0 && timeUnit != null){
                stringRedisTemplate.expire(key,timeOut,timeUnit);
            }
        }catch (Exception e){
            logger.error("multiSetHash error,for details:",e);
        }
    }

    /**
     * 批量删除hash子key对应的记录
     * @param key
     * @param subKeys
     */
    public void mutilDelHash(String key, Collection<String> subKeys){
        try {
            stringRedisTemplate.boundHashOps(key).delete(subKeys.toArray(new String[0]));
        }catch (Exception e){
            logger.error("mutilDelHash error,for details：",e);
        }
    }

    /**
     * 批量获取hash里的值（按照顺序返回，子key没有的记录返回null）
     * @param key
     * @param subKeys
     * @return
     */
    public List<Object> mutilGetHash(String key, List<Object> subKeys){
        List<Object> results = null;
        try {
            results = stringRedisTemplate.boundHashOps(key).multiGet(subKeys);
        }catch (Exception e){
            logger.error("mutilGetHash error,for details:",e);
        }
        return results;
    }
}
