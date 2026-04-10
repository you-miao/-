package com.campus.trade.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.campus.trade.common.BusinessException;
import com.campus.trade.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "文件上传接口")
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.allowed-types}")
    private String allowedTypes;

    @ApiOperation("上传单个文件")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        validateFile(file);
        String url = saveFile(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return Result.success(result);
    }

    @ApiOperation("批量上传文件")
    @PostMapping("/upload/batch")
    public Result<List<String>> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFile(file);
            urls.add(saveFile(file));
        }
        return Result.success(urls);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BusinessException("不支持的文件格式");
        }
    }

    private String saveFile(MultipartFile file) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String baseDir = new File(uploadPath).getAbsolutePath();
        String dir = baseDir + File.separator + datePath;
        FileUtil.mkdir(dir);

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
        String fileName = IdUtil.fastSimpleUUID() + ext;

        try {
            File dest = new File(dir + File.separator + fileName);
            file.transferTo(dest);
            System.out.println("[Upload] 文件已保存: " + dest.getAbsolutePath());
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
        return "/uploads/" + datePath + "/" + fileName;
    }
}
