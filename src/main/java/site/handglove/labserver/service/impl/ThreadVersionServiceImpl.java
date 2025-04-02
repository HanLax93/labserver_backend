package site.handglove.labserver.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import site.handglove.labserver.mapper.ThreadVersionMapper;
import site.handglove.labserver.model.ThreadVersion;
import site.handglove.labserver.service.ThreadVersionService;

@Service
public class ThreadVersionServiceImpl extends ServiceImpl<ThreadVersionMapper, ThreadVersion> implements ThreadVersionService {
    
}
