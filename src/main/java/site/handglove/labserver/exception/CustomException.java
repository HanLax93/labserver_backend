package site.handglove.labserver.exception;

import org.springframework.security.core.AuthenticationException;

import com.alibaba.fastjson2.JSON;
import com.github.dockerjava.api.exception.DockerException;

public class CustomException extends AuthenticationException {
    public CustomException(String msg) {
        super(msg);
    }

    public static String parseDockerExceptionMessage(DockerException ex) {
        String exceptionMessage = ex.getMessage();
        String jsonResponse = exceptionMessage.substring(exceptionMessage.indexOf("{"));
        String errorMessage = JSON.parseObject(jsonResponse).getString("message");
        return errorMessage;
    }
}
