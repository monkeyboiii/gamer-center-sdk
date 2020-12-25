package model;

import java.util.List;


public class GameInfo extends Game {


    List<User> players;


    public GameInfo(Game game, List<User> users) {
        super(game);
        players = users;
    }


    public List<User> getPlayers() {
        return players;
    }


}