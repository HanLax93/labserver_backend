package site.handglove.labserver.mapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import site.handglove.labserver.model.ContainerTask;

@Repository
public interface TaskMapper extends BaseMapper<ContainerTask> {

}
