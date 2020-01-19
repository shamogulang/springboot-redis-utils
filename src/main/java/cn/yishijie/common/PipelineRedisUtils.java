package cn.yishijie.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 批量获取分数
     * @param key
     * @param subKeys
     * @return
     */
    public List<Double>  getMultiZSetScores(String key,List<String> subKeys){
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

    /**
     * 倒序获取不同分数段从offset开始，前count记录
     * @param key
     * @param mins
     * @param maxs
     * @param offset
     * @param count
     * @return
     */
    public List<Object>  getZSetRevRangeByScoresWithScores(String key, List<Double> mins, List<Double> maxs, long offset, long count){
        try {
           return redisTemplate.executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                    int index = 0;
                    for(Double min : mins){
                        if(min != null){
                            Double max = maxs.get(index);
                            if(max != null){
                                redisConnection.zRevRangeByScoreWithScores(key.getBytes(),min,max,offset,count);
                            }
                        }
                        index++;
                    }
                    return null;
                }
            });
        }catch (Exception e){
            logger.error("getZSetRevRangeByScoresWithScores error,for details:",e);
        }
        return null;
    }

    /**
     * 批量倒序获取前start 至（end-1）位置的元素
     * 索引从0开始，最后一个元素的索引为end-1
     * @param keys
     * @param start
     * @param end
     * @return
     */
    public List<Set<DefaultTypedTuple<?>>> getZsetRevRange(final List<String> keys, final long start, final long end) {
        try {
            return redisTemplate.executePipelined(new RedisCallback<String>() {
                @Override
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                    for (String key : keys) {
                        if (key != null) {
                            connection.zRevRangeWithScores(key.getBytes(), start, end);
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("getZsetByPipeline failed: " + e.getMessage());
        }
        return null;
    }

}
