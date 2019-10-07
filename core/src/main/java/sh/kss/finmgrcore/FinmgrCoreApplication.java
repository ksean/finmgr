package sh.kss.finmgrcore;

import com.google.common.collect.ImmutableList;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sh.kss.finmgrlib.entity.transaction.InvestmentTransaction;

import java.util.Collections;


@SpringBootApplication(scanBasePackages = "sh.kss")
@RestController
public class FinmgrCoreApplication {

    private HomeResponse defaultResponse = new HomeResponse("hello world");
    private TransactionsResponse noTransactions = new TransactionsResponse(ImmutableList.of());

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/")
    @ResponseBody
    public HomeResponse home() {
        return defaultResponse;
    }

    @CrossOrigin(origins = "http://localhost:3000/transactions")
    @GetMapping("/transactions")
    @ResponseBody
    public TransactionsResponse transactions() {
        return noTransactions;
    }

    public static void main(String[] args) {
        SpringApplication.run(FinmgrCoreApplication.class, args);
    }
}
