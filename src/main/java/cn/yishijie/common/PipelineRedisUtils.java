package cn.yishijie.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author chenjianhui on 2020/01/19
 */
@Component
public class PipelineRedisUtils {

    private Logger logger = LoggerFactory.getLogger(PipelineRedisUtils.class);
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 批量increase MapCnt的数据
     * @param key
     * @param cntDeltas
     * @return
     */
    public List<Long> multiIncHash(String key, Map<String,Long> cntDeltas){
        List<Long> resultCnt = null;
        try {
            resultCnt =  redisTemplate.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    for(String subKey : cntDeltas.keySet()){
                        Long value = cntDeltas.get(subKey);
                        redisConnection.hIncrBy(key.getBytes(),subKey.getBytes(),value.longValue());
                    }
                    return null;
                }
            },redisTemplate.getStringSerializer());
        }catch (Exception e){
            logger.error("multiIncHash error,for details:",e);
        }
        return resultCnt;
    }

    /**
     * 批量判断key是否存在
     * @param keys
     * @return
     */
    public List<Boolean>  multiExist(List<String> keys){
        List<Boolean> booleanList = null;
        try {
            booleanList = redisTemplate.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    for(String key : keys){
                        redisConnection.exists(key.getBytes());
                    }
                    return null;
                }
            },redisTemplate.getStringSerializer());
        }catch (Exception e){
            logger.error("multiExist error,for details:",e);
        }
        return booleanList;
    }

    public List<Double>  getMultiZsetScores(String key,List<String> subKeys){
        try {
          return   redisTemplate.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    for(String subKey : subKeys){
                        redisConnection.zScore(key.getBytes(),subKey.getBytes());
                    }
                    return null;
                }
            },redisTemplate.getStringSerializer());
        }catch (Exception e){
            logger.error("getMultiZsetScores error,for details:",e);
        }
        return null;
    }
}
