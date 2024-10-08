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
package sh.kss.finmgr.core;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the correctness of the finmgr core API
 *
 */
@SpringBootTest
public class FinmgrCoreApplicationTests {

    private static final Logger LOG = LoggerFactory.getLogger(FinmgrCoreApplicationTests.class);

    @Autowired
    private FileUploadController fileUploadController;

    /**
     * Test that the Spring context can load
     *
     */
    @Test
    public void contextLoads() {
        LOG.info("Spring context loaded");
        assertThat(fileUploadController).isNotNull();
    }
}
