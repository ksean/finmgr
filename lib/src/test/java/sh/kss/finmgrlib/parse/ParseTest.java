package sh.kss.finmgrlib.parse;

import org.springframework.stereotype.Service;
import sh.kss.finmgrlib.FinmgrTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


@Service
public class ParseTest extends FinmgrTest {


    protected List<String> getLinesFromFile(File file) {

        List<String> lines = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {

                lines.add(scanner.nextLine());
            }
        }
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }

        return lines;
    }
}
