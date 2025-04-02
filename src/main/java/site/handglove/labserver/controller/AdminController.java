package site.handglove.labserver.controller;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.DockerException;

import site.handglove.labserver.exception.CustomException;
import site.handglove.labserver.model.Announcement;
import site.handglove.labserver.model.Container;
import site.handglove.labserver.model.ContainerTask;
import site.handglove.labserver.model.Img;
import site.handglove.labserver.model.Question;
import site.handglove.labserver.model.Stu;
import site.handglove.labserver.model.ThreadImg;
import site.handglove.labserver.model.ThreadVersion;
import site.handglove.labserver.model.User;
import site.handglove.labserver.model.vo.UserQueryVo;
import site.handglove.labserver.result.Result;
import site.handglove.labserver.security.custom.LoginUserHelper;
import site.handglove.labserver.service.AnnouncementService;
import site.handglove.labserver.service.ContainerService;
import site.handglove.labserver.service.ImgService;
import site.handglove.labserver.service.QuestionService;
import site.handglove.labserver.service.StuService;
import site.handglove.labserver.service.TaskService;
import site.handglove.labserver.service.ThreadImgService;
import site.handglove.labserver.service.ThreadVersionService;
import site.handglove.labserver.service.UserService;
import site.handglove.labserver.utils.Helper;

@CrossOrigin
@RestController()
@RequestMapping("/admin")
public class AdminController {
    @Value("${resourcesPath}")
    private String resourcesPath;

    @Autowired
    private StuService stuService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private RedisTemplate<String, Object> myRedisTemplate;

    @Autowired
    private ImgService imgService;

    @Autowired
    private ThreadImgService threadImgService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private ThreadVersionService threadVersionService;

    @Autowired
    private QuestionService questionService;

    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String TAG = "[AdminController]";

