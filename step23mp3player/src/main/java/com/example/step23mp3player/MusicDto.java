package com.example.step23mp3player;

public class MusicDto {

    private int num;
    private String writer;
    private String title;
    private String artist;
    private String orgFileName;
    private String saveFileName;
    private String regdate;

    public MusicDto() {}

    public MusicDto(int num, String writer, String title, String artist, String orgFileName, String saveFileName, String regdate) {
        this.num = num;
        this.writer = writer;
        this.title = title;
        this.artist = artist;
        this.orgFileName = orgFileName;
        this.saveFileName = saveFileName;
        this.regdate = regdate;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getOrgFileName() {
        return orgFileName;
    }

    public void setOrgFileName(String orgFileName) {
        this.orgFileName = orgFileName;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public void setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
    }

    public String getRegdate() {
        return regdate;
    }

    public void setRegdate(String regdate) {
        this.regdate = regdate;
    }
}
