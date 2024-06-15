package com.example.delayqueue.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.delayqueue.bean.Task;
import com.example.delayqueue.common.Constance;
import com.example.delayqueue.service.DelayTaskService;
import com.google.gson.Gson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 定时同步任务
 */
@Component
public class TransferJob {

    private static final Logger logger = LoggerFactory.getLogger(TransferJob.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private DelayTaskService delayTaskService;


    /**
     * 每分钟将任务从zSet移动到list中
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void zSet2List() {
        // 使用分布式锁避免任务重复执行
        String lockKey = Constance.APP_NAME + ":lock:zSet2List";
        final RLock lock = redissonClient.getLock(lockKey);
        final boolean tryLock = lock.tryLock();
        if(!tryLock) {
            // 没有获取到锁，无需同步任务
            logger.info("cant obtain zSet2List lock, key:{}", lockKey);
            return;
        }
        try {
            Gson gson = new Gson();
            // 获取所有未来任务的key
            final Set<String> keys = scan(Constance.APP_NAME + ":" + Constance.FUTURE + "*");
            for(String key : keys) {
                // 拿到zSet中需要执行的任务并删除
                final long current = System.currentTimeMillis();
                final Set<String> tasks = redisTemplate.opsForZSet().rangeByScore(key, 0, current);
                redisTemplate.opsForZSet().removeRangeByScore(key, 0, current);
                if(tasks == null) {
                    return;
                }
                // 将tasks移动到list中
                redisTemplate.executePipelined(new RedisCallback<Object>() { // 为了提升性能，使用pipeline
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    @Override
                    public Object doInRedis(RedisConnection connection) throws DataAccessException {
                        tasks.forEach(task -> {
                            final Task taskInstance = gson.fromJson(task, Task.class);
                            connection.lPush(serializer.serialize(Constance.APP_NAME + ":" + Constance.CURRENT + ":" + taskInstance.getTaskOwner()), serializer.serialize(task));
                        });
                        return null;
                    }
                });
                logger.info("transfer task from zSet to list, tasks:{}", tasks);
            }
        }finally {
            lock.unlock();
        }
    }


    /**
     * 将数据库中的任务每5min定时同步到redis中
     */
    @PostConstruct // 服务启动时就立即执行此方法
    @Scheduled(cron = "0 */5 * * * ?")
    public void db2Redis() {
        // 使用分布式锁避免任务重复执行
        String lockKey = Constance.APP_NAME + ":lock:db2Redis";
        final RLock lock = redissonClient.getLock(lockKey);
        final boolean tryLock = lock.tryLock();
        if(!tryLock) {
            logger.info("cant obtain db2Redis lock, key:{}", lockKey);
            return;
        }
        try {
            // 清理redis中所有任务
            final Set<String> futureKeys = scan(Constance.APP_NAME + ":" + Constance.FUTURE + "*");
            redisTemplate.delete(futureKeys);
            logger.info("delete task from redis zSet, keys: {}",futureKeys);
            final Set<String> currentKeys = scan(Constance.APP_NAME + ":" + Constance.CURRENT + "*");
            redisTemplate.delete(currentKeys);
            logger.info("delete task from redis list, keys: {}", currentKeys);
            // 查询数据库中未来5分组将要执行的任务
            final Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 5);
            final List<Task> tasks = delayTaskService.list(Wrappers.<Task>lambdaQuery().lt(Task::getExecuteTime, calendar.getTimeInMillis()));
            // 将任务添加到redis中
            if(tasks == null) {
                return;
            }
            tasks.forEach(task -> {
                delayTaskService.addTask2Redis(task);
            });
            logger.info("transfer task from db to redis, tasks:{}", tasks);
        }finally {
            lock.unlock();
        }
    }


    /**
     * 扫描key
     * @param pattern
     * @return
     */
    public Set<String> scan(String pattern) {
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisConnection connection) -> {
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().count(Long.MAX_VALUE).match(pattern).build())) {
                while(cursor.hasNext()) {
                    final String key = new String(cursor.next(), StandardCharsets.UTF_8);
                    keys.add(key);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        return keys;
    }
}
