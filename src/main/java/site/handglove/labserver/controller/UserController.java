package site.handglove.labserver.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.DockerException;

import site.handglove.labserver.exception.CustomException;
import site.handglove.labserver.model.Announcement;
import site.handglove.labserver.model.Container;
import site.handglove.labserver.model.ContainerTask;
import site.handglove.labserver.model.Question;
import site.handglove.labserver.model.Stu;
import site.handglove.labserver.model.ThreadVersion;
import site.handglove.labserver.model.User;
import site.handglove.labserver.model.vo.ApplyVo;
import site.handglove.labserver.result.Result;
import site.handglove.labserver.security.custom.CustomBcryptPasswordEncoder;
import site.handglove.labserver.security.custom.LoginUserHelper;
import site.handglove.labserver.service.AnnouncementService;
import site.handglove.labserver.service.QuestionService;
import site.handglove.labserver.service.StuService;
import site.handglove.labserver.service.TaskService;
import site.handglove.labserver.service.ThreadImgService;
import site.handglove.labserver.service.ThreadVersionService;
import site.handglove.labserver.service.UserService;
import site.handglove.labserver.utils.Helper;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Value("${resourcesPath}")
    private String resourcesPath;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StuService stuService;

    @Autowired
    private UserService userService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ThreadVersionService threadVersionService;

    @Autowired
    private RedisTemplate<String, Object> myRedisTemplate;

    @Autowired
    private ThreadImgService threadImgService;

    private final ObjectMapper objectMapper;

    private final String TAG = "[UserController]";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
        userService.save(new User(username, new CustomBcryptPasswordEncoder().encode("000000"), applyVo.getName(),
                applyVo.getEntryYear()));
        return submitted ? Result.OK().message("申请成功") : Result.FAIL().message("未知错误");
    }

    @PreAuthorize("hasAuthority('user')")
    @GetMapping("run")
    public Result<?> run() {
        String name = LoginUserHelper.getUsername();
        try {
            boolean ret1 = Helper.runContainer(name, null);
            boolean ret2 = Helper.runContainerSSH(name, null);
            if (ret1 && ret2) {
                return Result.OK().message("开启成功");
            }
            return Result.FAIL().message("开启失败");
        } catch (Exception ex) {
            return Result.FAIL().message(ex.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('user')")
    @GetMapping("runSSH/{name}")
    public Result<?> runSSH() {
        var name = LoginUserHelper.getUsername();
        try {
            boolean ret = Helper.runContainerSSH(name, null);
            if (ret) {
                return Result.OK().message("开启成功");
            }
            return Result.FAIL().message("开启失败");
        } catch (Exception ex) {
            return Result.FAIL().message(ex.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('user')")
    @PutMapping("update")
    public Result<?> updateUser(@RequestBody User user) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, LoginUserHelper.getUsername());
        User realUser = userService.getOne(queryWrapper);
        if (realUser.getId().intValue() == user.getId() && realUser.getUsername().equals(user.getUsername())
                && realUser.getPermission().intValue() == user.getPermission()) {
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
        try {
            var container = Helper.containerInfo(null, LoginUserHelper.getUsername());
            return Result.OK(new ArrayList<Container>() {
                {
                    add(container);
                }
            });
        } catch (DockerException ex) {
            return Result.FAIL().message(CustomException.parseDockerExceptionMessage(ex));
        } catch (Exception ex) {
            return Result.FAIL().message("位置错误");
        }
    }

    @PreAuthorize("hasAuthority('user')")
    @GetMapping("task")
    public Result<?> task() {
        LambdaQueryWrapper<ContainerTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContainerTask::getName, LoginUserHelper.getUsername());
        ContainerTask task = taskService.getOne(queryWrapper);
        return task != null ? Result.OK(new ArrayList<>() {
            {
                add(task);
            }
        }) : Result.FAIL().message("未查询到任务");
    }

    @SuppressWarnings({ "unchecked" })
    @PreAuthorize("hasAuthority('user')")
    @GetMapping("announcement")
    public Result<?> getAnnouncement() {
        boolean isAdmin = LoginUserHelper.getPermission() != 0;
        List<Announcement> pushedAnnouncements = new ArrayList<>();
        List<Announcement> announcements = new ArrayList<>();
        LambdaQueryWrapper<ThreadVersion> queryWrapper = new LambdaQueryWrapper<>();
        ThreadVersion threadVersion = threadVersionService
                .getOne(queryWrapper.eq(ThreadVersion::getThreadName, "announcements"));
        String announcementsDatabaseVersion = threadVersion.getVersion().toString();
        // query redis
        if (myRedisTemplate.hasKey("announcements") && myRedisTemplate.hasKey("announcementsVersion")) {
            // check version
            String announcementsRedisVersion = (String) myRedisTemplate.opsForValue().get("announcementsVersion");
            if (announcementsDatabaseVersion.equals(announcementsRedisVersion)) {
                ListOperations<String, Object> listOperations = myRedisTemplate.opsForList();
                List<Object> announcementsWrapper = listOperations.range("announcements", 0, -1);
                if (announcementsWrapper != null && announcementsWrapper.size() > 0) {
                    announcements = (List<Announcement>) announcementsWrapper.get(0);
                }
            }
        }

        announcements = announcementService.list();
        announcements.forEach(announcement -> {
            // thread_type = 0
            announcement.setImgs(announcementService.getImages(announcement.getId(), 0));
        });
        // test codes ends
        // save to redis
        if (announcements == null || announcements.isEmpty()) {
            return Result.FAIL().message("未查询到公告");
        }
        myRedisTemplate.delete("announcements");
        ListOperations<String, Object> listOperations = myRedisTemplate.opsForList();
        listOperations.rightPushAll("announcements", announcements);
        myRedisTemplate.opsForValue().set("announcementsVersion", announcementsDatabaseVersion);

        // 为管理员和普通用户筛选公告
        if (!isAdmin) {
            for (Announcement announcement : announcements) {
                if (announcement.getForAdmin() == 0) {
                    pushedAnnouncements.add(announcement);
                }
            }
            return Result.OK(pushedAnnouncements);
        }
        return Result.OK(announcements);
    }

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('user')")
    @GetMapping("question")
    public Result<?> getQuestion() {
        boolean isAdmin = LoginUserHelper.getPermission() != 0;
        List<Question> pushedQuestions = new ArrayList<>();
        List<Question> questions = new ArrayList<>();
        LambdaQueryWrapper<ThreadVersion> queryWrapper = new LambdaQueryWrapper<>();
        ThreadVersion threadVersion = threadVersionService
                .getOne(queryWrapper.eq(ThreadVersion::getThreadName, "questions"));
        String questionsDatabaseVersion = threadVersion.getVersion().toString();
        // query redis
        if (myRedisTemplate.hasKey("questions") && myRedisTemplate.hasKey("questionsVersion")) {
            // check version
            String questionsRedisVersion = (String) myRedisTemplate.opsForValue().get("questionsVersion");
            if (questionsDatabaseVersion.equals(questionsRedisVersion)) {
                ListOperations<String, Object> listOperations = myRedisTemplate.opsForList();
                List<Object> questionsWrapper = listOperations.range("questions", 0, -1);

                if (questionsWrapper != null && questionsWrapper.size() > 0) {
                    questions = (List<Question>) questionsWrapper.get(0);
                }
            }
        }

        if (questions == null || questions.isEmpty()) {
            questions = questionService.list();
            questions.forEach(question -> {
                // thread_type = 1
                question.setImgs(announcementService.getImages(question.getId(), 1));
                question.setAnsImgs(announcementService.getImages(question.getId(), 2));
            });
            // save to redis
            if (questions == null || questions.isEmpty()) {
                return Result.FAIL().message("未查询到问题");
            }
            myRedisTemplate.delete("questions");
            ListOperations<String, Object> listOperations = myRedisTemplate.opsForList();
            listOperations.rightPushAll("questions", questions);
            myRedisTemplate.opsForValue().set("questionsVersion", questionsDatabaseVersion);
        }

        // 为管理员和普通用户筛选问答
        if (!isAdmin) {
            for (Question question : questions) {
                if (question.getForAdmin() == 0) {
                    pushedQuestions.add(question);
                }
            }
            return Result.OK(pushedQuestions);
        }
        return Result.OK(questions);
    }

    @PreAuthorize("hasAuthority('user')")
    @PostMapping("question/add")
    private Result<?> addQuestion(@RequestBody Question question) {
        boolean isOk = questionService.save(question);
        return isOk ? Result.OK().message("添加成功") : Result.FAIL().message("添加失败");
    }

    @PreAuthorize("hasAuthority('user')")
    @PostMapping("question/save")
    public Result<?> saveQuestion(
            @RequestParam("question") String questionJson,
            @RequestParam(value = "qAddedImages", required = false) List<MultipartFile> qAddedImages) {
        Question question;

        boolean isAdmin = LoginUserHelper.getPermission() != 0;
        try {
            question = objectMapper.readValue(questionJson, Question.class);

            if (null == question) {
                throw new CustomException("参数错误");
            }
        } catch (Exception e) {
            return Result.FAIL().message("参数");
        }

        // 保存问题
        if (!StringUtils.hasText(questionJson) || !StringUtils.hasText(question.getDescription())) {
            return Result.FAIL().message("标题和描述不能为空");
        }

        if (!isAdmin) {
            question.setForAdmin(0);
        }
        question.setAuthor(LoginUserHelper.getUsername());
        questionService.save(question);
        int threadId = question.getId();

        // 保存图片
        String imagesPath = resourcesPath + "imgs/";
        if (null != qAddedImages) {
            try {
                threadImgService.linkImages(qAddedImages, threadId, 1, LoginUserHelper.getUsername(), imagesPath);
            } catch (CustomException ex) {
                return Result.FAIL().message(ex.getMessage());
            } catch (Exception ex) {
                logger.error(TAG + "saveQuestion", ex);
                return Result.FAIL().message("未知错误");
            }
        }

        // 更新版本
        LambdaQueryWrapper<ThreadVersion> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(ThreadVersion::getThreadName, "questions");
        ThreadVersion threadVersion = threadVersionService.getOne(versionWrapper);
        threadVersion.setVersion(threadVersion.getVersion() + 1);
        myRedisTemplate.delete("questions");
        threadVersionService.updateById(threadVersion);

        return Result.OK().message("保存成功");
    }
}