package org.example.chesslibuiexample.util;

import javafx.scene.image.Image;
import org.example.chesslibuiexample.HelloApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

public class PieceImageContainer {

    HashMap<String, Image> images;

    public PieceImageContainer(){
        try {
            loadImages();
        }catch (IOException e){
            throw new  RuntimeException("problem loading images",e);
        }

        System.out.println(images);
    }

    public HashMap<String, Image> getImages() {
        return images;
    }

    public void setImages(HashMap<String, Image> images) {
        this.images = images;
    }

    private void loadImages() throws IOException {
        images = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(HelloApplication.class.getResource("img.txt").openStream())); // retrieve stream of file names
        String line;
        while((line = reader.readLine()) != null) { // go through list of files
            String[] split = line.split("-");
            System.out.println(Arrays.toString(split));
            cleanUpFileName(split);
            String finalLine = line;
            System.out.println(split[0] + "-" + split[1] + ".png");
            images.put(split[2], new Image(String.valueOf(HelloApplication.class.getResource("img/" + split[0] + "-" + split[1] + ".png")).trim()));


        }

        reader.close();
    }

    private void cleanUpFileName(String[] split){
        if(split.length < 3){ // if length is less than three, it messed up loading a file. Closing program
            throw new RuntimeException("Error loading assets");
        }
        split[2] = split[2].substring(0, split[2].length() - 4);// cut that png off
    }
}
