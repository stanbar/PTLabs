package model;

import java.io.File;
import java.util.*;

/**
 * Created by Stachu on 28.02.2017.
 */
public class DiskDirectory extends DiskElement {

    private Set<DiskElement> children;

    public DiskDirectory(File file, boolean sortable) {
        super(file);
        if (sortable)
            children = new TreeSet<>((o1, o2) -> o1.file.getName().compareToIgnoreCase(o2.file.getName()));
        else
            children = new TreeSet<>((o1, o2) -> {
                return (int) (o1.file.length() - o2.file.length());
            });
    }


    protected void print(int depth) {
        String depthSigns = "";

        for (int i = 0; i < depth; i++) {
            depthSigns += " ";
        }
        depthSigns += " \\";
        Date date = new Date(file.lastModified());
        long fileSize = file.length();

        System.out.print(String.format("%-30s K %dMB \n", depthSigns + file.getName(), fileSize/(1024*1024)));

        for (DiskElement child : children) {
            child.print(depth + 1);
        }
    }

    public void addChild(DiskElement child) {
        children.add(child);
    }
}
