package site.handglove.labserver.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import site.handglove.labserver.mapper.MenuMapper;
import site.handglove.labserver.model.Menu;
import site.handglove.labserver.service.MenuService;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService  {

}
