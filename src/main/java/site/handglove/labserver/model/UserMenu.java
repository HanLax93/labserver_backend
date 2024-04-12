package site.handglove.labserver.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("user_menu")
public class UserMenu {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("role_id")
    private Integer roleId;

    @TableField("menu_id")
    private Integer menuId;
}
