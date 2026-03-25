package com.example.quickmeetjava;

public class Friend {
    private byte[] profilePic;
    private String username;
    private  String latestMessage;

    public Friend(String username, String latestMessage, byte[] profilePic) {
        this.username = username;
        this.latestMessage = latestMessage;
        this.profilePic = profilePic;
    }
    public byte[] getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(byte[] profilePic) {
        this.profilePic = profilePic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }
}
