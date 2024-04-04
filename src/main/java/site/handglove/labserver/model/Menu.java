package site.handglove.labserver.model;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("menu")
public class Menu {
    @TableField("id")
    private Integer id;

	@TableField("parent_id")
	private Integer parentId;

	@TableField("title")
	private String name;

	@TableField("type")
	private Integer type;

	@TableField("path")
	private String path;

	@TableField("component")
	private String component;

	@TableField("auth")
	private String perms;

	@TableField("icon")
	private String icon;

	@TableField("sort_value")
	private Integer sortValue;

	@TableField("status")
	private Integer status;

	// 下级列表
	@TableField(exist = false)
	private List<Menu> children;
	//是否选中
	@TableField(exist = false)
	private boolean isSelect;
}
