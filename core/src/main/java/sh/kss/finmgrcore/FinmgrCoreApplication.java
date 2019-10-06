package sh.kss.finmgrcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication(scanBasePackages = "sh.kss")
@RestController
public class FinmgrCoreApplication {

    private HomeResponse defaultResponse = new HomeResponse("hello world");

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/")
    @ResponseBody
    public HomeResponse home() {
        return defaultResponse;
    }

    public static void main(String[] args) {
        SpringApplication.run(FinmgrCoreApplication.class, args);
    }
}
