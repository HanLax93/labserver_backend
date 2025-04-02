package site.handglove.labserver.model;

import java.io.Serializable;
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
@TableName("imgs")
public class Img implements Serializable {
	private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("author")
    private String author;

    @TableField("filename")
    private String filename;

    @TableField("upload_time")
    private OffsetDateTime uploadTime;
}
