/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package abc;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;


/**
 *
 * @author OldSpice
 */
public class TestWebCam extends JPanel implements ActionListener {

    private BufferedImage image;
    private JButton button = new JButton("capture");
    int count = 1;

    public TestWebCam() {
        super();
        button.addActionListener((ActionListener) this);
        this.add(button);
    }

    private BufferedImage getimage() {
        return image;
    }

    private void setimage(BufferedImage newimage) {
        image = newimage;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.image == null) {
            return;
        }
        g.drawImage(this.image, 0, 0, this.image.getWidth(), this.image.getHeight(), null);
    }

    public static void main(String args[]) throws Exception {
        int foundImage = -1;
        JFrame frame = new JFrame("Face Recognizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 380);
        System.loadLibrary("opencv_java2413");
        //CascadeClassifier faceDetector=new CascadeClassifier("C:\\opencv\\sources\\data\\lbpcascades\\lbpcascade_silverware.xml");
        //CascadeClassifier faceDetector=new CascadeClassifier("C:\\opencv\\sources\\data\\lbpcascades\\lbpcascade_profileface.xml");
        TestWebCam toc = new TestWebCam();

        frame.add(toc);;
        frame.setVisible(true);
        Mat webcam_image = new Mat();
        MatToBufImg mat2Buf = new MatToBufImg();
        VideoCapture capture = null;
        try {
            capture = new VideoCapture(0);
        } catch (Exception xx) {
            xx.printStackTrace();
        }
        if (capture.open(0)) { //open(0) opens your Laptop webcam, open(1) opens USB attached Webcam
            while (true) {
                capture.read(webcam_image);
                if (!webcam_image.empty()) {
                   
                    String thisPath = TestWebCam.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                    thisPath = thisPath.substring(1, thisPath.length());
                    thisPath = thisPath.replace("/", "\\");

                    Mat templ = Highgui.imread(thisPath+"abc\\Roman_3.jpg");
                    Mat[] templates = new Mat[5];
                    for(int count = 0; count < 5; count++){
                        templates[count] = Highgui.imread(thisPath+"abc\\Roman_"+(count+1)+".jpg");
                    }
                    
                    int match_method = Imgproc.TM_SQDIFF;
                    // / Create the result matrix
                    int result_cols = webcam_image.cols() - templ.cols() + 1;
                    int result_rows = webcam_image.rows() - templ.rows() + 1;
                    Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

                   // / Do the Matching
                   //Imgproc.matchTemplate(webcam_image, templates[3], result, match_method);
                   // Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
                   int counter = 1;
                   int newFoundImage = 0;
                   for(Mat p : templates){
                       Imgproc.matchTemplate(webcam_image, p, result, match_method);
                       MinMaxLocResult mmr_Array = Core.minMaxLoc(result);
                       Point matchLoc_Array;
                        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
                           matchLoc_Array = mmr_Array.minLoc;
                        } else {
                           matchLoc_Array = mmr_Array.maxLoc;
                        }
                        double threshold = 1.0 * Math.pow(10, 9); 
                        
                        if((double)(mmr_Array.minVal) < (double)(threshold)){
                            
                           Core.rectangle(webcam_image, matchLoc_Array, new Point(matchLoc_Array.x + templ.cols(),
                           matchLoc_Array.y + templ.rows()), new Scalar(0, 255, 0));
                           newFoundImage = counter;
                        }
                        counter++;
                   }
                   
                    if(foundImage != newFoundImage){
                        if(newFoundImage != 0){
                            System.out.println("new found Roman "+ newFoundImage);
                        }
                        foundImage = newFoundImage;
                    }
                   
                   // / Localizing the best match with minMaxLoc
                    /*MinMaxLocResult mmr = Core.minMaxLoc(result);

                    Point matchLoc;
                    if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
                       matchLoc = mmr.minLoc;
                    } else {
                       matchLoc = mmr.maxLoc;
                    }
                    double threshold = 1.0 * Math.pow(10, 9); 
                    //System.out.println("minLoc: " + mmr.minLoc + "\t maxLoc: " + mmr.maxLoc + " || \t minVal: " + mmr.minVal + "\t maxVal: "+ mmr.maxVal);

                    if((double)(mmr.minVal) < (double)(threshold)){
                        System.out.println("found III");
                    }
                   // / Show me what you got

                    */
                    mat2Buf.setMatrix(webcam_image, ".jpg");
                    toc.setimage(mat2Buf.getBufferedImage());
                    toc.repaint();
                } else {
                    System.out.println("problems with webcam image capture");
                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException ex){
                        System.out.println(ex.getMessage());
                    }
                }
            }
        }
        capture.release();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String ans = JOptionPane.showInputDialog(null, "Color/Grey");
        System.out.println(ans);
        BufferedImage bi = image;
        ImageIcon ii = null;
        ii = new ImageIcon(bi);
        Image newimg = bi.getScaledInstance(320, 220, java.awt.Image.SCALE_SMOOTH);
        ii = new ImageIcon(newimg);
        Image i2 = ii.getImage();
        image = new BufferedImage(i2.getWidth(null), i2.getHeight(null), BufferedImage.SCALE_SMOOTH);
        image.getGraphics().drawImage(i2, 0, 0, null);
        RenderedImage rendered = null;
        if (i2 instanceof RenderedImage) {
            rendered = (RenderedImage) i2;
        } else {
            BufferedImage buffered = null;
            if (!ans.equalsIgnoreCase("color")) {
                buffered = new BufferedImage(
                        ii.getIconWidth(),
                        ii.getIconHeight(),
                        BufferedImage.TYPE_BYTE_GRAY);
            } else {
                buffered = new BufferedImage(
                        ii.getIconWidth(),
                        ii.getIconHeight(),
                        BufferedImage.SCALE_SMOOTH);
            }
            Graphics2D g = buffered.createGraphics();
            g.drawImage(i2, 0, 0, null);
            g.dispose();
            rendered = buffered;
        }
        try {
            ImageIO.write(rendered, "JPEG", new File("D:\\test\\saved.jpg"));
        } catch (Exception ex) {
        }
    }
}