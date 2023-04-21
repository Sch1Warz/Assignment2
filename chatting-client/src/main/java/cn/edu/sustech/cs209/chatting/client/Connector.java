package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.UserList;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;

public class Connector implements Runnable {
    public String username;
    private Socket socket;
    private static ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Controller controller;

    @Override
    public void run() {
        try{
            connect();
        } catch (IOException e){
            e.printStackTrace();
        }
        try{
            while(socket.isConnected()) {
                Platform.runLater(() -> {
                    this.controller.currentOnlineCnt.setText("Online user counts: " + String.valueOf(UserList.getUserList().size()));
                    this.controller.currentOnlineCnt1.setText("Online user: " + String.valueOf(UserList.getUserList()));
                });

                Message message = (Message) inputStream.readObject();
                if(message.getMessageType() == MessageType.NOTIFICATION){
                    UserList.setUserList(Arrays.asList(message.getMessage().split(", ")));
                    System.out.println(UserList.getUserList());

                }
                else if(message.getMessageType() == MessageType.PRIVATE){
                    this.controller.handleReceive(message);
                }
                else if(message.getMessageType() == MessageType.GROUP){
                    this.controller.handleReceive(message);
                }
            }
        } catch (ClassNotFoundException | IOException e){

            String filePath = "C:\\Users\\28573\\Desktop\\test\\Assignment2\\names.txt";
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(filePath);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }


            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Server is closed");
                ButtonType buttonTypeOne = new ButtonType("OK");

                alert.getButtonTypes().setAll(buttonTypeOne);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonTypeOne){
                    Platform.exit();
                }

            });
        }
    }

    public Connector(String username, Controller controller){
        this.username = username;
        this.controller = controller;
        try{
            this.socket = new Socket("localhost", 8090);
            outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.inputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void connect() throws IOException {
        Message msg = new Message(MessageType.CONNECTED, this.username, "server", "hello");
        outputStream.writeObject(msg);
    }

    public static void send(Message message){
        try {
            outputStream.writeObject(message);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
