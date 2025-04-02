package site.handglove.labserver.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import site.handglove.labserver.mapper.ImgMapper;
import site.handglove.labserver.model.Img;
import site.handglove.labserver.service.ImgService;

@Service
public class ImgServiceImpl extends ServiceImpl<ImgMapper, Img> implements ImgService {
    // @SelectKey(keyColumn = "id",keyProperty = "id",before = false,resultType =Integer.class,statement = {" select last_insert_id()"})
    public Integer saveImg(String author, String filename) {
        Img img = new Img();
        img.setAuthor(author);
        img.setFilename(filename);
        baseMapper.insert(img);
        return img.getId();
    }
}
