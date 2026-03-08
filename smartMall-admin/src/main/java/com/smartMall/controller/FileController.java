package com.smartMall.controller;

import cn.hutool.core.io.FileUtil;
import com.smartMall.annotation.AdminAuditLog;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.smartMall.entities.config.AppConfig;
import com.smartMall.entities.constant.Constants;
import com.smartMall.entities.enums.AdminOperationTypeEnum;
import com.smartMall.entities.enums.ResponseCodeEnum;
import com.smartMall.entities.vo.ResponseVO;
import com.smartMall.exception.BusinessException;
import com.smartMall.utils.FileUtils;
import com.smartMall.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/11/30 12:03
 */
@RestController
@RequestMapping("/file")
@Validated
@SaCheckPermission("product:manage")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Resource
    private AppConfig appConfig;

    @Resource
    private FileUtils fileUtils;

    @PostMapping("/uploadImage")
    @AdminAuditLog(value = "上传商品图片", type = AdminOperationTypeEnum.PRODUCT)
    public ResponseVO<String> uploadImage(@NotNull MultipartFile file, Boolean createThumbnail) throws IOException {
        String filePath = fileUtils.uploadImage(file, createThumbnail);
        return ResponseVO.success(filePath);
    }


    @GetMapping("/getResource")
    public void getResource(HttpServletResponse response, @NotNull String resourceName) {
        if (!StringTools.pathIsOk(resourceName)) {
            throw new BusinessException(ResponseCodeEnum.RESOURCE_NOT_FOUND);
        }
        String suffix = StringTools.getFileSuffix(resourceName);
        response.setContentType("image/" + suffix.replace(".", ""));
        response.setHeader("Cache-Control", "max-age=2590200");
        readFile(response, resourceName);
    }


    protected void readFile(HttpServletResponse response, String filePath) {
        if (!StringTools.pathIsOk(filePath)) {
            return;
        }
        File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + filePath);
        if (!file.exists()) {
            log.error("文件不存在");
            return;
        }
        try (OutputStream out = response.getOutputStream(); FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("文件读取失败", e);
        }
    }

}

