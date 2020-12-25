import model.*;
import org.junit.Before;
import org.junit.Test;
import util.DeveloperSDK;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class Main {

    private static String email = "11813010@mail.sustech.edu.cn";
    private static String password = "james123";

    private static String email2 = "11811713@mail.sustech.edu.cn";
    private static String password2 = "calvin123";

    private static String email3 = "alice@gmail.com";
    private static String password3 = "alice123";

    DeveloperSDK sdk;

    @Before
    public void setUp() {
        try {
//            sdk = new DeveloperSDK(email, password, "10.21.20.191");
            sdk = new DeveloperSDK(email, password);
            sdk.setIp("localhost");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void getGameById() throws Exception {
        Game game = sdk.getGameById(4L);
        System.out.println(game.getName());
    }

    @Test
    public void getGameByName() throws Exception {
        String name = "my";
        Game game = sdk.getGameByName(name);

        System.out.println(game.getDescription());
    }

    @Test
    public void getPlayerByGame() throws Exception {
        Game game = new Game();
        game.setId(1L);
        GameInfo gameInfo = sdk.getPlayersOfGame(game);

        System.out.println(gameInfo.getPlayers());
    }

    @Test
    public void getDevGames() throws Exception {
        List<Game> games = sdk.getDevGames();

        System.out.println(games.size());
    }

    @Test
    public void getGameInfoById() throws Exception {
        Long id = 1L;
        GameInfo gameInfo = sdk.getPlayersOfGame(id);

        System.out.println(gameInfo.getPlayers().size());
    }

    @Test
    public void pushNotification() throws Exception {
        Long id = 1L;
        sdk.sendNotification("Try the new game by Calvin!", "broadcast", id, true);

    }

    @Test
    public void cloudUploadAndDownloadAsInputStream() throws Exception {
        String name = "try  try 我的.py";
        String testFile = "D:\\Program\\Project\\gamer-center-developer-sdk\\src\\main\\resources\\" + name;
        Long game_id = 1L;
        Long user_id = 15736L;
        sdk.cloudUpload(game_id, user_id, testFile);

        InputStream inputStream = sdk.cloudDownload(game_id, user_id, name);
        byte[] bytes = inputStream.readAllBytes();
        System.out.println(bytes.length);
    }

    @Test
    public void cloudUploadAndDownloadAsFile() throws Exception {
        String file = "埃隆 马斯克.txt";
        String testFile = "D:\\Program\\Project\\gamer-center-developer-sdk\\src\\main\\resources\\" + file;
        Long game_id = 1L;
        Long user_id = 15738L;
        sdk.cloudUpload(game_id, user_id, testFile);

        sdk.cloudDownload(game_id, user_id, file, "D:\\Program\\Project\\gamer-center-developer-sdk\\src\\main\\resources\\out\\las Vegas.txt");

        System.out.println();
    }

    @Test
    public void userLogin() throws Exception {
        Game game = sdk.getGameByName("中国象棋");
//        User player = sdk.login(email2, password2, game.getId());
        User player = sdk.login(email3, password3, game.getId());
        UserInfo userInfo = sdk.getPlayerInfo(player.getId());

        System.out.println(userInfo);
    }

    @Test
    public void userSendMessage() throws Exception {
        String email = "charlie@foxmail.com";
        String password = "charlie123";
        User player = sdk.login(email, password, 1L);

        sdk.sendMessage("message from dev sdk", "random", player.getId(), 100007L);
    }

    @Test
    public void purchaseDLC() throws Exception {
        String email = "charlie@foxmail.com";
        String password = "charlie123";
        User player = sdk.login(email, password, 1L);

        sdk.purchaseDLC(player.getId(), 2L);
    }

    @Test
    public void cloudDownload() throws Exception {
        Long game_id = 1L;
        Long user_id = 15738L;
        String file = "埃隆 马斯克.txt";
        String path = "D:\\Program\\Project\\gamer-center-developer-sdk\\src\\main\\resources\\out\\";

        sdk.cloudDownload(game_id, user_id, file, path + file);
    }

    @Test
    public void cloudUpload() throws Exception {
        Long game_id = 15L;
        Long user_id = 15739L;
        String file = "埃隆 马斯克.txt";
        String path = "D:\\Program\\Project\\gamer-center-developer-sdk\\src\\main\\resources\\";

        InputStream inputStream = new FileInputStream(new File(path + file));
//        sdk.cloudDownload(game_id, user_id, file, path + file);
        sdk.cloudUpload(game_id, user_id, inputStream, "a random file name.txt");
    }

    @Test
    public void cloudList() throws Exception {
        Long game_id = 1L;
        Long user_id = 15738L;
        User user = sdk.login(email2, password2, 1L);
        List<String> strings = sdk.cloudList(game_id, user_id);
        System.out.println(strings);
    }

    @Test
    public void getGameDLCs() throws Exception {
        Long game_id = 9L;

        List<GameDLC> DLCs = sdk.getGameDLCs(game_id);

        System.out.println();
    }

    @Test
    public void purchaseGameDLC() throws Exception {
        Long game_id = 9L;
        List<GameDLC> DLCs = sdk.getGameDLCs(game_id);
        User user = sdk.login(email2, password2, 1L);

        sdk.purchaseDLC(user.getId(), DLCs.get(1).getId());

        System.out.println();
    }

    @Test
    public void getDLC() throws Exception {
        Long user_id = 1000001L;
        Long dlc_id = 4L;
        User user = sdk.login(email2, password2, 1L);
        InputStream inputStream = sdk.getDLC(user.getId(), dlc_id);
        System.out.println(inputStream.readAllBytes().length);
    }


}
