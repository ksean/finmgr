package sh.kss.finmgrcore;

import sh.kss.finmgrlib.Operations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "sh.kss")
@RestController
public class FinmgrCoreApplication {

    private final Operations operations;

    public FinmgrCoreApplication(Operations operations) {
        this.operations = operations;
    }

    @GetMapping("/")
    public String home() {
        return "hello world";
    }

    public static void main(String[] args) {
        SpringApplication.run(FinmgrCoreApplication.class, args);
    }
}