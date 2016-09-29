package com.word;

import java.io.*;

/**
 * Created by Z on 2016/9/28.
 */
public class WordMatch {
    private static final int WORDNUM = 8;

    /*private static void readfile(String filePath){

    }*/
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("word.txt")));
            String data = null;

            while ((data = br.readLine()) != null)
                System.out.println(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
