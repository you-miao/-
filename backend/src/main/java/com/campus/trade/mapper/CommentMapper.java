package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper extends BaseMapper<Comment> {

    IPage<Comment> selectCommentPage(Page<Comment> page,
                                      @Param("productId") Long productId,
                                      @Param("status") Integer status);

    List<Comment> selectReplies(@Param("parentId") Long parentId);
}
