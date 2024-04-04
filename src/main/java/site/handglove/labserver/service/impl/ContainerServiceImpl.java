package site.handglove.labserver.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import site.handglove.labserver.mapper.ContainerMapper;
import site.handglove.labserver.model.Container;
import site.handglove.labserver.service.ContainerService;

@Service
public class ContainerServiceImpl extends ServiceImpl<ContainerMapper, Container> implements ContainerService{

}
