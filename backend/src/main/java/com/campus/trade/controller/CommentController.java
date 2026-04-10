package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.common.Result;
import com.campus.trade.dto.CommentDTO;
import com.campus.trade.entity.Comment;
import com.campus.trade.security.SecurityUtil;
import com.campus.trade.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "评论接口")
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @ApiOperation("发表评论")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CommentDTO dto) {
        commentService.addComment(SecurityUtil.getCurrentUserId(), dto);
        return Result.ok("评论已提交，等待审核");
    }

    @ApiOperation("商品评论列表")
    @GetMapping("/list/{productId}")
    public Result<IPage<Comment>> list(@PathVariable Long productId,
                                        @RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(commentService.getCommentPage(productId, pageNum, pageSize));
    }

    @ApiOperation("点赞/取消点赞")
    @PostMapping("/like/{id}")
    public Result<Void> toggleLike(@PathVariable Long id) {
        commentService.toggleLike(SecurityUtil.getCurrentUserId(), id);
        return Result.success();
    }

    @ApiOperation("删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(SecurityUtil.getCurrentUserId(), id, SecurityUtil.isAdmin());
        return Result.success();
    }

    @ApiOperation("举报评论")
    @PostMapping("/report/{id}")
    public Result<Void> report(@PathVariable Long id, @RequestParam String reason) {
        commentService.reportComment(SecurityUtil.getCurrentUserId(), id, reason);
        return Result.success();
    }
}
