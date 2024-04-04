package site.handglove.labserver.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import site.handglove.labserver.mapper.TaskMapper;
import site.handglove.labserver.model.ContainerTask;
import site.handglove.labserver.service.TaskService;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, ContainerTask> implements TaskService {

}
