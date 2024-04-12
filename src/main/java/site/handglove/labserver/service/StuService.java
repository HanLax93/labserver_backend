package site.handglove.labserver.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import site.handglove.labserver.model.Container;
import site.handglove.labserver.model.Stu;

public interface StuService extends IService<Stu> {
    public List<Container> getAllContainers() throws Exception;

    public Container createContainer(String name);
}