    public AdminController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("allContainers")
    public Result<?> containers() {
        Comparator<Container> containerComparator = new Comparator<Container>() {

            @Override
            public int compare(Container arg0, Container arg1) {
                return arg0.getCreateTime().compareTo(arg1.getCreateTime());
            }

        };
        try {
            List<Container> allContainers = stuService.getAllContainers();
            allContainers.sort(containerComparator);
            return Result.OK(allContainers);
        } catch (DockerException ex) {
            return Result.FAIL().message(CustomException.parseDockerExceptionMessage(ex));
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(TAG + "allContrainers", ex);;
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
            logger.error(TAG + "createContainer", ex);
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
        myRedisTemplate.delete(user.getUsername());

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
        myRedisTemplate.delete(user.getUsername());
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
            logger.error(TAG + "run", ex);
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
            logger.error(TAG + "runSSH", ex);
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
            logger.error(TAG + "removeContainer", ex);
            return Result.FAIL().message("请查看日志");
        }
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("announcement/update")
    public Result<?> updateAnnouncement(
            @RequestParam(value = "imgs", required = false) List<MultipartFile> images,
            @RequestParam(value = "removal", required = false) String removalJson,
            @RequestParam("announcement") String announcementJson) {
        Announcement announcement = null;
        List<Img> removal = null;
        Integer threadId = null;

        try {
            announcement = objectMapper.readValue(announcementJson, Announcement.class);
            threadId = announcement.getId();
            if (removalJson != null) {
                removal = objectMapper.readValue(removalJson, new TypeReference<List<Img>>() {
                });
                List<Integer> removedIds = removal.stream().map(Img::getId).toList();
                // 删除关联图片 (thread_type = 0)
                if (removedIds != null && !removedIds.isEmpty()) {
                    LambdaQueryWrapper<ThreadImg> removalWrapper = new LambdaQueryWrapper<>();
                    removalWrapper.eq(ThreadImg::getThreadId, threadId)
                            .eq(ThreadImg::getType, 0)
                            .in(ThreadImg::getImgId, removedIds);
                    threadImgService.remove(removalWrapper);
                }
            }
        } catch (Exception e) {
            return Result.FAIL().message("参数错误");
        }

        // 更新公告
        if (StringUtils.isEmpty(announcement.getTitle()) && StringUtils.isEmpty(announcement.getDescription())) {
            return Result.FAIL().message("标题和内容不能为空");
        }
        if (null != threadId) {
            announcementService.updateById(announcement);
        } else {
            announcement.setAuthor(LoginUserHelper.getUsername());
            threadId = announcementService.saveThread(announcement);
        }
        LambdaQueryWrapper<ThreadVersion> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(ThreadVersion::getThreadName, "announcements");
        ThreadVersion threadVersion = threadVersionService.getOne(versionWrapper);
        threadVersion.setVersion(threadVersion.getVersion() + 1);
        myRedisTemplate.delete("announcements");
        threadVersionService.updateById(threadVersion);

        // 新增图片
        String imageResourcePath = resourcesPath + "imgs/";
        if (null != images && !images.isEmpty()) {
            for (MultipartFile image : images) {
                UUID uuid = UUID.randomUUID();
                String filename = uuid.toString().replace("-", "");
                String destination = imageResourcePath + filename + ".png";
                try {
                    // 保存文件到服务器
                    File targetFile = new File(destination.toString());
                    image.transferTo(targetFile);
                    // 保存图片信息到数据库
                    Integer imgId = imgService.saveImg(LoginUserHelper.getUsername(), filename + ".png");
                    ThreadImg threadImg = new ThreadImg();
                    threadImg.setThreadId(threadId);
                    threadImg.setImgId(imgId);
                    threadImg.setType(0);
                    threadImgService.save(threadImg);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Result.FAIL().message("图片保存失败");
                }
            }
        }
        return Result.OK().message("更新成功");
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("announcement/delete")
    public Result<?> deleteAnnouncement(@RequestBody Announcement announcement) {
        boolean success = announcementService.removeById(announcement.getId());
        if (!success) {
            return Result.FAIL().message("删除失败");
        }
        LambdaQueryWrapper<ThreadVersion> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(ThreadVersion::getThreadName, "announcements");
        ThreadVersion threadVersion = threadVersionService.getOne(versionWrapper);
        threadVersion.setVersion(threadVersion.getVersion() + 1);
        myRedisTemplate.delete("announcements");
        threadVersionService.updateById(threadVersion);
        return Result.OK().message("删除成功");
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("question/update")
    public Result<?> updateQuestion(
            @RequestParam(value = "qAddedImages", required = false) List<MultipartFile> qAddedImages,
            @RequestParam(value = "aAddedImages", required = false) List<MultipartFile> aAddedImages,
            @RequestParam(value = "qImagesRemoval", required = false) String qImagesRemovalJson,
            @RequestParam(value = "aImagesRemoval", required = false) String aImagesRemovalJson,
            @RequestParam("question") String questionJson) {
        // get question
        int threadId = -1;
        Question question;
        try {
            question = objectMapper.readValue(questionJson, Question.class);
            threadId = question.getId();

            if (threadId == -1) {
                throw new CustomException("参数错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.FAIL().message("参数错误");
        }

        String imagesPath = resourcesPath + "imgs/";
        if (null != qAddedImages) {
            try {
                threadImgService.linkImages(qAddedImages, threadId, 1, LoginUserHelper.getUsername(), imagesPath);
            } catch (CustomException ex) {
                return Result.FAIL().message(ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(TAG + "updateQuestion", ex);
                return Result.FAIL().message("未知错误");
            }
        }

        if (null != aAddedImages) {
            try {
                threadImgService.linkImages(aAddedImages, threadId, 2, LoginUserHelper.getUsername(), imagesPath);
            } catch (CustomException ex) {
                return Result.FAIL().message(ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(TAG + "updateQuestion", ex);
                return Result.FAIL().message("未知错误");
            }
        }

        if (null != qImagesRemovalJson) {
            try {
                threadImgService.unLinkImages(qImagesRemovalJson, threadId, 1);
            } catch (CustomException ex) {
                return Result.FAIL().message(ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(TAG + "updateQuestion", ex);
                return Result.FAIL().message("未知错误");
            }
        }

        if (null != aImagesRemovalJson) {
            try {
                threadImgService.unLinkImages(aImagesRemovalJson, threadId, 2);
            } catch (CustomException ex) {
                return Result.FAIL().message(ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(TAG + "updateQuestion", ex);
                return Result.FAIL().message("未知错误");
            }
        }

        if (StringUtils.isEmpty(question.getTitle()) && StringUtils.isEmpty(question.getDescription())) {
            return Result.FAIL().message("标题和内容不能为空");
        }
        questionService.updateById(question);
        LambdaQueryWrapper<ThreadVersion> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(ThreadVersion::getThreadName, "questions");
        ThreadVersion threadVersion = threadVersionService.getOne(versionWrapper);
        threadVersion.setVersion(threadVersion.getVersion() + 1);
        myRedisTemplate.delete("questions");
        threadVersionService.updateById(threadVersion);

        return Result.OK().message("编辑成功");
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("question/delete")
    public Result<?> deleteQuestion(@RequestBody Question question) {
        boolean success = questionService.removeById(question.getId());
        if (!success) {
            return Result.FAIL().message("删除失败");
        }
        LambdaQueryWrapper<ThreadVersion> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(ThreadVersion::getThreadName, "questions");
        ThreadVersion threadVersion = threadVersionService.getOne(versionWrapper);
        threadVersion.setVersion(threadVersion.getVersion() + 1);
        myRedisTemplate.delete("questions");
        threadVersionService.updateById(threadVersion);
        return Result.OK().message("删除成功");
    }
}
