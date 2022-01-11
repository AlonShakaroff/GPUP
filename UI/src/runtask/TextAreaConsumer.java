package runtask;

import javafx.scene.control.TextArea;

import java.util.function.Consumer;

public class TextAreaConsumer implements Consumer<String> {
    private TextArea textArea;

    TextAreaConsumer(TextArea textArea){
        this.textArea = textArea;
    }

    @Override
    public void accept(String s) {
        textArea.appendText(s);
    }
}
