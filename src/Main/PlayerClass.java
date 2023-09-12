package Main;

public class PlayerClass {
    public String player_name;
    public boolean have_messaged;

    public void setValues(String playerName, Boolean haveMessaged) {
        this.player_name = playerName;
        this.have_messaged = haveMessaged;
    }
}