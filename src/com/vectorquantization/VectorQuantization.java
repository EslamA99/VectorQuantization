package com.vectorquantization;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class VectorQuantization {
    static Image img = new Image();
    public void deCompress(String path) {
        IOFile ioFile = null;
        try {
            ioFile = IOFile.ReadFromFile(path);
            int[][] image = deCode(ioFile);
            Path p = Paths.get(path);

            String name = p.getFileName().toString();
            name = name.substring(0, name.lastIndexOf('.'));
            writeImage(image, "DeCompressed_" + name);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "invalid path");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "invalid Class");
        }

    }

    private int[][] deCode(IOFile ioFile) {

        int bookW = ioFile.codeBook.get(0)[0].length;
        int bookH = ioFile.codeBook.get(0).length;

        int imgWidth = ioFile.numOfColumns * bookW;
        int imgHeight = (ioFile.imgCode.size() / ioFile.numOfColumns) * bookH;
        int image[][] = new int[imgHeight][imgWidth];
        int y = 0;
        int x = 0;
        int countRow = 0;
        for (int i = 0; i < ioFile.imgCode.size(); i++) {
            int index = Integer.parseInt(ioFile.imgCode.get(i), 2);
            int[][] currBlock = ioFile.codeBook.get(index);
            if (i % ioFile.numOfColumns == 0 && i != 0) {
                countRow += bookH;
                x = 0;
            }
            y = countRow;
            int tmp = x;
            for (int bookY = 0; bookY < bookH; bookY++) {
                for (int bookX = 0; bookX < bookW; bookX++) {
                    image[y][x] = currBlock[bookY][bookX];
                    x++;
                }
                x = tmp;
                y++;
            }
            x += bookW;
        }
        return image;
    }

    public void compress(int blockW, int blockH, int blockSize, String path) {
        convertImageToMatrix(path);
        convertImgToBlocks(blockW, blockH);

        ArrayList<String> imageCode = split(blockSize, blockW, blockH);
        IOFile ioFile = new IOFile(img.matrix.length / blockW, img.codeBook, imageCode);
        try {
            Path path1 = Paths.get(path);
            ioFile.writToFile(path1.getFileName() + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static ArrayList<String> split(int bookSize, int blockW, int blockH) {
        int numOfSplits = (int) Math.ceil(Math.log(bookSize) / Math.log(2));
        ArrayList<double[][]> willSplit = new ArrayList<>();
        willSplit.add(getAvgBlock(img.imgBlocks, blockW, blockH));
        ArrayList<int[][]> codeBook = new ArrayList<>();
        for (int i = 0; i < numOfSplits; i++) {
            codeBook.clear();
            for (double[][] tmpRoot : willSplit) {
                for (int sp = 0; sp < 2; sp++) {
                    int[][] tmp = new int[blockH][blockW];
                    for (int y = 0; y < blockH; y++) {
                        for (int x = 0; x < blockW; x++) {
                            if (sp == 0) {
                                tmp[y][x] = (int) Math.floor(tmpRoot[y][x]);
                            } else {
                                tmp[y][x] = (int) Math.ceil(tmpRoot[y][x]);
                            }
                        }
                    }
                    codeBook.add(tmp);
                }
            }
            willSplit.clear();
            willSplit = getNextsplitWithEcelidian(codeBook, blockW, blockH);
        }
        img.codeBook = codeBook;
        ArrayList<String> childrenPosWithCodeBook = morePerformance(blockW, blockH);


        return childrenPosWithCodeBook;
    }

    private static ArrayList<String> morePerformance(int blockW, int blockH) {
        ArrayList<String> childrenPosWithCode = new ArrayList<>();
        //for (int i = 0; i < img.imgBlocks.size(); i++) childrenPosWithCode.add("");
        for (int performance = 0; true; performance++) {
            ArrayList<double[][]> willSplit = new ArrayList<>();
            ArrayList<Integer> childrenCount = new ArrayList<>();
            for (int i = 0; i < img.codeBook.size(); i++) {
                childrenCount.add(0);
                willSplit.add(new double[blockW][blockH]);
            }
            ArrayList<Double> distance = new ArrayList<>();
            for (int[][] currImgBlock : img.imgBlocks) {
                distance.clear();
                for (int[][] currBook : img.codeBook) {
                    double sum = 0.0;
                    for (int y = 0; y < blockH; y++) {
                        for (int x = 0; x < blockW; x++) {
                            sum += Math.pow(currBook[y][x] - currImgBlock[y][x], 2);
                        }
                    }
                    distance.add(Math.sqrt(sum));
                }

                int minIndex = distance.indexOf(Collections.min(distance));
                if (performance == 5) {
                    childrenPosWithCode.add(Integer.toBinaryString(minIndex));
                }
                int children = childrenCount.get(minIndex);
                childrenCount.set(minIndex, children + 1);
                double[][] tmp = willSplit.get(minIndex);
                for (int y = 0; y < blockH; y++) {
                    for (int x = 0; x < blockW; x++) {
                        tmp[y][x] += currImgBlock[y][x];
                    }
                }
                willSplit.set(minIndex, tmp);
            }
            if (performance == 5) {
                return childrenPosWithCode;
            }
            for (int i = 0; i < willSplit.size(); i++) {
                double[][] tmp = willSplit.get(i);
                int[][] tmpp = new int[blockH][blockW];
                for (int y = 0; y < blockH; y++) {
                    for (int x = 0; x < blockW; x++) {
                        tmp[y][x] = tmp[y][x] / childrenCount.get(i);
                        tmpp[y][x] = (int) tmp[y][x];
                    }
                }
                img.codeBook.set(i, tmpp);
                willSplit.set(i, tmp);
            }
        }


    }

    private static ArrayList<double[][]> getNextsplitWithEcelidian(ArrayList<int[][]> codeBook, int blockW, int blockH) {
        ArrayList<double[][]> willSplit = new ArrayList<>();
        ArrayList<Integer> childrenCount = new ArrayList<>();
        for (int i = 0; i < codeBook.size(); i++) {
            childrenCount.add(0);
            willSplit.add(new double[blockW][blockH]);
        }
        ArrayList<Double> distance = new ArrayList<>();
        for (int[][] currImgBlock : img.imgBlocks) {
            distance.clear();
            for (int[][] currBook : codeBook) {
                double sum = 0.0;
                for (int y = 0; y < blockH; y++) {
                    for (int x = 0; x < blockW; x++) {
                        sum += Math.pow(currBook[y][x] - currImgBlock[y][x], 2);
                    }
                }
                distance.add(Math.sqrt(sum));
            }

            int minIndex = distance.indexOf(Collections.min(distance));
            int children = childrenCount.get(minIndex);
            childrenCount.set(minIndex, children + 1);
            double[][] tmp = willSplit.get(minIndex);
            for (int y = 0; y < blockH; y++) {
                for (int x = 0; x < blockW; x++) {
                    tmp[y][x] += currImgBlock[y][x];
                }
            }
            willSplit.set(minIndex, tmp);
        }
        for (int i = 0; i < willSplit.size(); i++) {
            double[][] tmp = willSplit.get(i);
            for (int y = 0; y < blockH; y++) {
                for (int x = 0; x < blockW; x++) {
                    tmp[y][x] = tmp[y][x] / childrenCount.get(i);
                }
            }
            willSplit.set(i, tmp);
        }
        return willSplit;
    }

    private static double[][] getAvgBlock(ArrayList<int[][]> blocks, int blockW, int blockH) {
        double[][] block = new double[blockH][blockW];
        for (int i = 0; i < blocks.size(); i++) {
            for (int y = 0; y < blockH; y++) {
                for (int x = 0; x < blockW; x++) {
                    block[y][x] += (blocks.get(i)[y][x] / (double) blocks.size());
                }
            }
        }
        return block;
    }

    private static void convertImgToBlocks(int bookW, int bookH) {
        handleImgWithBookSize(bookW, bookH);
        int imgWidth = img.matrix[0].length;
        int imgHeight = img.matrix.length;
        for (int imgY = 0; imgY < imgHeight; imgY += bookH) {
            for (int imgX = 0; imgX < imgWidth; imgX += bookW) {
                int[][] block = new int[bookH][bookW];
                int y = 0;
                int x = 0;

                for (int bookY = imgY; bookY < imgY + bookH; bookY++) {
                    for (int bookX = imgX; bookX < imgX + bookW; bookX++) {
                        block[y][x] = img.matrix[bookY][bookX];
                        x++;
                    }
                    x = 0;
                    y++;
                }
                img.imgBlocks.add(block);
            }
        }
    }

    private static void handleImgWithBookSize(int bookW, int bookH) {

        int imgWidth = img.matrix[0].length;
        int imgHeight = img.matrix.length;
        int oldWidth = imgWidth;
        int oldHeight = imgHeight;
        /*if (imgWidth % bookW != 0) imgWidth = ((imgWidth / bookW) + 1) * bookW;
        if (imgHeight % bookH != 0) imgHeight = ((imgHeight / bookH) + 1) * bookH;*/
        while (imgWidth % bookW != 0)
            imgWidth++;
        while (imgHeight % bookH != 0)
            imgHeight++;

        int[][] newMatrix = new int[imgHeight][imgWidth];

        for (int y = 0; y < oldHeight; y++) {
            for (int x = 0; x < oldWidth; x++) {
                newMatrix[y][x] = img.matrix[y][x];
            }
        }
        img.matrix = newMatrix;
    }

    private static void convertImageToMatrix(String filePath) {
        File f = new File(filePath); //image file path
        int[][] imageMAtrix = null;
        try {
            BufferedImage img = ImageIO.read(f);
            int oldW = img.getWidth();
            int oldH = img.getHeight();
            imageMAtrix = new int[oldH][oldW];
            for (int y = 0; y < oldH; y++) {
                for (int x = 0; x < oldW; x++) {
                    int p = img.getRGB(x, y);
                    int a = (p >> 24) & 0xff;
                    int r = (p >> 16) & 0xff;
                    int g = (p >> 8) & 0xff;
                    int b = p & 0xff;
                    //because in gray image r=g=b  we will select r
                    imageMAtrix[y][x] = r;

                    //set new RGB value
                    p = (a << 24) | (r << 16) | (g << 8) | b;
                    img.setRGB(x, y, p);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            JOptionPane.showMessageDialog(null, "image Not Found");

        }
        img.matrix = imageMAtrix;
    }

    public static void writeImage(int[][] imagePixels, String outPath) {
        int oldH = imagePixels.length;
        int oldW = imagePixels[0].length;
        BufferedImage img = new BufferedImage(oldW, oldH, BufferedImage.TYPE_3BYTE_BGR);

        for (int y = 0; y < oldH; y++) {
            for (int x = 0; x < oldW; x++) {

                int a = 255;
                int pix = imagePixels[y][x];
                int p = (a << 24) | (pix << 16) | (pix << 8) | pix;

                img.setRGB(x, y, p);

            }
        }
        File f = new File(outPath);
        try {
            ImageIO.write(img, "jpg", f);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "no such file or directory");

        }
    }


}
