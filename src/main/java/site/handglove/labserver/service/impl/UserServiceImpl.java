package site.handglove.labserver.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import site.handglove.labserver.mapper.UserMapper;
import site.handglove.labserver.model.User;
import site.handglove.labserver.service.UserService;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

}
