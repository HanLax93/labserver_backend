package site.handglove.labserver.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import site.handglove.labserver.mapper.AnnouncementMapper;
import site.handglove.labserver.model.Announcement;
import site.handglove.labserver.model.Img;
import site.handglove.labserver.model.ThreadImg;
import site.handglove.labserver.service.AnnouncementService;
import site.handglove.labserver.service.ImgService;
import site.handglove.labserver.service.ThreadImgService;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
    @Autowired
    private ImgService imgService;

    @Autowired
    private ThreadImgService threadImgService;

    @Deprecated
    @Override
    public List<String> getImgs(Integer threadId, Integer type) {
        LambdaQueryWrapper<ThreadImg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ThreadImg::getThreadId, threadId).eq(ThreadImg::getType, type);
        queryWrapper.select(ThreadImg::getImgId);
        List<Integer> imgIds = threadImgService.listObjs(queryWrapper, obj -> (Integer) obj);
        if (imgIds.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Img> imgQueryWrapper = new LambdaQueryWrapper<>();
        imgQueryWrapper.in(Img::getId, imgIds).select(Img::getFilename);
        List<String> imgs = imgService.listObjs(imgQueryWrapper, obj -> (String) obj);
        return imgs;
    }

    @Override
    public List<Img> getImages(Integer threadId, Integer type) {
        LambdaQueryWrapper<ThreadImg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ThreadImg::getThreadId, threadId).eq(ThreadImg::getType, type);
        queryWrapper.select(ThreadImg::getImgId);
        List<Integer> imgIds = threadImgService.listObjs(queryWrapper, obj -> (Integer) obj);
        if (imgIds.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Img> imgQueryWrapper = new LambdaQueryWrapper<>();
        imgQueryWrapper.in(Img::getId, imgIds);
        List<Img> imgs = imgService.list(imgQueryWrapper);
        return imgs;
    }

    @Override
    public Integer saveThread(Announcement announcement) {
        save(announcement);
        return announcement.getId();
    }
}
