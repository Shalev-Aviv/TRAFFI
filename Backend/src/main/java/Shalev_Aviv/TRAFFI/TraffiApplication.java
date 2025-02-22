package Shalev_Aviv.TRAFFI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@SpringBootApplication
@RestController
public class TraffiApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TraffiApplication.class, args);
	}

	@GetMapping("/")
	public String index() {
		return "Connected to the server!";
	}
}