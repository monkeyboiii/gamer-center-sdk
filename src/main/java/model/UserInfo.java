package model;

import java.util.List;


/**
 * A more informed User class, requires user's token
 */
public class UserInfo extends User {


    private List<User> friends;


    /**
     * user owned games, DLCs
     */
    private List<Game> games;
    private List<GameDLC> gameDLCs;


    @Override
    public String toString() {
        return "UserInfo{" +
                "friends=" + friends +
                ", games=" + games +
                ", gameDLCs=" + gameDLCs +
                '}';
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    public List<GameDLC> getGameDLCs() {
        return gameDLCs;
    }

    public void setGameDLCs(List<GameDLC> gameDLCs) {
        this.gameDLCs = gameDLCs;
    }
}
