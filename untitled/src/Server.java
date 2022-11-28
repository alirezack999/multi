import netscape.javascript.JSObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    static Scanner scanner = new Scanner(System.in);
    private ServerSocket serverSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private int port;
    static int i = 0;
    static ArrayList<Socket> clients = new ArrayList<Socket>();

    public Server(int port){
        try {
            this.serverSocket = new ServerSocket(port);
        }catch (Exception e){
            log(e.getMessage());
        }
    }
    public void startServer(){
        try {
            log("Server started successfully");
            while (true) {
                log("The server is waiting for the client...");
                Socket client = this.serverSocket.accept();
                clients.add(client);
                log("The client has successfully connected to the server");
                Handler clientHandler = new Handler(client);
                Thread thread = new Thread(clientHandler);
                thread.start();
                if(clients.size() == 2){
                    break;
                }
            }
        }catch (Exception e){
            log(e.getMessage());
        }

    }
    public static void log(String message){
        System.out.println(message);
    }

    //Handler user
    static class Handler implements Runnable{
        final static String INET_ADDR = "224.0.0.3";
        final static int PORT = 8888;
        private Socket socket ;
        private DataInputStream input;
        private DataOutputStream output;
        public Handler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            startConnection();
                write();
                read();
        }
        public void write(){

            Thread write = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //String message = INET_ADDR+" "+PORT;

                        JSONObject message = new JSONObject();
                        message.put("IP",INET_ADDR);
                        message.put("PORT",PORT);
                        System.out.println(message);
                          getOutput().writeUTF(message.toJSONString());
                        log("Message sent successfully");
                    }catch (Exception e){
                        log(e.getMessage());
                    }
                }
            });
            write.start();
        }
        public void read(){
            Thread read = new Thread(new Runnable() {
                @Override
                public void run() {
                    String message = null;
                    try {
                        message = getInput().readUTF();
                        log("The message was received from the server : "+message);
                    }catch (Exception e){
                        log(e.getMessage());
                    }
                }
            });
            read.start();
        }
        private void startConnection(){
            try {
                this.input = new DataInputStream(this.socket.getInputStream());
                this.output = new DataOutputStream(this.socket.getOutputStream());
            }catch (Exception e){
                Server.log(e.getMessage());
            }
        }

        public void MessageProcessing(JSObject message){

            JSONParser parser = new JSONParser();
            String msg = (String) message.getMember("type");
            log(msg);



        }
        private void mainMenu(){
            log("1-login");
            log("2-register");
            log("3-exit");
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

}
