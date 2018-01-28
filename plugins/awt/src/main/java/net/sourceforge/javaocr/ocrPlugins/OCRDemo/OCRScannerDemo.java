/*
 * Copyright (c) 2003-2012, Ronald B. Cemer , Konstantin Pribluda, William Whitney, Andrea De Pasquale
 *
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.sourceforge.javaocr.ocrPlugins.OCRDemo;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.CharacterRange;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.OCRScanner;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImage;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImageLoader;
import net.sourceforge.javaocr.scanner.PixelImage;

import static java.util.logging.Logger.global;
import static sun.misc.Version.println;

/**
 * Demo application to demonstrate OCR document scanning and decoding.
 * @author Ronald B. Cemer
 */
public class OCRScannerDemo
{

    private static final long serialVersionUID = 1L;
    private boolean debug = true, deletebadimage = true;
    private String globalimage;
    private Image image;
    private OCRScanner scanner;
    private HashMap<Character, ArrayList<TrainingImage>> trainingImageMap = new HashMap<Character, ArrayList<TrainingImage>>();

    public OCRScannerDemo()
    {
        scanner = new OCRScanner();
    }

    /**
     * Load demo training images.
     * @param trainingImageDir The directory from which to load the images.
     */
    public void loadTrainingImages(String trainingImageDir)
    {
        if (debug)
        {
            System.err.println("loadTrainingImages(" + trainingImageDir + ")");
        }

        try
        {
            scanner.clearTrainingImages();
            trainDirectories(trainingImageDir + "numbersNew/",1);
            trainDirectories(trainingImageDir + "CapLettersNew/",2);
            trainDirectories(trainingImageDir + "FullRangeChars/",5);
            trainSingleChar(trainingImageDir);

            if (debug)
            {
                System.err.println("adding images");
            }
            scanner.addTrainingImages(trainingImageMap);
            if (debug)
            {
                System.err.println("loadTrainingImages() done");
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            movefile();
            System.exit(2);
        }
    }
    private void movefile(){
        try{
            File afile =new File(globalimage);

            if(afile.renameTo(new File("/Users/scapista/Desktop/Training_data/discardTraning/" + afile.getName()))){
                System.out.println("File is moved successful!");
            }else{
                System.out.println("File is failed to move!");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void trainSingleChar(String trainingImageDir) throws IOException{
        File[] files = new File(trainingImageDir).listFiles();
        for (File file : files) {
            if (file.isDirectory() && file.getName().getBytes().length == 1) {
                if (debug) {
                    System.err.println("Directory: " + file.getName() + " is being processed");
                }
                trainDirectories(trainingImageDir + file.getName(), 6);
            }
        }
    }
    private void trainDirectories(String trainingImageDir,int type) throws IOException{
        char minRange, maxRange;
        TrainingImageLoader loader = new TrainingImageLoader();
        File[] files = new File(trainingImageDir).listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.

        switch (type){
            case 1:
                minRange = '0';
                maxRange = '9';

                break;
            case 2:
                minRange = 'A';
                maxRange = 'Z';
                break;
            case 5:
                minRange = '!';
                maxRange = '~';
                break;
            case 6:
                int endIndex = trainingImageDir.lastIndexOf("/");
                minRange = trainingImageDir.substring(endIndex+1,endIndex+2).charAt(0);
                maxRange = minRange;
                break;
            default:
                System.err.println("invalid training type: " + type + " ");
                return;
        }
        if (debug) {
            System.err.println("ascii minRange: " + minRange + " ascii maxRange: " + maxRange);
        }
        if (!trainingImageDir.endsWith(File.separator))
        {
            trainingImageDir += File.separator;
        }
        for (File file : files) {
            if (file.isFile()) {
                globalimage = trainingImageDir + file.getName();
                if (maxRange == minRange){
                    if (debug)
                    {
                        System.err.println("Single ascii logging:" + globalimage);
                    }
                    loader.load(
                            globalimage,
                            new CharacterRange(minRange),
                            trainingImageMap);
                } else {
                    if (debug) {
                        System.err.println("Range ascii logging:" + globalimage);
                    }
                    loader.load(
                            globalimage,
                            new CharacterRange(minRange, maxRange),
                            trainingImageMap);
                }
            }
        }
    }

    public void process(String imageFilename)
    {
        if (debug)
        {
            System.err.println("process(" + imageFilename + ")");
        }
        try
        {
            image = ImageIO.read(new File(imageFilename));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (image == null)
        {
            System.err.println("Cannot find image file: " + imageFilename);
            return;
        }

        if (debug)
        {
            System.err.println("constructing new PixelImage");
        }

        PixelImage pixelImage = new PixelImage(image);
        if (debug)
        {
            System.err.println("converting PixelImage to grayScale");
        }
        pixelImage.toGrayScale(false);
        if (debug)
        {
            System.err.println("filtering");
        }
        pixelImage.filter();
        if (debug)
        {
            System.err.println("setting image for display");
        }
       
        System.out.println(imageFilename + ":");
        String text = scanner.scan(image, 0, 0, 0, 0, null);
        System.out.println("[" + text + "]");
    }

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.err.println("Please specify one or more image filenames.");
            System.exit(1);
        }
        String trainingImageDir = "/Users/scapista/Desktop/Training_data/";
        if (trainingImageDir == null)
        {
            System.err.println("Please specify -DTRAINING_IMAGE_DIR=<dir> on "
                    + "the java command line.");
            return;
        }
        OCRScannerDemo demo = new OCRScannerDemo();
        demo.loadTrainingImages(trainingImageDir);
        for (int i = 0; i < args.length; i++)
        {
            demo.process("/Users/scapista/Desktop/Training_data/target_4lines.jpg");
        }
        System.out.println("done.");
    }
    private static final Logger LOG = Logger.getLogger(OCRScannerDemo.class.getName());
}
