package com.ghelius.artifacts.artifacts;


public class TextButton {
    enum State {Normal, True, False};

    String text;
    State state;
    int id;

    TextButton(String text, int id) {
        this.text = text;
        this.state = State.Normal;
        this.id = id;
    }
}
