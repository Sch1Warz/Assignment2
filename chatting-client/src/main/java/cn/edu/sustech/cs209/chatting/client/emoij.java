package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class emoij extends Application {

    private static final String[] EMOJI_VALUES = { "\u263A", "\uD83D\uDE00", "\uD83D\uDE01", "\uD83D\uDE02" };
    public ComboBox<String> emojiComboBox;
    @Override
    public void start(Stage primaryStage) throws Exception {
        ComboBox<String> emojiComboBox = new ComboBox<>();
        emojiComboBox.getItems().addAll("Smile", "Grinning Face", "Grinning Face with Smiling Eyes", "Face with Tears of Joy");
//        emojiComboBox.setCellFactory(param -> new EmojiListCell());
        emojiComboBox.setOnAction(event -> addEmojiToTextArea(emojiComboBox.getValue()));

        TextArea textArea = new TextArea();

        VBox vbox = new VBox(10, emojiComboBox, textArea);

        Scene scene = new Scene(vbox, 400, 400);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addEmojiToTextArea(String emojiName) {
        int emojiIndex = getIndex(emojiName);
        String emojiValue = EMOJI_VALUES[emojiIndex];

        TextArea textArea = (TextArea) ((VBox) emojiComboBox.getParent()).getChildren().get(1);
        textArea.appendText(emojiValue);
    }

    private int getIndex(String name) {
        for (int i = 0;  i < emojiComboBox.getItems().size();  i++) {
            if (emojiComboBox.getItems().get(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        launch(args);
    }

    // A customized ListCell to show emoji instead of text in the ComboBox.
    private class EmojiListCell extends javafx.scene.control.ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setGraphic(null);
                setText(null);
            } else {
                setText(item);
                setGraphic(new javafx.scene.text.Text(EMOJI_VALUES[emoij.this.getIndex(item)]));
            }
        }
    }
}


