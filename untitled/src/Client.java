import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {

    private Socket socket;
    public static Scanner scanner = new Scanner(System.in);
    private DataInputStream input;
    private DataOutputStream output;

    public Client(){

    }

    public Client(Socket socket, DataInputStream input, DataOutputStream output){
        this.socket = socket;
        this.input = input;
        this.output = output;
    }
    public void startConnection(){
        try {
            input = new DataInputStream(this.socket.getInputStream());
            output = new DataOutputStream(this.socket.getOutputStream());
        }catch (Exception e){
            log(e.getMessage());
        }
    }
    public void connect(String ip, int port){
        try {
            this.socket = new Socket(ip,port);
            log("Connected");
        }catch (Exception e){
            log(e.getMessage());
        }
    }
    public void write(){
        Scanner scanner = new Scanner(System.in);
        Thread write = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String message = scanner.nextLine();
                    try {
                        getOutput().writeUTF(message);
                        log("Message sent successfully");
                    } catch (Exception e) {
                        log(e.getMessage());
                    }
                }
            }
        });

        write.start();

    }
    public void read(){
        Thread read = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String message = null;
                    JSONParser parser = new JSONParser();
                    try {
                        message = getInput().readUTF();
                        JSONObject object = (JSONObject) parser.parse(message);
                        log("The message was received from the server : " + object);
                        multiCast((String) object.get("IP"), (String) object.get("PORT"));
                    } catch (Exception e) {
                        log(e.getMessage());
                    }
                }
            }
        });
        read.start();
    }

    public void multiCast(String ip,String port){
        try {
            log("multiCast");
            InetAddress address = InetAddress.getByName(ip);
            byte[] buf = new byte[256];
            MulticastSocket clientSocket = new MulticastSocket(Integer.parseInt(port));
            clientSocket.joinGroup(address);
            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {
                            DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                            clientSocket.receive(msgPacket);
                            String msg = new String(buf, 0, buf.length);
                            System.out.println("received msg: " + msg);
                        } catch (IOException e) {
                            log(e.getMessage());
                        }
                    }
                }
            });
            thread1.start();
            Thread thread2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {
                            String msg = scanner.nextLine();
                            DatagramSocket serverSocket = new DatagramSocket();
                            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),msg.getBytes().length, InetAddress.getByName(ip),Integer.parseInt(port));
                            serverSocket.send(msgPacket);
                            System.out.println("Client Server sent packet with msg: " + msg);
                        } catch (Exception e) {
                            log(e.getMessage());
                        }
                    }
                }
            });
            thread2.start();
        } catch (Exception e) {
            log(e.getMessage());
        }

    }
    private void log(String message){
        System.out.println(message);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataInputStream getInput() {
        return input;
    }

    public void setInput(DataInputStream input) {
        this.input = input;
    }

    public DataOutputStream getOutput() {
        return output;
    }

    public void setOutput(DataOutputStream output) {
        this.output = output;
    }
}
