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
import static jdk.nashorn.internal.objects.NativeString.substring;
import static sun.misc.Version.println;

/**
 * Demo application to demonstrate OCR document scanning and decoding.
 * @author Ronald B. Cemer
 */
public class OCRScannerDemo {

    private static final long serialVersionUID = 1L;
    private boolean debug = true, deletebadimage = true;
    private String globalimage;
    private Image image;
    private OCRScanner scanner;
    private HashMap<Character, ArrayList<TrainingImage>> trainingImageMap = new HashMap<Character, ArrayList<TrainingImage>>();

    private void debugInfo(String logOutput) {
        if (debug) {
            System.err.println( Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + logOutput);
        }
    }

    private  OCRScannerDemo() {
        scanner = new OCRScanner();
    }

    /**
     * Load demo training images.
     * @param trainingImageDir The directory from which to load the images.
     */
    private void loadTrainingImages(String trainingImageDir) {
        debugInfo( "Trainging Image Directory -> " + trainingImageDir );
        try {
            scanner.clearTrainingImages();
            trainDirectories(trainingImageDir + "numbersNew/",'0','9');
            trainDirectories(trainingImageDir + "CapLettersNew/",'A','Z');
            trainDirectories(trainingImageDir + "FullRangeChars/",'!','~');
            trainSingleChar(trainingImageDir);

            debugInfo ( "adding images");
            scanner.addTrainingImages(trainingImageMap);
            debugInfo ( "loadTrainingImages() done");
        } catch (IOException ex) {
            ex.printStackTrace();
            movefile();
            System.exit(2);
        }
    }
    private void movefile() {
        try {
            File afile = new File(globalimage);

            if (afile.renameTo(new File("/Users/scapista/Desktop/Training_data/discardTraning/" + afile.getName()))) {
                debugInfo ( "File is moved successful!");
            } else {
                debugInfo ( "File is failed to move!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private char getDirectoryCharName(String trainingImageDir) {
        debugInfo ( trainingImageDir);
        int endIndex = trainingImageDir.lastIndexOf("/");
        return trainingImageDir.substring(endIndex+1,endIndex+2).charAt(0);
    }
    /**
     * loops through all directories in input parm directory
     *
     * @param trainingImageDir directory to be trained
     */
    private void trainSingleChar(String trainingImageDir) throws IOException {
        File[] files = new File(trainingImageDir).listFiles();
        for (File file : files) {
            if (file.isDirectory() && file.getName().getBytes().length == 1) {
                debugInfo ( "Directory: " + file.getName() + " is being processed");
                trainSingleChar(trainingImageDir + file.getName()
                        , getDirectoryCharName(trainingImageDir + file.getName()));
            }
        }
    }
    /**
     * overridden to get a single character if need
     * this gives the ability to move the directory
     * outside of the global trainingImageDir variable
     *
     * @param trainableChar char to be trained
     * @param trainingImageDir directory to be trained
     */
    private void trainSingleChar(String trainingImageDir, char trainableChar) throws IOException{
        File directory = new File(trainingImageDir);
        if(directory.exists()){
            debugInfo ( "direectory " + directory + " exists");
            trainDirectories(trainingImageDir, trainableChar, trainableChar);
        } else {
            debugInfo ( "direectory " + directory + " does not exist");
        }
    }
    private void trainDirectories(String trainingImageDir,char minRange, char maxRange) throws IOException{
        TrainingImageLoader loader = new TrainingImageLoader();
        File[] files = new File(trainingImageDir).listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.

        debugInfo ( "ascii minRange: " + minRange + " ascii maxRange: " + maxRange);
        if (!trainingImageDir.endsWith(File.separator)) {
            trainingImageDir += File.separator;
        }
        //process all files in
        for (File file : files) {
            if (file.isFile()) {
                globalimage = trainingImageDir + file.getName();
                if (maxRange == minRange){
                    debugInfo ( "Single ascii logging:" + globalimage);
                    loader.load(
                            globalimage,
                            new CharacterRange(minRange),
                            trainingImageMap);
                } else {
                    debugInfo ( "Range ascii logging:" + globalimage);
                    loader.load(
                            globalimage,
                            new CharacterRange(minRange, maxRange),
                            trainingImageMap);
                }
            }
        }
    }


    private void process(String imageFilename) {
        debugInfo ( "process(" + imageFilename + ")");
        try {
            image = ImageIO.read(new File(imageFilename));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (image == null) {
            System.err.println("Cannot find image file: " + imageFilename);
            return;
        }

        debugInfo ( "constructing new PixelImage" );
        PixelImage pixelImage = new PixelImage(image);
        debugInfo ( "converting PixelImage to grayScale" );
        pixelImage.toGrayScale(false);
        debugInfo ( "filtering");
        pixelImage.filter();
        debugInfo ( "setting image for display" );
       
        System.out.println(imageFilename + ":");
        String text = scanner.scan(image, 0, 0, 0, 0, null);
        System.out.println("[" + text + "]");
    }

    public static void main(String[] args)
    {
        if (args.length < 1) {
            System.err.print( "Please specify one or more image filenames.");
            System.exit(1);
        }
        String trainingImageDir = "/Users/scapista/Desktop/Training_data/";
        if (trainingImageDir == null) {
            System.err.println("Please specify -DTRAINING_IMAGE_DIR=<dir> on "
                    + "the java command line.");
            return;
        }
        OCRScannerDemo demo = new OCRScannerDemo();
        demo.loadTrainingImages(trainingImageDir);
        for (int i = 0; i < args.length; i++) {
            demo.process("/Users/scapista/Desktop/Training_data/target_4lines.jpg");
        }
        System.out.println("done.");
    }
    private static final Logger LOG = Logger.getLogger(OCRScannerDemo.class.getName());
}
