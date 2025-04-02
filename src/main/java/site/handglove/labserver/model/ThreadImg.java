package site.handglove.labserver.model;

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
@TableName("thread_imgs")
public class ThreadImg {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("thread_id")
    private Integer threadId;

    @TableField("img_id")
    private Integer imgId;

    @TableField("type")
    private Integer type;
}
