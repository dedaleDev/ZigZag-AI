package com.flavientech;
import javax.sound.sampled.*;
import java.io.File;


public class SoundPlayer {
    private String path;
    public SoundPlayer(String path){
        this.path = path;
        this.runSound();
    }

    public void runSound(){
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(this.path));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            Thread.sleep(clip.getMicrosecondLength() / 1000);
            clip.close();
            audioInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
