package site.handglove.labserver.service.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import site.handglove.labserver.exception.CustomException;
import site.handglove.labserver.mapper.ThreadImgMapper;
import site.handglove.labserver.model.Img;
import site.handglove.labserver.model.ThreadImg;
import site.handglove.labserver.service.ImgService;
import site.handglove.labserver.service.ThreadImgService;

@Service
public class ThreadImgServiceImpl extends ServiceImpl<ThreadImgMapper, ThreadImg> implements ThreadImgService {
    @Autowired
    private ImgService imgService;

    private final ObjectMapper objectMapper;

    public ThreadImgServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void linkImages(List<MultipartFile> images, Integer threadId, Integer threadType, String author,
            String imagesPath) {
        for (MultipartFile image : images) {
            String filename = UUID.randomUUID().toString().replace("-", "") + ".png";
            String filePath = imagesPath + filename;
            try {
                File targetFile = new File(filePath);
                image.transferTo(targetFile);

            } catch (Exception e) {
                throw new CustomException("图片上传错误错误");
            }
            // 保存图片信息到数据库

            Integer imgId = imgService.saveImg(author, filename);
            ThreadImg threadImg = new ThreadImg();
            threadImg.setThreadId(threadId);
            threadImg.setImgId(imgId);
            threadImg.setType(threadType);
            this.save(threadImg);
        }
    }

    @Override
    public void unLinkImages(String removalJson, Integer threadId, Integer threadType) {
            try {
                List<Img> qImagesRemoval = objectMapper.readValue(removalJson, new TypeReference<List<Img>>() {
                });
                if (!qImagesRemoval.isEmpty()) {
                    List<Integer> removedIds = qImagesRemoval.stream().map(Img::getId).toList();
                    LambdaQueryWrapper<ThreadImg> removalWrapper = new LambdaQueryWrapper<>();
                    removalWrapper.eq(ThreadImg::getThreadId, threadId)
                            .eq(ThreadImg::getType, threadType)
                            .in(ThreadImg::getImgId, removedIds);
                    this.remove(removalWrapper);
                }
            } catch (Exception e) {
                throw new CustomException("参数错误");
            }
    }
}
