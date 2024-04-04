package site.handglove.labserver.controller;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import site.handglove.labserver.model.ApplyVo;
import site.handglove.labserver.model.Container;
import site.handglove.labserver.model.ContainerTask;
import site.handglove.labserver.model.Stu;
import site.handglove.labserver.model.User;
import site.handglove.labserver.result.Result;
import site.handglove.labserver.security.custom.CustomBcryptPasswordEncoder;
import site.handglove.labserver.security.custom.LoginUserHelper;
import site.handglove.labserver.service.StuService;
import site.handglove.labserver.service.TaskService;
import site.handglove.labserver.service.UserService;
import site.handglove.labserver.utils.Helper;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private StuService stuService;

    @Autowired
    private UserService userService;

    @PostMapping("apply")
    public Result<?> apply(@RequestBody ApplyVo applyVo) {
        // if exist stu_index
        LambdaQueryWrapper<Stu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Stu::getName);
        List<String> nameList = stuService.listObjs(queryWrapper, obj -> (String) obj);

        LambdaQueryWrapper<ContainerTask> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.select(ContainerTask::getName);
        List<String> nameList2 = taskService.listObjs(queryWrapper2, obj -> (String) obj);

        String username = applyVo.getUsername();
        nameList.addAll(nameList2);
        if (nameList.contains(username)) {
            return Result.FAIL().message("用户名已存在或者已提交");
        }

        // submit
        var containerTask = new ContainerTask();
        containerTask.setName(applyVo.getUsername());
        boolean submitted = taskService.save(containerTask);
        userService.save(new User(username, new CustomBcryptPasswordEncoder().encode("000000"), applyVo.getName(), applyVo.getEntryYear()));
        return submitted ? Result.OK().message("申请成功") : Result.FAIL().message("未知错误");
    }

    @PreAuthorize("hasAuthority('user')")
    @GetMapping("run")
    public Result<?> run() {
        String name = LoginUserHelper.getUsername();
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

    @PreAuthorize("hasAuthority('user')")
    @GetMapping("runSSH/{name}")
    public Result<?> runSSH() {
        var name = LoginUserHelper.getUsername();
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

    @PreAuthorize("hasAuthority('user')")
    @PutMapping("update")
    public Result<?> updateUser(@RequestBody User user) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, LoginUserHelper.getUsername());
        User realUser = userService.getOne(queryWrapper);
        if (realUser.getId().intValue() == user.getId() && realUser.getUsername().equals(user.getUsername()) && realUser.getPermission().intValue() == user.getPermission()) {
            boolean isOk = userService.updateById(user);
            return isOk ? Result.OK().message("修改成功") : Result.FAIL();
        } else {
            return Result.FAIL().message("用户非法");
        }
    }

    @PreAuthorize("hasAuthority('user')")
    @GetMapping("getCurUser")
    public Result<?> getCurUser() {
        String username = LoginUserHelper.getUsername();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userService.getOne(queryWrapper);
        return Result.OK(user);
    }

    @PreAuthorize("hasAuthority('user')")
    @GetMapping("container")
    public Result<?> container() {
        List<String> containerInfo = Helper.containerInfo(LoginUserHelper.getUsername());
        if (containerInfo != null && containerInfo.size() != 0) {
            Container container = Helper.containerParser(containerInfo.get(0).split("\t"));
            return Result.OK(new ArrayList<Container>(){
                {
                    add(container);
                }
            });
        }
        return Result.FAIL().message("无容器信息");
    }

    @PreAuthorize("hasAuthority('user')")
    @GetMapping("task")
    public Result<?> task() {
        LambdaQueryWrapper<ContainerTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContainerTask::getName, LoginUserHelper.getUsername());
        ContainerTask task = taskService.getOne(queryWrapper);
        return task != null ? Result.OK(new ArrayList<>(){{ add(task); }}) : Result.FAIL().message("未查询到任务");
    }
}