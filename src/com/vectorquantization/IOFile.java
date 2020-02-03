package com.vectorquantization;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class IOFile implements Serializable {
    int numOfColumns;
    ArrayList<int[][]> codeBook;
    ArrayList<String> imgCode;

    public IOFile(int numOfColumns, ArrayList<int[][]> codeBook, ArrayList<String> imgCode) {
        this.numOfColumns = numOfColumns;
        this.codeBook = codeBook;
        this.imgCode = imgCode;
    }

    public static IOFile ReadFromFile(String path) throws IOException, ClassNotFoundException {
        // Read objects
        FileInputStream fi = new FileInputStream(new File(path));
        ObjectInputStream oi = new ObjectInputStream(fi);
        IOFile ioFile = (IOFile) oi.readObject();
        oi.close();
        fi.close();
        return ioFile;
    }

    public void writToFile(String path) throws IOException {
        FileOutputStream f = new FileOutputStream(new File(path));
        ObjectOutputStream o = new ObjectOutputStream(f);
        // Write objects to file
        o.writeObject(this);
        o.close();
        f.close();
    }
}
