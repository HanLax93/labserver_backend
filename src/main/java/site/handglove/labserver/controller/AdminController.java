package site.handglove.labserver.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import site.handglove.labserver.model.Container;
import site.handglove.labserver.model.ContainerTask;
import site.handglove.labserver.model.User;
import site.handglove.labserver.model.UserQueryVo;
import site.handglove.labserver.result.Result;
import site.handglove.labserver.service.StuService;
import site.handglove.labserver.service.TaskService;
import site.handglove.labserver.service.UserService;
import site.handglove.labserver.utils.Helper;


@CrossOrigin
@RestController()
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private StuService stuService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("allContainers")
    public Result<?> containers() {
        List<Container> allContainers = stuService.getAllContainers();
        return Result.OK(allContainers);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("approve/{name}")
    public Result<?> createContainer(@PathVariable String name) {
        var container = stuService.createContainer(name);
        if (container.getRunning() == 1 && container.getSshStatus() == 1) {
            LambdaQueryWrapper<ContainerTask> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ContainerTask::getName, name);
            ContainerTask task = taskService.getOne(queryWrapper);
            task.setIsProcessed(1);
            taskService.updateById(task);
            return Result.OK().message("操作成功");
        }
        return Result.FAIL();
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("{page}/{limit}")
    public Result<?> allUsers(@PathVariable Long page, @PathVariable Long limit, UserQueryVo userQueryVo) {
        //创建page对象
        Page<User> pageParam = new Page<>(page,limit);

        //封装条件，判断条件值不为空
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        //获取条件值
        String username = userQueryVo.getKeyword();
        String createTimeBegin = userQueryVo.getCreateTimeBegin();
        String createTimeEnd = userQueryVo.getCreateTimeEnd();
        //判断条件值不为空
        //like 模糊查询
        if(!StringUtils.isEmpty(username)) {
            wrapper.like(User::getUsername,username);
        }
        //ge 大于等于
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge(User::getCreatedAt,createTimeBegin);
        }
        //le 小于等于
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le(User::getCreatedAt,createTimeEnd);
        }

        //调用mp的方法实现条件分页查询
        IPage<User> pageModel = userService.page(pageParam, wrapper);
        return Result.OK(pageModel);
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("get/{username}")
    public Result<?> getUserById(@PathVariable String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userService.getOne(queryWrapper);
        return Result.OK(user);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("update")
    public Result<?> updateUser(@RequestBody User user) {
        boolean isOk = userService.updateById(user);
        return isOk ? Result.OK() : Result.FAIL();
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("run/{name}")
    public Result<?> run(@PathVariable String name) {
        Helper.runContainer(name);
        Helper.runContainerSSH(name);
        List<String> containerInfo = Helper.containerInfo(name);
        if (containerInfo.size() != 0) {
            Container container = Helper.containerParser(containerInfo.get(0).split("\\t"));
            if (container.getRunning() == 1 && container.getSshStatus() == 1) {
                return Result.OK().message("启动成功");
            }
        }
            return Result.FAIL().message("启动失败");
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("runSSH/{name}")
    public Result<?> runSSH(@PathVariable String name) {
        Helper.runContainerSSH(name);
        List<String> containerInfo = Helper.containerInfo(name);
        if (containerInfo.size() != 0) {
            Container container = Helper.containerParser(containerInfo.get(0).split("\\t"));
            if (container.getRunning() == 1 && container.getSshStatus() == 1) {
                return Result.OK().message("启动成功");
            }
        }
            return Result.FAIL().message("启动失败");
    }

    @SuppressWarnings("null")
    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("tasks")
    public Result<?> tasks() {
        List<ContainerTask> tasks = taskService.list();
        return tasks != null || tasks.size() != 0 ? Result.OK(tasks) : Result.FAIL().message("未查询到任务");
    }
}
