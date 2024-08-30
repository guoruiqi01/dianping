package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    /**
     * 查询hot
     * @param current
     * @return
     */
    Result queryHotBlog(Integer current);

    /**
     * 根据id查询blog
     * @param id
     * @return
     */
    Result queryBlogById(Long id);

    /**
     * 利用redis实现点赞功能
     * @param id
     * @return
     */
    Result likeBlog(Long id);

    /**
     * 根据blogid查询点赞的用户列表
     * @param id
     * @return
     */
    Result queryBlogLikes(Long id);
}
