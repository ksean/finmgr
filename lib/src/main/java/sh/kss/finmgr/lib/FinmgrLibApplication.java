/*
    finmgr - A financial transaction framework
    Copyright (C) 2024 Kennedy Software Solutions Inc.

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
package sh.kss.finmgr.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The library of entities and useful functions for the framework objects
 *
 */
@SpringBootApplication
public class FinmgrLibApplication {

    private static final Logger LOG = LoggerFactory.getLogger(FinmgrLibApplication.class);

    /**
     * A spring application context so we can easily inject services and other components
     *
     * @param args launch arguments
     */
    public static void main(String[] args) {

        LOG.info("Start Spring application context");
        SpringApplication.run(FinmgrLibApplication.class, args);
    }
}
