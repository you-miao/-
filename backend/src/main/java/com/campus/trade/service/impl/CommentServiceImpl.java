package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.CommentDTO;
import com.campus.trade.entity.Comment;
import com.campus.trade.entity.CommentLike;
import com.campus.trade.entity.CommentReport;
import com.campus.trade.mapper.CommentLikeMapper;
import com.campus.trade.mapper.CommentMapper;
import com.campus.trade.mapper.CommentReportMapper;
import com.campus.trade.service.CommentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private CommentLikeMapper likeMapper;
    @Resource
    private CommentReportMapper reportMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void addComment(Long userId, CommentDTO dto) {
        Comment comment = new Comment();
        comment.setProductId(dto.getProductId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setRating(dto.getRating());
        comment.setStatus(0);
        if (dto.getImages() != null) {
            try {
                comment.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException ignored) {}
        }
        commentMapper.insert(comment);
    }

    @Override
    public IPage<Comment> getCommentPage(Long productId, int pageNum, int pageSize) {
        IPage<Comment> page = commentMapper.selectCommentPage(
                new Page<>(pageNum, pageSize), productId, 1);
        for (Comment c : page.getRecords()) {
            List<Comment> replies = commentMapper.selectReplies(c.getId());
            c.setReplies(replies);
        }
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long userId, Long commentId) {
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, userId)
                .eq(CommentLike::getCommentId, commentId);
        Long count = likeMapper.selectCount(wrapper);
        if (count > 0) {
            likeMapper.delete(wrapper);
            commentMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Comment>()
                            .eq(Comment::getId, commentId)
                            .setSql("like_count = like_count - 1"));
        } else {
            CommentLike like = new CommentLike();
            like.setUserId(userId);
            like.setCommentId(commentId);
            likeMapper.insert(like);
            commentMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Comment>()
                            .eq(Comment::getId, commentId)
                            .setSql("like_count = like_count + 1"));
        }
    }

    @Override
    public void deleteComment(Long userId, Long commentId, boolean isAdmin) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }
        commentMapper.deleteById(commentId);
    }

    @Override
    public void reportComment(Long reporterId, Long commentId, String reason) {
        CommentReport report = new CommentReport();
        report.setCommentId(commentId);
        report.setReporterId(reporterId);
        report.setReason(reason);
        report.setStatus(0);
        reportMapper.insert(report);

        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setStatus(3);
            commentMapper.updateById(comment);
        }
    }

    @Override
    public IPage<Comment> getAdminCommentPage(Integer status, int pageNum, int pageSize) {
        return commentMapper.selectCommentPage(new Page<>(pageNum, pageSize), null, status);
    }
}
