package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.UserList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.HashMap;

public class Main {


    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\28573\\Desktop\\test\\Assignment2\\names.txt";
        FileWriter fileWriter = new FileWriter(filePath);
        fileWriter.write("");
        fileWriter.flush();
        fileWriter.close();

        System.out.println("Start server");
        ServerSocket serverSocket = new ServerSocket(8090);
        Server1 server = new Server1(serverSocket);
        server.keepListen();
    }


}

class Server1 {
    private volatile Map<String, ClientService> clients;
    private final ServerSocket serverSocket;

    public Server1(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clients = new HashMap<>();
    }

    public void keepListen() {
        System.out.println("The Server is started");
        while (true) {
            try {
                Socket socket = this.serverSocket.accept();
                ClientService clientService = new ClientService(socket);
                clientService.start();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }


    private class ClientService extends Thread {
        private String username;
        private Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        public ClientService(Socket socket) {
            try {
                this.socket = socket;
                this.inputStream = new ObjectInputStream(socket.getInputStream());
                this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    while (socket.isConnected()) {
                        Message clientMsg = (Message) inputStream.readObject();
//                    System.out.println(
//                            clientMsg.getSentBy() + " " + clientMsg.getSendTo() + " " + clientMsg.getData());
                        if (clientMsg.getMessageType() == MessageType.CONNECTED) {
                            this.username = clientMsg.getName();
                            System.out.println(
                                    clientMsg.getName() + " " + clientMsg.getSendTo() + " " + clientMsg.getMessage());
                            UserList.addUser(this.username);
                            System.out.println(UserList.getUserList());
                            System.out.println(UserList.listString());
                            clients.put(this.username, this);
                            clients.forEach((s, clientService) -> {
                                clientService.sendUserList("-1");
                            });
                        } else if (clientMsg.getMessageType() == MessageType.PRIVATE) {
                            sendTo(clientMsg.getSendTo(), clientMsg);
                        } else if (clientMsg.getMessageType() == MessageType.GROUP) {
                            String members = clientMsg.getSendTo();
                            List<String> toSend = Arrays.asList(members.split(", "));
                            toSend.forEach(s -> {
                                if (!s.equals(this.username)) {
                                    try {
                                        sendTo(s, clientMsg);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }

                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Client No Connection!");
                    break;
                } finally {
                    String filePath = "C:\\Users\\28573\\Desktop\\test\\Assignment2\\names.txt";
                    try {
                        String result = "";
                        String line = null;
                        FileReader fr = new FileReader(filePath);
                        FileWriter writer = new FileWriter(filePath);
                        BufferedReader br = new BufferedReader(fr);
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                            String[] arr = line.split(" ");

                            for (String s : arr) {
                                if (!Objects.equals(s, this.username)) {
                                    result += (this.username + " ");
                                }
                            }
                        }
                        writer.write(result);
                        writer.flush();
                        writer.close();
                        fr.close();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    UserList.removeUser(this.username);
                    clients.remove(this.username);
                    clients.forEach((s, clientService) -> {
                        clientService.sendUserList(this.username);
                    });

                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public synchronized void sendTo(String username, Message message) throws IOException {
            ClientService service;
            if (clients.containsKey(username)) {
                service = clients.get(username);
                service.send(message);
            }
        }

        public synchronized void send(Message message) throws IOException {
            outputStream.writeObject(message);
        }

        public void sendUserList(String i) {
            Message message;
                message = new Message(MessageType.NOTIFICATION,
                        "server", this.username, UserList.listString());

            try {
                outputStream.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}