package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.dto.CommentDTO;
import com.campus.trade.entity.Comment;

public interface CommentService {
    void addComment(Long userId, CommentDTO dto);
    IPage<Comment> getCommentPage(Long productId, int pageNum, int pageSize);
    void toggleLike(Long userId, Long commentId);
    void deleteComment(Long userId, Long commentId, boolean isAdmin);
    void reportComment(Long reporterId, Long commentId, String reason);
    IPage<Comment> getAdminCommentPage(Integer status, int pageNum, int pageSize);
}
