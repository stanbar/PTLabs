package model;

import java.io.File;

/**
 * Created by Stachu on 28.02.2017.
 */
public abstract class DiskElement {
    File file;

    protected abstract void print(int depth);

    public void print() {
        print(0);
    }

    DiskElement(File file) {
        this.file = file;
    }
}
