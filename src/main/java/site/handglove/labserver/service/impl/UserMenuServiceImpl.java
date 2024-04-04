package site.handglove.labserver.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import site.handglove.labserver.mapper.UserMenuMapper;
import site.handglove.labserver.model.Menu;
import site.handglove.labserver.model.RouterVo;
import site.handglove.labserver.model.User;
import site.handglove.labserver.model.UserMenu;
import site.handglove.labserver.service.MenuService;
import site.handglove.labserver.service.UserMenuService;
import site.handglove.labserver.service.UserService;
import site.handglove.labserver.utils.Helper;

@Service
public class UserMenuServiceImpl extends ServiceImpl<UserMenuMapper, UserMenu> implements UserMenuService {
    @Autowired
    private MenuService menuService;

    @Autowired
    private UserService userService;

    @Override
    public List<RouterVo> getMenusByUsername(String username) {
        List<Menu> list = null;
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userService.getOne(queryWrapper);

        LambdaQueryWrapper<UserMenu> menuIdQueryWrapper = new LambdaQueryWrapper<>();
        menuIdQueryWrapper.eq(UserMenu::getRoleId, user.getPermission());
        List<UserMenu> userMenus = this.list(menuIdQueryWrapper);
        List<Integer> menuIds = userMenus.stream().map(item -> item.getMenuId()).collect(Collectors.toList());

        LambdaQueryWrapper<Menu> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(Menu::getId, menuIds);
        list = menuService.list(queryWrapper2);

        List<Menu> menuTree = Helper.buildTree(list);
        List<RouterVo> routerVoList = Helper.buildRouter(menuTree);

        return routerVoList;
    }

    @Override
    public List<String> getButtonsByUsername(String username) {
        List<Menu> list = null;
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userService.getOne(queryWrapper);

        LambdaQueryWrapper<UserMenu> menuIdQueryWrapper = new LambdaQueryWrapper<>();
        menuIdQueryWrapper.eq(UserMenu::getRoleId, user.getPermission());
        List<UserMenu> userMenus = this.list(menuIdQueryWrapper);
        List<Integer> menuIds = userMenus.stream().map(item -> item.getMenuId()).collect(Collectors.toList());

        LambdaQueryWrapper<Menu> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.in(Menu::getId, menuIds);
        list = menuService.list(queryWrapper2);

        List<String> buttonList = list.stream().filter(e -> e.getType().intValue() == 2).map(item -> item.getPerms()).collect(Collectors.toList());
        return buttonList;
    }
}
