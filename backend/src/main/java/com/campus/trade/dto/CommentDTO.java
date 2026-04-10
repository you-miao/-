package com.campus.trade.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CommentDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    @NotBlank(message = "评论内容不能为空")
    private String content;
    private Long parentId;
    private Long replyToUserId;
    private Integer rating;
    private List<String> images;
}
