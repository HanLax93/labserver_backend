package site.handglove.labserver.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import site.handglove.labserver.model.Announcement;
import site.handglove.labserver.model.Img;

public interface AnnouncementService extends IService<Announcement> {
    public List<String> getImgs(Integer threadId, Integer type);

    public List<Img> getImages(Integer threadId, Integer type);

    public Integer saveThread(Announcement announcement);
}
