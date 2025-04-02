package site.handglove.labserver.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;

import site.handglove.labserver.model.ThreadImg;

public interface ThreadImgService extends IService<ThreadImg>{
    public void linkImages(List<MultipartFile> images, Integer threadId, Integer threadType, String author, String imagesPath);

    public void unLinkImages(String removalJson, Integer threadId, Integer threadType);
}
