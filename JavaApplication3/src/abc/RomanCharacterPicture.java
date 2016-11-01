/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package abc;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

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
            
            BufferedImage romanCharacter = webcamImageBuff.getBufferedImage().getSubimage((int)(x1 * widthRatio), (int)(y1 * heightRatio), (int)(widthRatio*(x2-x1)), (int)(heightRatio*(y2-y1)));
            
            //ImageIO.write(romanCharacter, "PNG", new FileOutputStream((Math.round(Math.random()*1000))+"dst.png"));
            String result = instance.doOCR(romanCharacter);
            int counterI = 0;
            for( int i=0; i<result.length(); i++ ) {
                if( result.charAt(i) == 'I' || result.charAt(i) == 'l' || result.charAt(i) == '1'|| result.charAt(i) == 'i' ) {
                    counterI++;
                } 
            }

            int counterV = 0;
            for( int i=0; i<result.length(); i++ ) {
                if( result.charAt(i) == 'V' || result.charAt(i) == 'v') {
                    counterV++;
                } 
            }
        
            return (counterI + (counterV * 5));
        }catch(Exception ex){
            return 0;
        }

    }
    
}
