package site.handglove.labserver.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.dockerjava.api.exception.DockerException;

import site.handglove.labserver.mapper.ContainerMapper;
import site.handglove.labserver.model.Container;
import site.handglove.labserver.service.ContainerService;
import site.handglove.labserver.utils.Helper;

@Service
public class ContainerServiceImpl extends ServiceImpl<ContainerMapper, Container> implements ContainerService{
    public boolean removeContainer(String name) throws Exception {
        LambdaQueryWrapper<Container> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Container::getName, name);
        var container = this.getOne(queryWrapper);
        try {
            Helper.removeContainer(container.getStuIndex(), name);
            return true;
        } catch (DockerException ex) {
            throw ex;
        }
    }
}
