package site.handglove.labserver.model.vo;

import java.util.List;

import lombok.Data;
import site.handglove.labserver.model.Announcement;
import site.handglove.labserver.model.Img;

@Data
public class AnnouncementVo {
    private List<String> imgs;

    private List<Img> removal;

    private Announcement announcement;
}
