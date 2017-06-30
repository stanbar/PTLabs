import model.DiskDirectory;
import model.DiskElement;
import model.DiskFile;

import java.io.File;

/**
 * Created by Stachu on 28.02.2017.
 */

public class Main {

    private static boolean sortable;

    public static void main(String[] args) {
        String path = args[0];

        sortable = false;

        File file = new File(path);
        DiskDirectory diskDirectory = new DiskDirectory(file, sortable);
        createTree(file.listFiles(), diskDirectory);
        diskDirectory.print();
    }

    private static void createTree(File[] filesArg, DiskDirectory diskDirectory) {
        for (File file : filesArg) {

            boolean isDirectory = file.isDirectory();

            DiskElement diskElement;

            if (isDirectory) {
                diskElement = new DiskDirectory(file, sortable);
                createTree(file.listFiles(), (DiskDirectory) diskElement);
            } else
                diskElement = new DiskFile(file);

            diskDirectory.addChild(diskElement);

        }
    }
}
