package site.handglove.labserver.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.github.dockerjava.api.exception.DockerException;

import site.handglove.labserver.exception.CustomException;
import site.handglove.labserver.model.Container;
import site.handglove.labserver.model.ContainerTask;
import site.handglove.labserver.model.Stu;
import site.handglove.labserver.model.User;
import site.handglove.labserver.model.UserQueryVo;
import site.handglove.labserver.result.Result;
import site.handglove.labserver.service.ContainerService;
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

    @Autowired
    private ContainerService containerService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("allContainers")
    public Result<?> containers() {
        try {
            List<Container> allContainers = stuService.getAllContainers();
            return Result.OK(allContainers);
        } catch (DockerException ex) {
            return Result.FAIL().message(CustomException.parseDockerExceptionMessage(ex));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.FAIL().message("请查看日志");
        }
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("approve/{name}")
    public Result<?> createContainer(@PathVariable String name) {
        LambdaQueryWrapper<Container> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Container::getName, name);
        var checkContainer = containerService.getOne(checkWrapper);
        if (checkContainer != null) {
            return Result.FAIL().message("容器已存在");
        }

        try {
            var container = stuService.createContainer(name);
            if (container.getRunning() == 1 && container.getSshStatus() == 1) {
                LambdaQueryWrapper<ContainerTask> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ContainerTask::getName, name);
                ContainerTask task = taskService.getOne(queryWrapper);
                task.setIsProcessed(1);
                taskService.updateById(task);
                return Result.OK().message("操作成功");
            }
            return Result.FAIL().message("操作失败");
        } catch (DockerException ex) {
            return Result.FAIL().message(CustomException.parseDockerExceptionMessage(ex));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.FAIL().message("操作失败，请查看日志");
        }
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("decline/{name}")
    public Result<?> decline(@PathVariable String name) {
        LambdaQueryWrapper<ContainerTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContainerTask::getName, name);
        ContainerTask task = taskService.getOne(queryWrapper);
        task.setIsProcessed(2);
        var isOK = taskService.updateById(task);

        LambdaQueryWrapper<User> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(User::getUsername, name);
        User user = userService.getOne(queryWrapper2);
        user.setIsDeleted(1);
        userService.updateById(user);
        redisTemplate.delete(user.getUsername());

        return isOK ? Result.OK().message("已拒绝") : Result.FAIL().message("操作失败");
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("remove/{name}")
    public Result<?> removeUser(@PathVariable String name) {
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getUsername, name);
        User user = userService.getOne(userWrapper);
        if (user.getPermission() == 1) {
            return Result.FAIL().message("禁止删除管理员");
        }
        user.setIsDeleted(1);
        var isOK = userService.updateById(user);
        redisTemplate.delete(user.getUsername());
        return isOK ? Result.OK().message("删除成功") : Result.FAIL();
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("{page}/{limit}")
    public Result<?> allUsers(@PathVariable Long page, @PathVariable Long limit, UserQueryVo userQueryVo) {
        // 创建page对象
        Page<User> pageParam = new Page<>(page, limit);

        // 封装条件，判断条件值不为空
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDeleted, 0);
        // 获取条件值
        String username = userQueryVo.getKeyword();
        String createTimeBegin = userQueryVo.getCreateTimeBegin();
        String createTimeEnd = userQueryVo.getCreateTimeEnd();
        // 判断条件值不为空
        // like 模糊查询
        if (!StringUtils.isEmpty(username)) {
            wrapper.like(User::getUsername, username);
        }
        // ge 大于等于
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge(User::getCreatedAt, createTimeBegin);
        }
        // le 小于等于
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le(User::getCreatedAt, createTimeEnd);
        }

        // 调用mp的方法实现条件分页查询
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
        try {
            boolean ret1 = Helper.runContainer(name, null);
            boolean ret2 = Helper.runContainerSSH(name, null);
            if (ret1 && ret2) {
                return Result.OK().message("开启成功");
            }
            return Result.FAIL().message("开启失败");
        } catch (DockerException ex) {
            return Result.FAIL().message(CustomException.parseDockerExceptionMessage(ex));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.FAIL().message("请查看日志");
        }
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("runSSH/{name}")
    public Result<?> runSSH(@PathVariable String name) {
        try {
            boolean ret = Helper.runContainerSSH(name, null);
            if (ret) {
                return Result.OK().message("开启成功");
            }
            return Result.FAIL().message("开启失败");
        } catch (DockerException ex) {
            return Result.FAIL().message(CustomException.parseDockerExceptionMessage(ex));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.FAIL().message("请查看日志");
        }
    }

    @SuppressWarnings("null")
    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("tasks")
    public Result<?> tasks() {
        List<ContainerTask> tasks = taskService.list();
        return tasks != null || tasks.size() != 0 ? Result.OK(tasks) : Result.FAIL().message("未查询到任务");
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("removeContainer/{name}")
    public Result<?> removeContainer(@PathVariable String name) {
        try {
            containerService.removeContainer(name);
            LambdaQueryWrapper<Stu> stuWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<Container> containerWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            stuWrapper.eq(Stu::getName, name);
            containerWrapper.eq(Container::getName, name);
            userWrapper.eq(User::getUsername, name);

            stuService.remove(stuWrapper);
            containerService.remove(containerWrapper);
            var user = userService.getOne(userWrapper);
            user.setIsDeleted(1);
            userService.updateById(user);

            return Result.OK().message("操作成功");
        } catch (DockerException ex) {
            return Result.FAIL().message(CustomException.parseDockerExceptionMessage(ex));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.FAIL().message("请查看日志");
        }
    }
}
