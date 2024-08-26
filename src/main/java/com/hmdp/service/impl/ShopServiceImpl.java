package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TTL;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    /**
     * 根据id查询商铺信息
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @Override
    public Result querById(Long id) {
        // 缓存穿透
        // return Result.ok(queryWithPassThrough(id));
//        Shop shop = cacheClient.
//                queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, shopId -> getById(shopId), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 互斥锁缓存击穿
        // Shop shop = queryWithMutex(id);

        // 逻辑过期解决缓存击穿
        // Shop shop = queryWithLogicalExpire(id);

        // 工具类缓存击穿
        Shop shop = cacheClient.
                queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, shopId -> getById(shopId), 20L, TimeUnit.SECONDS);

        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        return Result.ok(shop);
    }

    private final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 逻辑过期解决缓存击穿
     * @param id
     * @return
     */
//    public Shop queryWithLogicalExpire(Long id) {
//        String key = CACHE_SHOP_KEY + id;
//        // 1. 尝试从redis中查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2. 判断缓存是否存在
//        if (StrUtil.isBlank(shopJson)) {
//            // 3. 不存在，直接返回，逻辑过期一定存在
//            return null;
//        }
//        // 4. 命中，需要先把json反序列化为对象
//        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
//        JSONObject data = (JSONObject) redisData.getData(); // {"": "", "":""}
//        Shop shop = JSONUtil.toBean(data, Shop.class);
//        LocalDateTime expireTime = redisData.getExpireTime();
//
//
//        // 5. 判断是否过期
//        if (expireTime.isAfter(LocalDateTime.now())) {
//            // 5.1 未过期，直接返回店铺信息
//            return shop;
//        }
//
//        // 5.2 已过期，需要缓存重建
//
//        // 6. 缓存重建
//        // 6.1 获取互斥锁
//        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
//        boolean isLock = tryLock(lockKey);
//        // 6.2 判断是否获取锁成功
//        if (isLock) {
//            // 6.3 成功，开启独立线程，实现缓存重建
//            CACHE_REBUILD_EXECUTOR.submit(() -> {
//                try {
//                    // 重建缓存
//                    this.saveShop2Redis(id, 20L);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    // 释放锁
//                    unLock(lockKey);
//                }
//            });
//        }
//        // 6.4 返回过期的商铺信息
//        return shop;
//    }

    /**
     * 互斥锁缓存击穿
     * @param id
     * @return
     */
//    public Shop queryWithMutex(Long id) {
//        String key = CACHE_SHOP_KEY + id;
//        // 1. 尝试从redis中查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2. 判断缓存是否存在
//        if (StrUtil.isNotBlank(shopJson)) {
//            // 3. 存在，直接返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//
//        // 判断命中的是否是空值
//        if (shopJson != null) {
//            return null;
//        }
//
//        // 4. 实现缓存重建
//        // 4.1 获取互斥锁
//        String lockKey = "lock:shop:" + id;
//        Shop shop = null;
//        try {
//            boolean isLock = tryLock(lockKey);
//            // 4.2 判断是否获取成功
//            if (!isLock) {
//                // 4.3 如果失败，则休眠并重试
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//            // 4.4 如果成功，根据id查询数据库
//            shop = getById(id);
//            // 模拟重建缓存延迟
//            Thread.sleep(200);
//
//            // 5. 不存在，返回错误
//            if (shop == null) {
//                // 将空值写入缓存
//                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return shop;
//            }
//            // 6. 存在，写入redis
//            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            // 7. 释放互斥锁
//            unLock(lockKey);
//        }
//
//        // 8. 返回
//        return shop;
//    }

    /**
     * 缓存穿透解决方案
     * @param id
     * @return
     */
//    public Shop queryWithPassThrough(Long id) {
//        String key = CACHE_SHOP_KEY + id;
//        // 1. 尝试从redis中查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2. 判断缓存是否存在
//        if (StrUtil.isNotBlank(shopJson)) {
//            // 3. 存在，直接返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//
//        // 判断命中的是否是空值
//        if (shopJson != null) {
//            return null;
//        }
//
//        // 4. 缓存不存在，根据id查询数据库
//        Shop shop = getById(id);
//
//        // 5. 不存在，返回错误
//        if (shop == null) {
//            // 将空值写入缓存
//            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//            return shop;
//        }
//        // 6. 存在，写入redis
//        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        // 7. 返回
//        return shop;
//    }

    /**
     * 获取redis锁
     * @param key
     * @return
     */
//    private boolean tryLock(String key) {
//        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
//        return BooleanUtil.isTrue(flag);
//    }

    /**
     * 释放redis锁
     * @param key
     * @return
     */
//    private void unLock(String key) {
//        stringRedisTemplate.delete(key);
//    }

    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
         // 1. 查询店铺数据
        Shop shop = getById(id);
        Thread.sleep(200); // 模拟耗时的缓存重建
        // 2. 封装成逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 3. 写入Redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id,
                JSONUtil.toJsonStr(redisData));
    }

    /**
     * 更新商铺信息
     * @param shop
     * @return
     */
    @Override
    public Result update(Shop shop) {
        if (shop.getId() == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1. 更新数据库
        updateById(shop);

        // 2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());

        return Result.ok();
    }
}
