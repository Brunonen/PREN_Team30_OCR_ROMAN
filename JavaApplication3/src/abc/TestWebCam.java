/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package abc;

import com.sun.javafx.geom.Vec3f;
import com.sun.javafx.geom.Vec4f;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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
        if (capture.open(1)) { //open(0) opens your Laptop webcam, open(1) opens USB attached Webcam
            while (true) {
                capture.read(webcam_image);
                if (!webcam_image.empty()) {
                    //templateMatchingWithRed(webcam_image, thisPath);
                    String thisPath = TestWebCam.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                    thisPath = thisPath.substring(1, thisPath.length());
                    thisPath = thisPath.replace("/", "\\");
                    //Mat test = detectRedRects(webcam_image);
                    Mat hsv_image = new Mat();
                    Imgproc.cvtColor(webcam_image, hsv_image, Imgproc.COLOR_BGR2HSV);

                    Mat lower_red_hue_range = new Mat();
                    Mat upper_red_hue_range = new Mat();

                    /*Core.inRange(hsv_image, new Scalar(0,100,100), new Scalar(1, 227, 255), lower_red_hue_range);
                    Core.inRange(hsv_image, new Scalar(160, 100, 100), new Scalar(179, 227, 255), upper_red_hue_range);*/

                    Core.inRange(hsv_image, new Scalar(0,100,100), new Scalar(10, 255, 255), lower_red_hue_range);
                    Core.inRange(hsv_image, new Scalar(160, 100, 100), new Scalar(179, 255, 255), upper_red_hue_range);

                    Mat red_hue_image = new Mat();
                    Core.addWeighted(lower_red_hue_range, 1.0, upper_red_hue_range, 1.0, 0.0, red_hue_image);

                    Imgproc.GaussianBlur(red_hue_image, red_hue_image, new Size(9, 9), 2, 2);

                    List<MatOfPoint> contours = new ArrayList<>();
                    Mat hirarchy = new Mat();

                    Imgproc.findContours(red_hue_image, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

                    MatOfPoint2f approxCurve = new MatOfPoint2f();
                     int x = 1;
                     int barCount = 0;
                     //For each contour found
                     for (int i=0; i<contours.size(); i++)
                     {
                         //Convert contours(i) from MatOfPoint to MatOfPoint2f
                         MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(i).toArray() );
                         //Processing on mMOP2f1 which is in type MatOfPoint2f
                         double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
                         Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                         //Convert back to MatOfPoint
                         MatOfPoint points = new MatOfPoint( approxCurve.toArray() );

                         // Get bounding rect of contour
                         Rect rect = Imgproc.boundingRect(points);
                        if(rect.height > 50 ) {
                          // draw enclosing rectangle (all same color, but you could use variable i to make them unique)
                            barCount++; 
                        }
                         
                     }
                     
                    if(barCount > 2){
                        System.out.println("Found Bars");
                        try{
                            ITesseract instance = new Tesseract();

                            MatToBufImg webcamImageBuff = new MatToBufImg();
                            webcamImageBuff.setMatrix(webcam_image, ".jpg");
                            String result = instance.doOCR(webcamImageBuff.getBufferedImage());
                            System.out.println("found: " + result);
                        }catch(Exception e){
                           // System.err.println(e.getMessage());
                        }
/*
                        Mat[] templates = new Mat[5];
                        for(int count = 0; count < 5; count++){
                            templates[count] = Highgui.imread(thisPath+"abc\\Roman_"+(count+1)+".jpg");
                        }
                        
                        int match_method = Imgproc.TM_SQDIFF;


                       // / Do the Matching
                       //Imgproc.matchTemplate(webcam_image, templates[3], result, match_method);
                       // Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
                       int counter = 1;

                       double[] match_Array = new double[5];
                       for(Mat p : templates){
                            // / Create the result matrix
                            int result_cols = webcam_image.cols() - p.cols() + 1;
                            int result_rows = webcam_image.rows() - p.rows() + 1;
                            Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
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

                               Core.rectangle(webcam_image, matchLoc_Array, new Point(matchLoc_Array.x + p.cols(),
                               matchLoc_Array.y + p.rows()), new Scalar(0, 255, 5));
                               //newFoundImage = counter;
                               match_Array[counter-1] = mmr_Array.minVal;
                            }
                            counter++;
                       }
                       for(double d : match_Array){
                           System.out.print(d + " : ");
                       }
                        int newFoundImage = 0;
                        double highestMatch = match_Array[0];
                        for( int count  = 0; count < match_Array.length; count++){
                            System.out.print(" : " + match_Array[count]);
                            if(match_Array[count] < highestMatch){
                                highestMatch = match_Array[count];
                                newFoundImage = count + 1;
                            }
                        }
                        System.out.print("\n");
                        
                        if(foundImage != newFoundImage){
                            if(newFoundImage != 0){
                                System.out.println("new found Roman "+ newFoundImage);
                            }
                            foundImage = newFoundImage;
                        } */
                    }
                    //Imgproc.drawContours(webcam_image, contours, -1, new Scalar(255,255,0));
                    
                    //Mat templ = Highgui.imread(thisPath+"abc\\Roman_3.jpg");
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

    private static void templateMatchingWithRed(Mat webcam_image, String thisPath){
      
        Mat templ = Highgui.imread(thisPath+"abc\\Red_Bars.png");
        
        int match_method = Imgproc.TM_SQDIFF;
        // / Create the result matrix
        int result_cols = webcam_image.cols() - templ.cols() + 1;
        int result_rows = webcam_image.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
        
        Imgproc.matchTemplate(webcam_image, templ, result, match_method);
        MinMaxLocResult mmr_Array = Core.minMaxLoc(result);
        Point matchLoc_Array;
         if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc_Array = mmr_Array.minLoc;
         } else {
            matchLoc_Array = mmr_Array.maxLoc;
         }
            Core.rectangle(webcam_image, matchLoc_Array, new Point(matchLoc_Array.x + templ.cols(),
            matchLoc_Array.y + templ.rows()), new Scalar(0, 255, 0));
    }
    
    /*private static Mat detectRedRects(Mat webcam_image){


        
        return red_hue_image;
        
    }*/
    
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