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

import net.sourceforge.javaocr.ocrPlugins.mseOCR.CharacterRange;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.OCRScanner;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImage;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.TrainingImageLoader;
import net.sourceforge.javaocr.scanner.PixelImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Demo application to demonstrate OCR document scanning and decoding.
 * @author Ronald B. Cemer
 */
public class OCRScannerDemo {

    private static final long serialVersionUID = 1L;
    //private boolean debug = false;
    private static final String START = "Start", END = "END";
    private Image image;
    private OCRScanner scanner;
    private HashMap<Character, ArrayList<TrainingImage>> trainingImageMap = new HashMap<Character, ArrayList<TrainingImage>>();
    private static final Level DEBUGLEVEL= Level.INFO;

    private static Logger debug =
            Logger.getLogger(new Throwable() .getStackTrace()[0].getClassName());


    private static void debugMethod(String startEnd){
        if(startEnd.equals("Start"))
            debug.finest(Thread.currentThread().getStackTrace()[2].getMethodName() + " Started");
        else
            debug.finest(Thread.currentThread().getStackTrace()[2].getMethodName() + " Completed");
    }
    private void debugInfo(String logOutput) {

        /* if (debug) {
            System.err.println( Thread.currentThread().getStackTrace()[2].getMethodName() + ": " + logOutput);
        }*/
    }

    private  OCRScannerDemo() {
        scanner = new OCRScanner();
    }

    /**
     * Load demo training images.
     * @param trainingImageDir The directory from which to load the images.
     */
    private void startTraining(String trainingImageDir) {
        debug.info( "Trainging Image Directory -> " + trainingImageDir );
        try {
            scanner.clearTrainingImages();
            trainDirectories(trainingImageDir + "numbersNew/",'0','9');
            trainDirectories(trainingImageDir + "CapLettersNew/",'A','Z');
            trainDirectories(trainingImageDir + "FullRangeChars/",'!','~');
            trainSingleChar(trainingImageDir);

            debug.info ( "adding images");
            scanner.addTrainingImages(trainingImageMap);
            debug.info ( "loadTrainingImages() done");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(2);
        }
    }
    private void movefile(String fileName, String moveDirectory) {
        
        try {
            File afile = new File(fileName);

            if (afile.renameTo(new File( moveDirectory + afile.getName()))) {
                debug.fine ( "File is moved successful!");
            } else {
                debug.fine ( "File is failed to move!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    private char getDirectoryCharName(String trainingImageDir) {
        debug.finer ( trainingImageDir);
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
                debug.finer( "Directory: " + file.getName() + " is being processed");
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
    private void trainSingleChar(String trainingImageDir, char trainableChar) throws IOException {
        
        File directory = new File(trainingImageDir);
        if(directory.exists()){
            debug.fine ( "direectory " + directory + " exists");
            trainDirectories(trainingImageDir, trainableChar, trainableChar);
        } else {
            debug.fine ( "direectory " + directory + " does not exist");
        }
        
    }
    private void loadTrainingImages(String dirFileName, char minRange, char maxRange) {
        
        TrainingImageLoader loader = new TrainingImageLoader();
        if (maxRange == minRange) {
            debug.fine("Single ascii char: " + minRange + " --> " + dirFileName);
        } else {
            debug.fine("Range ascii max: " + maxRange + " min: " + maxRange + " --> " + dirFileName);
        }
        try {
            loader.load(
                    dirFileName,
                    new CharacterRange(minRange, maxRange),
                    trainingImageMap);
        } catch (IOException e) {
            e.printStackTrace();
            movefile(dirFileName, "/Users/scapista/Desktop/Training_data/discardTraining/" );
        }
        
    }
    private void trainDirectories(String trainingImageDir, char minRange, char maxRange) throws IOException{
        
        File[] files = new File(trainingImageDir).listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.

        debug.fine ("trainingImageDir: " + trainingImageDir + " minRange:" + minRange + " maxRange: " + maxRange);
        if (!trainingImageDir.endsWith(File.separator)) {
            trainingImageDir += File.separator;
        }
        //process all files in directory
        for (File file : files) {
            if (file.isFile()) {
                loadTrainingImages(trainingImageDir + file.getName(), minRange, maxRange);
            }
        }
        
    }



    private void process(String imageFilename) {
        
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

        debug.fine( "constructing new PixelImage" );
        PixelImage pixelImage = new PixelImage(image);

        debug.fine ( "converting PixelImage to grayScale" );
        pixelImage.toGrayScale(false);

        debug.fine ( "filtering" );
        pixelImage.filter();

        debug.fine ( "setting image for display" );
        debug.info("Input Image Name -> " + imageFilename + ":");
        String text = scanner.scan(image, 0, 0, 0, 0, null);
        System.out.println("[" + text + "]"); 
        
    }
    public static void main(String[] args)
    {
        Handler[] handlers = Logger.getLogger( "" ).getHandlers();
        for ( int index = 0; index < handlers.length; index++ ) {
            handlers[index].setLevel( DEBUGLEVEL );
        }
        debug.setLevel(DEBUGLEVEL);

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
        demo.startTraining(trainingImageDir);
        for (int i = 0; i < args.length; i++) {
            demo.process("/Users/scapista/Desktop/Training_data/IMG_2371.jpg");
        }
        System.out.println("done.");
    }
    private static final Logger LOG = Logger.getLogger(OCRScannerDemo.class.getName());
}
