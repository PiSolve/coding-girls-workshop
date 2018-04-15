package com.codinggirls.com.androidworkshop1;

/**
 * Created by renu.yadav on 4/4/18.
 */

public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello world");
        printWithLoop(10);
    }

    private static void printWithLoop(int limit){
        for(int i =0;i<limit;i++){
            System.out.println(i);
        }
    }
}
