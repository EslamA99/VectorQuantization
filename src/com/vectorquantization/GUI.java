package com.vectorquantization;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GUI extends JFrame {
    private JTextField boockW;
    private JTextField bookH;
    private JTextField codeBookLength;
    private JButton compressImageButton;
    private JButton deCompressImageButton;
    /*private JTextField compressArea;
    private JTextField deCompressArea;
    private JButton browseButton;
    private JButton browseButton1;*/
    private JPanel panel1;
    private JLabel showImg;

    public GUI() {
        setTitle("Arithmetic Coding");
        setSize(800, 600);
        add(panel1);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        btnActions();


    }

    private void btnActions() {
        compressImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png"));
                chooser.showOpenDialog(null);
                File f = chooser.getSelectedFile();
                if (f != null) {
                    VectorQuantization vectorQuantization = new VectorQuantization();

                    try {
                        int width = Integer.parseInt(boockW.getText());
                        int height = Integer.parseInt(bookH.getText());
                        int length = Integer.parseInt(codeBookLength.getText());
                        vectorQuantization.compress(width, height, length, f.getPath());
                        showImg.setIcon(new ImageIcon(new ImageIcon(f.getPath()).getImage().getScaledInstance(600, 450, Image.SCALE_DEFAULT)));
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "Enter Valid Data!");
                    }

                }

            }
        });
        deCompressImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                chooser.showOpenDialog(null);
                File f = chooser.getSelectedFile();
                if (f != null) {
                    VectorQuantization vectorQuantization = new VectorQuantization();
                    vectorQuantization.deCompress(f.getPath());
                    Path p = Paths.get(f.getPath());
                    String name = p.getFileName().toString();
                    name = name.substring(0, name.lastIndexOf('.'));
                    showImg.setIcon(new ImageIcon(new ImageIcon("DeCompressed_"+name).getImage().getScaledInstance(500, 450, Image.SCALE_DEFAULT)));
                }
            }
        });

    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUI gui = new GUI();
                gui.setVisible(true);
            }
        });
    }
}
