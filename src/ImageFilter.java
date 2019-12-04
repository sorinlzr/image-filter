import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.*;

public class ImageFilter extends JFrame implements ActionListener {

    private Image sourceImage;
    private PlugInFilter filter;
    private Image filteredImage;
    private LoadedImage loadedImage;
    private JLabel label;
    private JButton reset;
    private JButton save;
    private JMenuBar menuBar;
    private JMenu file;
    private JMenuItem open;
    private JMenuItem saveAs;
    private JMenuItem exit;


    String[] filters = {"Grayscale", "Invert", "Contrast", "Blur", "Sharpen"};

    public ImageFilter() {
        setTitle("Image Filter");
        setSize(new Dimension(800, 600));

        menuBar = new JMenuBar();
        file = new JMenu("File");
        open = new JMenuItem("Open");
        open.addActionListener(e-> openFile());
        saveAs = new JMenuItem("Save As");
        saveAs.addActionListener(e->this.saveFile((this.filteredImage==null)?this.sourceImage:this.filteredImage));
        exit = new JMenuItem("Exit");
        exit.addActionListener(e->System.exit(0));

        file.add(open);
        file.add(saveAs);
        file.add(exit);
        menuBar.add(file);
        setJMenuBar(menuBar);

        Panel panel = new Panel();
        add(panel, BorderLayout.SOUTH);

        reset = new JButton("Reset");
        reset.addActionListener(this);
        panel.add(reset);

        for (String filter : filters) {
            JButton b = new JButton(filter);
            b.addActionListener(this);
            panel.add(b);
        }

        save = new JButton("Save As");
        save.addActionListener(e -> this.saveFile((this.filteredImage==null)?this.sourceImage:this.filteredImage));
        panel.add(save);

        label = new JLabel("");
        add(label, BorderLayout.NORTH);

        openFile();

        loadedImage = new LoadedImage(sourceImage);
        add(loadedImage, BorderLayout.CENTER);

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    private Image imageResize(Image image) {
        int imgWidth, imgHeight;
        double contRatio = (double) this.getWidth() / (double) this.getHeight();
        double imgRatio = (double) image.getWidth(this) / (double) image.getHeight(this);

        if (contRatio < imgRatio) {
            imgWidth = this.getWidth();
            imgHeight = (int) (this.getWidth() / imgRatio);

        } else {
            imgWidth = (int) (this.getHeight() * imgRatio);
            imgHeight = this.getHeight();
        }

        return image.getScaledInstance(imgWidth, imgHeight, Image.SCALE_AREA_AVERAGING);

    }

    public void actionPerformed(ActionEvent ae) {
        String action = "";

        try {
            action = ae.getActionCommand();
            if (action.equals("Reset")) {
                loadedImage.set(sourceImage);
                label.setText("Normal");
            } else {
                filter = (PlugInFilter) (Class.forName(action)).getConstructor().newInstance();
                filteredImage = filter.filter(this, sourceImage);
                loadedImage.set(filteredImage);
                label.setText("Filtered: " + action);
            }
            repaint();

        } catch (ClassNotFoundException e) {
            label.setText(action + " not found");
            loadedImage.set(sourceImage);
            repaint();
        } catch (InstantiationException e) {
            label.setText("couldn't new " + action);
        } catch (IllegalAccessException e) {
            label.setText("No access: " + action);
        } catch (NoSuchMethodException | InvocationTargetException e) {
            label.setText("Filter creation error: " + action);
        }
    }

    private void openFile(){
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "tif"));
            chooser.setAcceptAllFileFilterUsed(false);
            int choice = chooser.showOpenDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION){
                File imageFile = chooser.getSelectedFile();
                this.sourceImage = ImageIO.read(imageFile);
                this.sourceImage = imageResize(this.sourceImage);
                if (this.loadedImage==null){
                    loadedImage = new LoadedImage(this.sourceImage);
                }else {
                    loadedImage.set(sourceImage);
                }
                this.repaint();
            }
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Can't load image");
        }
    }

    private void saveFile(Image image) {
        BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(this),
                image.getHeight(this),
                BufferedImage.TYPE_INT_RGB);

        Graphics g = bufferedImage.createGraphics();
        g.drawImage(image, 0, 0, this);
        g.dispose();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("*.png", "png"));

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIO.write(bufferedImage, "png", new File(file.getAbsolutePath() + ".png"));
            } catch (IOException ex) {
                System.out.println("Failed to save image!");
            }
        } else {
            System.out.println("No file chosen!");
        }
    }

    public static void main(String[] args) {
        new ImageFilter();

    }


}
