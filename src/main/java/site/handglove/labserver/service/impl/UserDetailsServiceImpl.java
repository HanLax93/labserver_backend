package site.handglove.labserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.ArrayList;
import java.util.List;

import site.handglove.labserver.exception.CustomException;
import site.handglove.labserver.model.User;
import site.handglove.labserver.security.custom.CustomUser;
import site.handglove.labserver.security.custom.UserDetailsService;
import site.handglove.labserver.service.UserService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userService.getOne(queryWrapper);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        if (user.getIsDeleted() == 1) {
            throw new CustomException("用户已禁用，请更换用户名重新注册");
        }

        List<SimpleGrantedAuthority> permission = new ArrayList<>();
        if (user.getPermission() == 1) {
            permission.add(new SimpleGrantedAuthority("admin"));
            permission.add(new SimpleGrantedAuthority("user"));
        } else {
            permission.add(new SimpleGrantedAuthority("user"));
        }
        return new CustomUser(user, permission);
    }
}
