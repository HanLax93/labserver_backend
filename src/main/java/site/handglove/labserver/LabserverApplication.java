package site.handglove.labserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PreDestroy;
import redis.clients.jedis.Jedis;

@SpringBootApplication
@MapperScan("site.handglove.labserver.mapper")
public class LabserverApplication {
	@Value("${spring.data.redis.password}")
	private String password;

	public static void main(String[] args) {
		SpringApplication.run(LabserverApplication.class, args);
	}

	@PreDestroy
	public void destroy() {
		Jedis jedis = new Jedis();
		jedis.auth(password);
		jedis.flushAll();
		jedis.close();
	}
}
