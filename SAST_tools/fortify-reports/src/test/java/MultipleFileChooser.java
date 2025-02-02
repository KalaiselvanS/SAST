import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import com.dhl.tools.fortify.reports.Messages;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

public class MultipleFileChooser extends JFrame {
    public static void main(String[] args) {
        MultipleFileChooser multipleFileChooser = new MultipleFileChooser();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        FileFilter fprFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "Select FPR files";
			}
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".fpr");
			}
		};
		fileChooser.setFileFilter(fprFilter);
        JButton button = new JButton("Open Files");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = fileChooser.showOpenDialog(multipleFileChooser);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File[] openFiles = fileChooser.getSelectedFiles();
                    System.out.println("Files: " + Arrays.toString(openFiles));
                }
            }
        });
        JPanel panel = new JPanel();
        panel.add(button);
        multipleFileChooser.add(panel);
        multipleFileChooser.setSize(new Dimension(400, 400));
        multipleFileChooser.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        multipleFileChooser.setVisible(true);
    }
}