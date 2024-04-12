package site.handglove.labserver.service;

import com.baomidou.mybatisplus.extension.service.IService;

import site.handglove.labserver.model.Container;

public interface ContainerService extends IService<Container> {
    public boolean removeContainer(String name) throws Exception;
}
