package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class EmojiExample extends Application {

    private TextArea inputTextArea;
    private TextFlow outputTextFlow;

    @Override
    public void start(Stage primaryStage) {
        // create input text area
        inputTextArea = new TextArea();
        inputTextArea.setPrefSize(300, 150);

        // create send button
        Button sendButton = new Button("Send");
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String inputText = inputTextArea.getText();
                String outputText = convertUnicodeToEmoji(inputText);
                addOutputText(outputText);
                inputTextArea.clear();
            }
        });

        // create output text flow
        outputTextFlow = new TextFlow();

        // create vbox and add components
        VBox vbox = new VBox(inputTextArea, sendButton, outputTextFlow);

        // create scene and set stage
        Scene scene = new Scene(vbox, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String convertUnicodeToEmoji(String inputText) {
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
        return outputText;
    }

    private void addOutputText(String outputText) {
        Text textNode = new Text(outputText);
        outputTextFlow.getChildren().add(textNode);
        outputTextFlow.getChildren().add(new Text("\n"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}


