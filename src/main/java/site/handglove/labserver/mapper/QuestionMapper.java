package site.handglove.labserver.mapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import site.handglove.labserver.model.Question;

@Repository
public interface QuestionMapper extends BaseMapper<Question> {
    
}
