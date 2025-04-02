package site.handglove.labserver.model;

import java.time.OffsetDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName("thread_version")
@Data
public class ThreadVersion {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("thread_name")
    private String threadName;

    @TableField("version")
    private Integer version;

    @TableField("create_time")
    private OffsetDateTime createTime;

    @TableField(value = "update_time", update = "now()")
    private OffsetDateTime updateTime;
}
