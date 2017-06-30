package model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Stachu on 28.02.2017.
 */
public class DiskFile extends DiskElement {

    public DiskFile(File file) {
        super(file);
    }

    protected void print(int depth) {
        String depthSigns = "";

        for (int i = 0; i < depth; i++) {
            depthSigns += " ";
        }
            depthSigns += "-";
        Date date = new Date(file.lastModified());
        SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = template.format(date);
        long fileSize = file.length();
        System.out.print(String.format("%-30s P %dMB \n", depthSigns + file.getName(), fileSize/(1024*1024)));
    }
}
