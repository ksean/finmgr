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
package sh.kss.finmgr.lib.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.kss.finmgr.lib.FinmgrTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * A subset of tests that parse a file as part of the testing process
 *
 */
public abstract class ParseTest extends FinmgrTest {

    private static final Logger LOG = LoggerFactory.getLogger(FinmgrTest.class);

    /**
     * getLinesFromFile takes an input file and returns lines of text as a List of Strings using the Java Scanner class
     *
     * @param file a Java.io.File to be parsed
     * @return a list of the Strings found in the file
     */
    protected static List<String> getLinesFromFile(File file) {

        LOG.debug(String.format("getLinesFromFile for: %s", file.getAbsolutePath()));

        // Create an empty list to start
        List<String> lines = new ArrayList<>();

        try {
            // Define a new scanner for the file
            Scanner scanner = new Scanner(file);

            // While next lines exist, add them to the list
            while (scanner.hasNextLine()) {

                lines.add(scanner.nextLine());
            }
        }
        catch (FileNotFoundException fnfe) {
            // If the input file was invalid
            LOG.debug(String.format("FileNotFoundException for: %s", file.getAbsolutePath()));
            fnfe.printStackTrace();
        }

        return lines;
    }
}
