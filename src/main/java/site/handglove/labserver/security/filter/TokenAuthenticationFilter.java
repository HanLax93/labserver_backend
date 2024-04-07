package site.handglove.labserver.security.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alibaba.fastjson2.JSON;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import site.handglove.labserver.result.Result;
import site.handglove.labserver.security.custom.LoginUserHelper;
import site.handglove.labserver.utils.JWTHelper;
import site.handglove.labserver.utils.ResponseUtil;

@SuppressWarnings("null")
public class TokenAuthenticationFilter extends OncePerRequestFilter{
    private RedisTemplate<String, String> redisTemplate;

    public TokenAuthenticationFilter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 如果是登录，直接放行
        if ("/login".equals(request.getRequestURI()) || "/user/apply".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if (authentication != null && authentication.getAuthorities().size() != 0) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        }
        else {
            ResponseUtil.out(response, Result.FAIL().message("登录过期，请退出重新登录"));
        }
    }
    
    @SuppressWarnings("rawtypes")
    public UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        String token = request.getHeader("token");
        if (StringUtils.hasLength(token)) {
            // 解析token
            String username = JWTHelper.getUsername(token);
            if (StringUtils.hasLength(username)) {
                // 当前用户信息放到threadlocal里
                LoginUserHelper.setUsername(username);

                // 通过username从redis中获取权限数据
                String permission = (String) redisTemplate.opsForValue().get(username);
                if (StringUtils.hasLength(permission)) {
                    List<Map> maplist = JSON.parseArray(permission, Map.class);
                    authorities = maplist.stream().map(map -> new SimpleGrantedAuthority((String) map.get("authority")))
                            .collect(Collectors.toList());
                }
                // 把获取的数据（String）转换为集合类型
                return new UsernamePasswordAuthenticationToken(username, null, authorities);
            }
        }
        return null;
    }
}
