package com.slycord.carsimfinal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread{

    private String host;
    private int port;
    public static String nickname;
    boolean running = true;

    public Client(String host, int port, String newName) throws IOException {
        this.host = host;
        this.port = port;
        nickname = newName;
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    public void run() {
        // connect client to server
        Socket client = null;

        try {
            client = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client successfully connected to server!");

        // create a new thread for server messages handling
            try {
                new Thread(new ReceivedMessagesHandler(client.getInputStream(), nickname)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }


        PrintStream output = null;
        try {
            output = new PrintStream(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

//        for(int i = 0; i < 100; i++) output.println("name," + nickname);

        while (running) {


            try {
                currentThread().sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Transmitted to physics: ID1, Gas, Brake, Engine, Impact, current Angle - physicsBuffer
            // Transmitted to other phones: ID2,  Current Angle, xpos, ypos - carBuffer
            //this is the output from the phone back to the server
            output.println(nickname + ",physics," + MainActivity.physicsBuffer[0] + "," + MainActivity.physicsBuffer[1] + "," + MainActivity.physicsBuffer[2] + "," + MainActivity.physicsBuffer[3] + "," + MainActivity.physicsBuffer[4] + "," + MainActivity.carBuffer[0] + ","  + MainActivity.carBuffer[1] + ","  + MainActivity.carBuffer[2]);

            try {
                currentThread().sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        output.close();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class ReceivedMessagesHandler implements Runnable {

    public String[] buffer;
    public boolean running = true;
    private InputStream server;
    private String nickName;

    public ReceivedMessagesHandler(InputStream server, String newName) {
        this.server = server;
        this.nickName = newName;
    }

    public void run() {
        // receive server messages and print out to screen
        Scanner s = new Scanner(server);
        while (running) {

            //This is where the physics data is coming in
            if(s.nextLine().contains(nickName) && !(s.nextLine().contains("null"))) {
                buffer = s.nextLine().split(",");
                if (buffer.length == 8) {
                    //Buffer length 8 for 1 player, 11 for 2 player, 14 for 3 player
                     MainActivity.acceleration = buffer[1];
                     if (!buffer[2].contains("null")) MainActivity.speed = Double.parseDouble(buffer[2]);
                     MainActivity.slipAngle = buffer[3];
                     MainActivity.sixty = buffer[4];
                     MainActivity.rpm = buffer[5];
                     MainActivity.currentGear = buffer[6];
                     MainActivity.brakingDistance = buffer[7];

                    /*
                    To-Do
                    Test logic for setX and setY in multiplayer, has not been tested since
                    it has been normalized to account for different screen sizes
                     */
//                     if (!buffer[8].contains("null")) MainActivity.car2.setRotation(Float.parseFloat(buffer[8])*MainActivity.width);
//                     if (!buffer[9].contains("null")) MainActivity.car2.setX(Float.parseFloat(buffer[9])*MainActivity.width);
//                     if (!buffer[10].contains("null"))MainActivity.car2.setY(Float.parseFloat(buffer[10])*MainActivity.width);
//                     if (!buffer[11].contains("null"))MainActivity.car3.setRotation(Float.parseFloat(buffer[11])*MainActivity.width);
//                     if (!buffer[12].contains("null"))MainActivity.car3.setX(Float.parseFloat(buffer[12])*MainActivity.width);
//                     if (!buffer[13].contains("null"))MainActivity.car3.setY(Float.parseFloat(buffer[13])*MainActivity.width);
                 }
            }

        }
        s.close();
    }
}
