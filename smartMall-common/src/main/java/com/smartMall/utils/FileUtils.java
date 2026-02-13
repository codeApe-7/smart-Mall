package com.smartMall.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;

import com.smartMall.entities.config.AppConfig;
import com.smartMall.entities.constant.Constants;
import com.smartMall.entities.enums.DateTimePatternEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/11/30 14:52
 */
@Component
public class FileUtils {

    @Resource
    private AppConfig appConfig;

    public String uploadImage(MultipartFile file, Boolean createThumbnail) throws IOException {
        // 1. 生成文件夹路径：按年月日分目录（如 2025/04/15/）
        String folderName = DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern()) + "/";
        String folderPath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + folderName;

        // 2. 创建文件夹（若不存在）
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); // 使用 mkdirs 而非 mkdirs（图片中为笔误）
        }

        // 3. 生成唯一文件名（含后缀）
        String fileName = StringTools.getRandomNumber(Constants.LENGTH_32);
        String suffix = StringTools.getFileSuffix(file.getOriginalFilename());
        String resultFileName = fileName + suffix;

        // 4. 保存原图
        String filePath = folderPath + resultFileName;
        file.transferTo(new File(filePath));

        // 5. 若需生成缩略图，则调用 createImageThumbnail（复用现有逻辑）
        if (createThumbnail != null && createThumbnail) {
            String thumbnailPath = filePath.replace(suffix, Constants.IMAGE_THUMBNAIL_SUFFIX + suffix);
            // 注意：原 createImageThumbnail 方法使用的是 Constants.IMAGE_THUMBNAIL_PATH（字符串替换），但此处更推荐显式拼接路径
            // 为保持一致性，仍沿用原逻辑：替换后缀为 "_thumb" + 原后缀
            String thumbFileName = fileName + Constants.IMAGE_THUMBNAIL_SUFFIX + suffix;
            String thumbFilePath = folderPath + thumbFileName;

            // 调用 ffmpeg 生成缩略图（200px宽，等比缩放）
            String cmd = String.format("ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"", filePath, thumbFilePath);
            ProcessUtils.executeCommand(cmd, true);
        }

        // 6. 返回相对路径（或可配置为 URL 路径）
        return folderName + resultFileName;
    }


    public void createImageThumbnail(String filePath) {
        String CMD = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";
        String suffix = "." + FileUtil.getSuffix(filePath);
        CMD = String.format(CMD, filePath, filePath.replace(suffix, Constants.IMAGE_THUMBNAIL_SUFFIX));
        ProcessUtils.executeCommand(CMD, true);
    }
}
