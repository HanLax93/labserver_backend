package site.handglove.labserver.model;

import java.time.OffsetDateTime;

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
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("username")
    private String username;

    @TableField("name")
    private String name;

    @TableField("entry_year")
    private Integer entryYear;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("permission")
    private Integer permission;

    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("created_at")
    private OffsetDateTime createdAt;

    @TableField("updated_at")
    private OffsetDateTime updatedAt;

    public User(String username, String passwordHash, String name, Integer entryYear) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.entryYear = entryYear;
    }
}
