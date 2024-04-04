package site.handglove.labserver.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.servlet.http.HttpServletRequest;
import site.handglove.labserver.model.LoginVo;
import site.handglove.labserver.model.RouterVo;
import site.handglove.labserver.model.User;
import site.handglove.labserver.result.Result;
import site.handglove.labserver.security.custom.CustomBcryptPasswordEncoder;
import site.handglove.labserver.security.custom.LoginUserHelper;
import site.handglove.labserver.service.UserMenuService;
import site.handglove.labserver.service.UserService;
import site.handglove.labserver.utils.JWTHelper;

@CrossOrigin
@RestController
public class IndexController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMenuService userMenuService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginVo loginVo) {
        Map<String, Object> map = new HashMap<>();
        String username = loginVo.getUsername();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        User user = userService.getOne(queryWrapper.eq(User::getUsername, username));

        if (user == null) {
            return Result.FAIL("用户不存在");
        }

        if (!new CustomBcryptPasswordEncoder().matches(loginVo.getPassword(), user.getPasswordHash())) {
            return Result.FAIL("密码错误");
        }

        String token = JWTHelper.createToken(username);
        map.put("token", token);

        return Result.OK(map);
    }

    @GetMapping("/info")
    public Result<?> info(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        // 获取token字符串
        String token = request.getHeader("token");
        // 解析token字符串，获取用户id或者用户名
        String username = JWTHelper.getUsername(token);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userService.getOne(queryWrapper);;
        // 根据用户id或者用户名查询用户信息
        // 返回用户可以操作的菜单列表
        // 查询数据库动态构建路由结构，进行显示
        List<RouterVo> routerList = userMenuService.getMenusByUsername(username);
        if (user.getPermission() == 0) {
            routerList.get(0).setAlwaysShow(false);
            routerList.get(0).getMeta().setTitle("");
        }
        routerList.get(1).setAlwaysShow(false);
        routerList.get(1).getMeta().setTitle("");
        // 返回用户可以操作的按钮列表
        List<String> buttonList = userMenuService.getButtonsByUsername(username);
        map.put("name", username);
        map.put("routers", routerList);
        map.put("buttons", buttonList);

        return Result.OK(map);
    }

    @GetMapping("/user/logout")
    public Result<?> logout() {
        LoginUserHelper.removeUsername();
        return Result.OK();
    }
}
