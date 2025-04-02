package site.handglove.labserver.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("announcements")
public class Announcement implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("author")
    private String author;

    @TableField("title")
    private String title;

    @TableField(exist = false)
    private List<Img> imgs;

    @TableField("description")
    private String description;

    @TableField("for_admin")
    private Integer forAdmin;

    @TableField("version")
    private Integer version;

    @TableField("create_time")
    private OffsetDateTime createTime;

    @TableField("update_time")
    private OffsetDateTime updateTime;
}
