package site.handglove.labserver.mapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import site.handglove.labserver.model.Announcement;

@Repository
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    
}
