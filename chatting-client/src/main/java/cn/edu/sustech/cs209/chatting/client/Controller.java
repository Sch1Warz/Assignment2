package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.client.bubble.BubbleSpec;
import cn.edu.sustech.cs209.chatting.client.bubble.BubbledLabel;
import cn.edu.sustech.cs209.chatting.common.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;


public class Controller implements Initializable {

    public Label currentOnlineCnt;
    public Label currentOnlineCnt1;
    public ComboBox<String> combo1;
    Map<String, Integer> chatWithName;
    private static final String[] EMOJI_VALUES = {"\u263A",
            "\uD83D\uDE00",
            "\uD83D\uDE01",
            "\uD83D\uDE02",
            "\ud83d\udc4c"};

    @FXML
    ListView<Message> chatContentList;

    @FXML
    ListView<Chat> chatList;
    @FXML
    TextArea inputArea;
    @FXML
    Label currentUsername;

    ChatType currentType;
    String currentChatName;

    String username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {
            username = input.get();
            String filePath = "C:\\Users\\28573\\Desktop\\test\\Assignment2\\names.txt";

            boolean chongfu = false;
            String line = null;
            try {
                FileReader fr = new FileReader(filePath);
                BufferedReader br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    String[] arr = line.split(" ");
                    for (String s : arr) {
                        if (Objects.equals(s, username)) {
                            chongfu = true;
                            break;
                        }
                    }
                }
                fr.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            if (chongfu) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("The username is being used!");
                    alert.showAndWait();
                    Platform.exit();
                });

            } else {
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(filePath, true);
                    fileWriter.write(" " + username);
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                currentUsername.setText("Current User: " + username);
                Handler Handler = new Handler(username, this);
                Thread x = new Thread(Handler);
                x.start();
                chatWithName = new HashMap<>();
            }


        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Please input a username!");
                alert.showAndWait();
                Platform.exit();
            });

        }


        combo1.getItems().addAll("Smile",
                "Grinning Face",
                "Grinning Face with Smiling Eyes",
                "Face with Tears of Joy",
                "OKOK");
        combo1.setOnAction((event) -> {
            String emojiName = combo1.getValue();
            int emojiIndex = 0;
            for (int i = 0; i < combo1.getItems().size(); i++) {
                if (combo1.getItems().get(i).equals(emojiName)) {
                    emojiIndex = i;
                    break;
                }
            }
            String emojiValue = EMOJI_VALUES[emojiIndex];

            inputArea.appendText(emojiValue);
        });


        chatContentList.setCellFactory(new MessageCellFactory());
        chatList.setCellFactory(new ChatCellFactory());
        chatList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        chatContentList.setItems(FXCollections.observableList(newValue.getMessageList()));
                        currentType = newValue.getChatType();
                        currentChatName = newValue.getChatName();
                    }
                }
        );
    }

    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        UserList.getUserList().forEach(s -> {
            if (!Objects.equals(s, this.username)) {
                userSel.getItems().add(s);
            }
        });

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        String ans = user.get();

        if ((ans != null) && !chatWithName.containsKey(ans)) {

            Chat chat = new Chat(ChatType.PRIVATE, ans);
            chatList.getItems().add(chat);
            chatWithName.put(ans, chatList.getItems().indexOf(chat));
            chatList.getSelectionModel().select(chat);
        } else if ((ans != null) && chatWithName.containsKey(ans)) {
            chatList.getSelectionModel().select(null);
            chatList.getSelectionModel().select(chatWithName.get(ans));
        }
    }


    @FXML
    public void createGroupChat() {

        Stage stage = new Stage();

        ArrayList<String> aqa = new ArrayList<>();

        UserList.getUserList().forEach(s -> {
            if (!Objects.equals(s, this.username)) {
                aqa.add(s);
            }
        });

        Button okBtn = new Button("OK");

        okBtn.setOnAction(event -> {
            stage.close();
        });


        VBox vbox = new VBox();
        HBox hbox = new HBox();
        Label label = new Label("选择需要加入群聊的人");
        label.setWrapText(true);
        CheckBox[] boxArray = new CheckBox[aqa.size()];

        AtomicReference<String> chooseNames = new AtomicReference<>(this.username);

        for (int i = 0; i < aqa.size(); i++) {
            CheckBox ck1 = new CheckBox(aqa.get(i));
            hbox.getChildren().addAll(ck1);
            boxArray[i] = ck1;

            ck1.selectedProperty().addListener((arg0, arg1, arg2) -> {
                chooseNames.set(this.username + "," + Arrays.toString(getCheckedItem(boxArray).split("、")).replace("[", "").replace("]", "").replace(" ", ""));
                label.setText(String.format("您%s了%s。当前已选人员包括：%s",
                        (ck1.isSelected() ? "选择" : "取消"), ck1.getText(), chooseNames.get()));
            });
        }
        vbox.getChildren().addAll(hbox, label, okBtn);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(100, 200, 50, 200));

        vbox.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(vbox));
        stage.showAndWait();


        String[] arr = chooseNames.get().split(",");
        Arrays.sort(arr);
        String users = "";
        for (int i = 0; i < arr.length; i++) {
            if (i != arr.length - 1) {
                users += arr[i] + ",";
            } else {
                users += arr[i];
            }
        }
        if (!users.equals(username) && !chatWithName.containsKey(users)) {
//            System.out.println(users);
            Chat chat = new Chat(ChatType.GROUP, users);
            chat.setMembers(Arrays.asList(users.split(",")));
            chatList.getItems().add(chat);
            chatWithName.put(users, chatList.getItems().indexOf(chat));
            chatList.getSelectionModel().select(chat);
        } else if (!users.equals(username) && chatWithName.containsKey(users)) {
            chatList.getSelectionModel().select(null);
            chatList.getSelectionModel().select(chatWithName.get(users));
        }
    }

    private String getCheckedItem(CheckBox[] boxArray) {
        String itemDesc = "";
        for (CheckBox box : boxArray) {
            if (box.isSelected()) {
                if (itemDesc.length() > 0) {
                    itemDesc = itemDesc + "、";
                }
                itemDesc = itemDesc + box.getText();
            }
        }
        return itemDesc;
    }


    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        // TODO
        if (!inputArea.getText().isEmpty() && chatList.getSelectionModel().getSelectedItem() != null) {
            if (currentType == ChatType.PRIVATE) {
                Message message = new Message(MessageType.PRIVATE,
                        username, currentChatName, convertUnicodeToEmoji(inputArea.getText()));
                Handler.send(message);

                Chat chat = chatList.getItems().get(chatWithName.get(currentChatName));
                chat.addMessage(message);
                chatList.getItems().set(chatWithName.get(currentChatName), chat);
                chatList.getSelectionModel().select(chatWithName.get(currentChatName));
            } else if (currentType == ChatType.GROUP) {
                String sentBy = currentChatName + ":::" + username;

                Chat chat = chatList.getItems().get(chatWithName.get(currentChatName));
                String sendTo = chat.memberString();
                Message message = new Message(MessageType.GROUP,
                        sentBy, sendTo, convertUnicodeToEmoji(inputArea.getText()));

                Handler.send(message);
                chat.addMessage(new Message(MessageType.GROUP,
                        username, sendTo, convertUnicodeToEmoji(inputArea.getText())));
                chatList.getItems().set(chatWithName.get(currentChatName), chat);
                chatList.getSelectionModel().select(chatWithName.get(currentChatName));
            }
            inputArea.clear();
        }
        chatList.getSelectionModel().select(null);
        chatList.getSelectionModel().select(chatWithName.get(currentChatName));
    }

    private String convertUnicodeToEmoji(String inputText) {
        if (inputText.contains("\\u")) {
            String outputText = "";
            String[] tokens = inputText.split("\\\\u");
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    try {
                        int codePoint = Integer.parseInt(token, 16);
                        outputText += new String(Character.toChars(codePoint));
                    } catch (NumberFormatException e) {
                        outputText += "\\u" + token;
                    }
                }
            }
            System.out.println(outputText);
            return outputText;
        } else {
            return inputText;
        }

    }

    public void handleReceive(Message message) {
        Platform.runLater(() -> {
            if (message.getMessageType() == MessageType.PRIVATE) {
                if (chatWithName.containsKey(message.getName())) {
                    Chat chat = chatList.getItems().get(chatWithName.get(message.getName()));
                    chat.addMessage(message);
                    chatList.getItems().set(chatWithName.get(message.getName()), chat);
                    chatList.getSelectionModel().select(null);
                    chatList.getSelectionModel().select(chatWithName.get(message.getName()));
                } else {
                    Chat chat = new Chat(ChatType.PRIVATE, message.getName());
                    chat.addMember(message.getName());
                    chat.addMessage(message);
                    chatList.getItems().add(chat);
                    chatWithName.put(message.getName(), chatList.getItems().indexOf(chat));
                    chatList.getSelectionModel().select(null);
                    chatList.getSelectionModel().select(chat);
                }
            } else if (message.getMessageType() == MessageType.GROUP) {
                String groupName = message.getName().split(":::")[0];
                String senderName = message.getName().split(":::")[1];
                message.setSentBy(senderName);
                if (chatWithName.containsKey(groupName)) {
                    Chat chat = chatList.getItems().get(chatWithName.get(groupName));
                    chat.setMembers(Arrays.asList(message.getSendTo().split(", ")));
                    chat.addMessage(message);
                    chatList.getItems().set(chatWithName.get(groupName), chat);
                    chatList.getSelectionModel().select(null);
                    chatList.getSelectionModel().select(chatWithName.get(groupName));
                } else {
                    Chat chat = new Chat(ChatType.GROUP, groupName);
                    chat.setMembers(Arrays.asList(message.getSendTo().split(", ")));
                    chat.addMessage(message);
                    chatList.getItems().add(chat);
                    chatWithName.put(groupName, chatList.getItems().indexOf(chat));
                    chatList.getSelectionModel().select(null);
                    chatList.getSelectionModel().select(chat);
                }
            }
        });

    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel,
     * or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    BubbledLabel bl6 = new BubbledLabel();
                    bl6.setText(msg.getName() + ": " + msg.getMessage());


                    HBox x = new HBox();

                    if (username.equals(msg.getName())) {
                        bl6.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN,
                                null,
                                null)));

                        bl6.setBubbleSpec(BubbleSpec.FACE_LEFT_CENTER);
                        x.setAlignment(Pos.TOP_RIGHT);
                        x.getChildren().addAll(bl6);
                    } else {
                        bl6.setBackground(new Background(new BackgroundFill(Color.WHITE,
                                null,
                                null)));
                        bl6.setBubbleSpec(BubbleSpec.FACE_RIGHT_CENTER);
                        x.setAlignment(Pos.TOP_LEFT);
                        x.getChildren().addAll(bl6);
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(x);
                }
            };
        }
    }

    private static class ChatCellFactory implements Callback<ListView<Chat>, ListCell<Chat>> {

        @Override
        public ListCell<Chat> call(ListView<Chat> param) {
            return new ListCell<Chat>() {
                @Override
                protected void updateItem(Chat chat, boolean empty) {
                    super.updateItem(chat, empty);
                    if (empty || Objects.isNull(chat)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    HBox wrapper = new HBox();
                    // TODO: member>=3
                    Label chatNameLabel = new Label(chat.getChatName());
                    if (chat.getMembers().size() > 3) {
                        chatNameLabel.setText(
                                chat.getThree() + "..." + "(" + chat.getMembers().size() + ")"
                        );
                    }
                    chatNameLabel.setWrapText(true);
                    chatNameLabel.setPrefSize(50, 20);
                    wrapper.setAlignment(Pos.CENTER);
                    wrapper.getChildren().add(chatNameLabel);

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
