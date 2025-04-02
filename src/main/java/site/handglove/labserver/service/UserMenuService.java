package site.handglove.labserver.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import site.handglove.labserver.model.UserMenu;
import site.handglove.labserver.model.vo.RouterVo;

public interface UserMenuService extends IService<UserMenu> {
    List<RouterVo> getMenusByUsername(String username);

    List<String> getButtonsByUsername(String username);
}
