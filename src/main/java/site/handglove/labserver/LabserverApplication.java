package site.handglove.labserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("site.handglove.labserver.mapper")
public class LabserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(LabserverApplication.class, args);
	}

}
