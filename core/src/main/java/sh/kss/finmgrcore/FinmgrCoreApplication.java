package sh.kss.finmgrcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sh.kss.finmgrlib.operation.Operation;

@SpringBootApplication(scanBasePackages = "sh.kss")
@RestController
public class FinmgrCoreApplication {

    private final Operation operation;

    private HomeResponse defaultResponse = new HomeResponse("hello world");

    public FinmgrCoreApplication(Operation operation) {
        this.operation = operation;
    }

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
