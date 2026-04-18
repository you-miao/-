package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.LoginDTO;
import com.campus.trade.dto.RegisterDTO;
import com.campus.trade.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@Api(tags = "认证接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String device = request.getHeader("User-Agent");
        Map<String, Object> result = authService.login(dto, ip, device);
        return Result.success("登录成功", result);
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success("注册成功", null);
    }

    @ApiOperation("检查用户名是否可用")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        return Result.success(authService.isUsernameAvailable(username));
    }

    @ApiOperation("检查学号是否可用")
    @GetMapping("/check-student-no")
    public Result<Boolean> checkStudentNo(@RequestParam String studentNo) {
        return Result.success(authService.isStudentNoAvailable(studentNo));
    }
}
