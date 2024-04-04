package site.handglove.labserver.model;

import java.time.OffsetDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("container_task")
public class ContainerTask {
    @TableField("id")
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("is_processed")
    private int isProcessed;

    @TableField("create_time")
    private OffsetDateTime createTime;

    @TableField("update_time")
    private OffsetDateTime updateTime;
}
