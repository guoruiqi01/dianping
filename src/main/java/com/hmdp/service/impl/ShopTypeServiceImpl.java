package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询所有商铺类别，并排序返回
     * @return
     */
    @Override
    public Result queryShopByType() {
        String key = "cache:type:";
        // 1. 先从redis中查询缓存
        List<String> cacheShopTypeList = stringRedisTemplate.opsForList().range(key, 0, -1);

        // 2. 判断是否存在
        if (cacheShopTypeList != null && !cacheShopTypeList.isEmpty()) {
            List<ShopType> shopTypeList = new ArrayList<>();
            // 3. 存在，直接返回 将redis中存储的类型转换为类实体
            for (String cache : cacheShopTypeList) {
                ShopType shopType = JSONUtil.toBean(cache, ShopType.class);
                shopTypeList.add(shopType);
            }
            return Result.ok(shopTypeList);
        }

        // 4. 不存在，查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        // 5. 查数据库不存在，返回错误
        if (shopTypeList == null || shopTypeList.size() == 0) {
            return Result.fail("店铺类型不存在");
        }

        // 6. 存在，写入redis
        for (ShopType shopType : shopTypeList) {
            String jsonShopType = JSONUtil.toJsonStr(shopType);
            stringRedisTemplate.opsForList().rightPush(key, jsonShopType);
        }

        // 7. 返回
        return Result.ok(shopTypeList);
    }
}
