package site.handglove.labserver.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

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
        List<String> allContainersString = Helper.containerInfo(null);

        List<Container> allContainers = allContainersString.stream().map(item -> {
            var tmp = item.split("\t");
            return Helper.containerParser(tmp);
        }).collect(Collectors.toList());

        return allContainers;
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
        boolean success = Helper.addService(name, stuIndex);
        if (success) {
            var containerInfo = Helper.containerInfo(name);
            var tmp = containerInfo.get(0).split("\\t");
            var newContainer =  Helper.containerParser(tmp);
            var stu = new Stu(name, stuIndex);
            this.save(stu);
            containerService.save(newContainer);
            return newContainer;
        }
        return null;
    }
}
