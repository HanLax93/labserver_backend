package site.handglove.labserver.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import site.handglove.labserver.model.ThreadVersion;

@Mapper
public interface ThreadVersionMapper extends BaseMapper<ThreadVersion> {
    
}
