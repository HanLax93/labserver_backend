package site.handglove.labserver.model;

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
@AllArgsConstructor
@NoArgsConstructor
@TableName("questions")
public class Question {
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

    @TableField(exist = false)
    private List<Img> ansImgs;

    @TableField("ans_description")
    private String ansDescription;

    @TableField("version")
    private Integer version;

    @TableField("for_admin")
    private Integer forAdmin;

    @TableField("create_time")
    private OffsetDateTime createTime;

    @TableField(value = "update_time", update = "now()")
    private OffsetDateTime updateTime;
}
