package Shalev_Aviv.TRAFFI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;

@SpringBootApplication
public class TraffiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TraffiApplication.class, args);
	}

	@PostMapping("/api/test")
	public String test() {
		return "Hello World";
	}

}
