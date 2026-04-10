package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long userId;
    private Long parentId;
    private Long replyToUserId;
    private String content;
    private String images;
    private Integer rating;
    private Integer likeCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String userAvatar;
    @TableField(exist = false)
    private String replyToUserName;
    @TableField(exist = false)
    private List<Comment> replies;
    @TableField(exist = false)
    private Boolean isLiked;
}
