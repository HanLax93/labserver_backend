package site.handglove.labserver.model;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stu {
    @TableField("id")
    private Integer id;
    
    @TableField("name")
    private String name;

    @TableField("stu_index")
    private Integer stuIndex;

    @TableField("is_admin")
    private Boolean isAdmin;

    public Stu(String name, Integer stuIndex) {
        this.name = name;
        this.stuIndex = stuIndex;
    }

    @Override
    public String toString() {
        return "Stu [name = " + this.name + ", stu index = " + this.stuIndex + "]";
    }
}
