package site.handglove.labserver.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.dockerjava.api.exception.DockerException;

import site.handglove.labserver.mapper.StuMapper;
import site.handglove.labserver.model.Container;
import site.handglove.labserver.model.Stu;
import site.handglove.labserver.service.ContainerService;
import site.handglove.labserver.service.StuService;
import site.handglove.labserver.utils.Helper;

@Service
public class StuServiceImpl extends ServiceImpl<StuMapper, Stu> implements StuService {
    @Autowired
    private ContainerService containerService;

    @Override
    public List<Container> getAllContainers() {
        LambdaQueryWrapper<Container> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Container::getName);
        List<String> names = containerService.listObjs(queryWrapper, obj -> (String) obj);
        try {
            List<Container> allContainers = names.stream().map(item -> {
                return Helper.containerInfo(null, item);
            }).collect(Collectors.toList());
            return allContainers;
        } catch (DockerException ex) {
            throw ex;
        }
    }

    @Override
    public Container createContainer(String name) {
        LambdaQueryWrapper<Stu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Stu::getStuIndex);
        List<Integer> stuIndexes = this.listObjs(queryWrapper, obj -> (Integer) obj);
        stuIndexes.sort((a, b) -> a - b);

        int stuIndex = stuIndexes.get(stuIndexes.size() - 1) + 1;
        if (stuIndex == 8) {
            ++stuIndex;
        }
        
        try {
            boolean success = Helper.createContainer(name, stuIndex);
            if (success) {
                var newContainer =  Helper.containerInfo(null, name);
                var stu = new Stu(name, stuIndex);
                this.save(stu);
                containerService.save(newContainer);
                return newContainer;
            }
        } catch (DockerException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
