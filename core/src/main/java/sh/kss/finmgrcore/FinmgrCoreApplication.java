/*
    finmgr - A financial transaction framework
    Copyright (C) 2021 Kennedy Software Solutions Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package sh.kss.finmgrcore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sh.kss.finmgrcore.storage.StorageProperties;
import sh.kss.finmgrcore.storage.StorageService;
import sh.kss.finmgrlib.parse.Parser;

/**
 * A Java Spring web API wrapping the finmgr library project
 *
 */
@SpringBootApplication(scanBasePackages = "sh.kss")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@EnableConfigurationProperties(StorageProperties.class)
public class FinmgrCoreApplication {

    private static final Logger LOG = LogManager.getLogger(FinmgrCoreApplication.class);

    private final HomeResponse defaultResponse = new HomeResponse("hello world");

    /**
     *
     *
     * @return
     */
    @GetMapping("/")
    @ResponseBody
    public HomeResponse home() {
        LOG.debug("Received /");

        return defaultResponse;
    }

    /**
     *
     *
     * @return
     */
    @GetMapping("/transactions")
    @ResponseBody
    public TransactionsResponse transactions() {
        LOG.info("Received /transactions");

        return new TransactionsResponse(Parser.traversePath("upload-dir"));
    }

    /**
     *
     *
     * @param args
     */
    public static void main(String[] args) {

        LOG.debug("Starting Spring FinmgrCoreApplication context");
        SpringApplication.run(FinmgrCoreApplication.class, args);
    }

    /**
     *
     *
     * @param storageService
     * @return
     */
    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.init();
        };
    }
}
