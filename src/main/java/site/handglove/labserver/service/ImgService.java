package site.handglove.labserver.service;

import com.baomidou.mybatisplus.extension.service.IService;

import site.handglove.labserver.model.Img;

public interface ImgService extends IService<Img> {
    public Integer saveImg(String author, String filename);
}
