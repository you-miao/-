package com.campus.trade.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateUserInfoDTO {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称长度不能超过20位")
    private String nickname;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "真实姓名长度不能超过20位")
    private String realName;

    @NotNull(message = "请选择性别")
    private Integer gender;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^\\d{6,20}$", message = "学号需为6-20位数字")
    private String studentNo;
}
