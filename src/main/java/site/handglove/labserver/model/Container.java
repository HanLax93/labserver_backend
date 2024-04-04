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
@TableName("container")
public class Container {
    @TableField("id")
    private Integer id;

    @TableField("docker_id")
    private String dockerID;

    @TableField("name")
    private String name;

    @TableField("port")
    private int port;

    @TableField(exist = false)
    private int running;

    @TableField(exist = false)
    private int sshStatus;

    @TableField("stu_index")
    private Integer stuIndex;

    @TableField("created_time")
    private OffsetDateTime createTime;

    @Override
    public String toString() {
        return "Container [dockerID=" + dockerID + ", createdTime=" + createTime + ", name=" + name + ", port=" + port
                + ", running=" + running + "]";
    }
}
