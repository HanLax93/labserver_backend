package site.handglove.labserver.security.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import site.handglove.labserver.model.LoginVo;
import site.handglove.labserver.result.Result;
import site.handglove.labserver.utils.JWTHelper;
import site.handglove.labserver.utils.ResponseUtil;
import site.handglove.labserver.security.custom.CustomUser;

public class TokenLoginFilter extends UsernamePasswordAuthenticationFilter {
    private RedisTemplate<String, String> redisTemplate;
    // 构造方法
    public TokenLoginFilter(AuthenticationManager authenticationManager, RedisTemplate<String, String> redisTemplate) {
        this.setAuthenticationManager(authenticationManager);
        this.setPostOnly(false);
        // 指定登录接口及提交方式，可以指定任意路径
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", "POST"));
        this.redisTemplate = redisTemplate;
    }

    // 登录认证
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 获取请求中的用户名和密码
        try {
            // 获取用户信息
            LoginVo user = new ObjectMapper().readValue(request.getInputStream(), LoginVo.class);
            // 将用户信息封装到token中
            Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            // 调用AuthenticationManager的authenticate方法进行认证
            return this.getAuthenticationManager().authenticate(authentication);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // 认证成功调用的方法
    // @SuppressWarnings("null")
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        // 获取当前用户
        CustomUser customUser = (CustomUser)authResult.getPrincipal();
        // 生成token
        String token = JWTHelper.createToken(customUser.getUser().getUsername());
        redisTemplate.opsForValue().set(customUser.getUser().getUsername(), JSON.toJSONString(customUser.getAuthorities()));
        // 将token返回给前端
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        
        ResponseUtil.out(response, Result.OK(map));
    }
    // 认证失败
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        if(failed.getCause() instanceof RuntimeException) {
            ResponseUtil.out(response, Result.FAIL().message(failed.getMessage()));
        } else {
            ResponseUtil.out(response, Result.FAIL().message("登录失败"));
        }
    }
}