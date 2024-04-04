package site.handglove.labserver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import site.handglove.labserver.result.Result;

import org.springframework.http.HttpStatus;

import java.io.IOException;

public class ResponseUtil {
    @SuppressWarnings("rawtypes")
    public static void out(HttpServletResponse response, Result r) {
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        try {
            mapper.writeValue(response.getWriter(), r);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
