package pl.skf.sws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SwsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwsApplication.class, args);
	}

}
