/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package abc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author bruno
 */
public class RomanCharacterPicture {
    private Mat webcam_image;
    private RectangleCoordinates leftRectangle;
    private RectangleCoordinates rightRectangle;
    
    public RomanCharacterPicture(Mat webcam_image, RectangleCoordinates leftRectangle, RectangleCoordinates rightRectangle){
        this.webcam_image = webcam_image;
        this.leftRectangle = leftRectangle;
        this.rightRectangle = rightRectangle;
    }
    
    public RomanCharacterPicture(Mat webcam_image, List<Rect> foundRectangles){
        this.webcam_image = webcam_image;
        if(foundRectangles.get(0).x < foundRectangles.get(1).x){
            leftRectangle = new RectangleCoordinates(foundRectangles.get(0).x + foundRectangles.get(0).width, foundRectangles.get(0).y);
            rightRectangle = new RectangleCoordinates(foundRectangles.get(1).x,foundRectangles.get(1).y + foundRectangles.get(1).height);
        }else{
            leftRectangle = new RectangleCoordinates(foundRectangles.get(1).x + foundRectangles.get(1).width, foundRectangles.get(1).y);
            rightRectangle = new RectangleCoordinates(foundRectangles.get(0).x,foundRectangles.get(0).y + foundRectangles.get(0).height);
        }
    }
    
    public int evaluatePicture(){
        try{
            ITesseract instance = new Tesseract();

            MatToBufImg webcamImageBuff = new MatToBufImg();

            webcamImageBuff.setMatrix(webcam_image, ".jpg");
            double heightRatio = (double)webcamImageBuff.getBufferedImage().getHeight() / (double)webcam_image.height();
            double widthRatio = (double)webcamImageBuff.getBufferedImage().getWidth() / (double)webcam_image.width();
            int x1 = this.leftRectangle.getxPos();
            int y1 = this.leftRectangle.getyPos();
            int x2 = this.rightRectangle.getxPos();
            int y2 = this.rightRectangle.getyPos();
            Rect rect = new Rect(leftRectangle.getxPos(),leftRectangle.getyPos(),(rightRectangle.getxPos() - leftRectangle.getxPos()),(rightRectangle.getyPos() - leftRectangle.getyPos()));
            //Rect rect = new Rect(new Point(leftRectangle.getxPos(), leftRectangle.getyPos()), new Point(leftRectangle.getxPos(), rightRectangle.getyPos()), , (rightRectangle.getxPos()-leftRectangle.getxPos()));
            Mat subImageMat = webcam_image.submat(rect);
            
            BufferedImage romanCharacter = webcamImageBuff.getBufferedImage().getSubimage((int)(x1 * widthRatio), (int)(y1 * heightRatio), (int)(widthRatio*(x2-x1)), (int)(heightRatio*(y2-y1)));
            
            //int[] pixels = ((DataBufferInt) romanCharacter.getRaster().getDataBuffer()).getData();
            //Mat subImageMat = new Mat(romanCharacter.getHeight(), romanCharacter.getWidth(), CvType.CV_8UC3);
            //subImageMat.put(0, 0, pixels);

            Mat hsv_image = new Mat();
            Imgproc.cvtColor(subImageMat, hsv_image, Imgproc.COLOR_BGR2HSV);

            Mat lower_black_hue_range = new Mat();
            Mat upper_black_hue_range = new Mat();

            Core.inRange(hsv_image, new Scalar(0,0,0), new Scalar(180, 255, 30), lower_black_hue_range);
            Core.inRange(hsv_image, new Scalar(0, 0, 20), new Scalar(180, 255, 40), upper_black_hue_range);

            Mat black_hue_image = new Mat();
            Core.addWeighted(lower_black_hue_range, 1.0, upper_black_hue_range, 1.0, 0.0, black_hue_image);

            Imgproc.GaussianBlur(black_hue_image, black_hue_image, new Size(9, 9), 2, 2);
            
            MatToBufImg blackImageBuff = new MatToBufImg();

            blackImageBuff.setMatrix(black_hue_image, ".jpg");
            BufferedImage test = blackImageBuff.getBufferedImage();
            
            //ImageIO.write(test, "PNG", new FileOutputStream((Math.round(Math.random()*1000))+"dst.png"));
            String result = instance.doOCR(test);
            int counterI = 0;
            for( int i=0; i<result.length(); i++ ) {
                if( result.charAt(i) == 'I' || result.charAt(i) == 'l' || result.charAt(i) == '1'|| result.charAt(i) == 'i' || result.charAt(i) == 'L' || result.charAt(i) == 'j' || result.charAt(i) == 'J') {
                    counterI++;
                } 
            }

            int counterV = 0;
            for( int i=0; i<result.length(); i++ ) {
                if( result.charAt(i) == 'V' || result.charAt(i) == 'v' || result.charAt(i) == 'W' || result.charAt(i) == 'w' || result.contains("\\//")) {
                    counterV++;
                } 
            }
            //System.out.println("Result: "+result+ " calc:" + (counterI + (counterV * 5)));
            return (counterI + (counterV * 5));
        }catch(Exception ex){
            //System.out.println(ex.getMessage());
            ex.printStackTrace();
            return 0;
        }

    }
    
}
