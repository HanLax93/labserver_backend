package site.handglove.labserver.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import site.handglove.labserver.mapper.QuestionMapper;
import site.handglove.labserver.model.Question;
import site.handglove.labserver.service.QuestionService;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    
}
