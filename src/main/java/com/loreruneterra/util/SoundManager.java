package com.loreruneterra.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class SoundManager {

    private static MediaPlayer currentPlayer;

    public static void play(String soundFile) {
        try {
            URL resource = SoundManager.class.getResource("/sounds/" + soundFile);
            if (resource == null) {
                System.err.println("Sonido no encontrado: " + soundFile);
                return;
            }
            Media media = new Media(resource.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(0.4);
            player.play();
            currentPlayer = player;
        } catch (Exception e) {
            System.err.println("Error reproduciendo sonido: " + e.getMessage());
        }
    }
}