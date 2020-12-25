package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.*;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static util.RequestManager.RequestType;


public class DeveloperSDK {

    private final RequestManager requestManager = new RequestManager();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create(); // deserializer


    //
    //
    //
    //
    // dev-oriented methods


    private final Map<String, String> map = new HashMap<>() {
        {
            put("role", "d"); // credentials
        }
    };
    private final Map<String, String> requestResults = new HashMap<>();
    private List<Game> games;


    /**
     * get a single game
     */
    public Game getGameByName(String name) throws Exception {
        map.put("game_name", name);
        requestManager.sendRequest(RequestType.GAME, map);

        String original = map.get("json-game");
        Game game = gson.fromJson(original, Game.class);
        requestResults.put("game-" + game.getId().toString(), original);

        return game;
    }

    public Game getGameById(Long id) throws Exception {
        map.put("game_id", id.toString());
        requestManager.sendRequest(RequestType.GAME, map);

        String original = map.get("json-game");
        Game game = gson.fromJson(original, Game.class);
        requestResults.put("game-" + game.getId().toString(), original);

        return game;
    }

    public List<GameDLC> getGameDLCs(Long gameId) throws Exception {
        map.put("game_id", gameId.toString());
        requestManager.sendRequest(RequestType.GAME_DLC, map);

        String original = map.get("json-dlc-list");
        List<GameDLC> gameDLCs = gson.fromJson(original, new TypeToken<List<GameDLC>>() {
        }.getType());
        requestResults.put("dlc-list", original);

        return gameDLCs;
    }


    /**
     * get all games belonging to dev,
     * can supply with tag to filter
     */
    public List<Game> getDevGames() throws Exception {
        requestManager.sendRequest(RequestType.GAMES, map);

        String original = map.get("json-dev-games");
        games = gson.fromJson(original, new TypeToken<List<Game>>() {
        }.getType());
        requestResults.put("dev-games", original);

        return games;
    }

    public List<Game> getDevGames(String tag) throws Exception {
        map.put("tag", tag);

        return getDevGames();
    }


    /**
     * dev only able to see players of his/her own game
     */
    public GameInfo getPlayersOfGame(Long gameId) throws Exception {
        map.put("game_id", gameId.toString());
        requestManager.sendRequest(RequestType.PLAYER_OF_GAME, map);

        String original = map.get("json-player-of-game");
        List<User> users = gson.fromJson(original, new TypeToken<List<User>>() {
        }.getType());
        GameInfo gameInfo = new GameInfo(getGameById(gameId), users);
        requestResults.put("game-info-" + gameInfo.getId().toString(), original);

        return gameInfo;
    }

    public GameInfo getPlayersOfGame(Game game) throws Exception {
        map.put("game_id", game.getId().toString());
        requestManager.sendRequest(RequestType.PLAYER_OF_GAME, map);

        String original = map.get("json-player-of-game");
        List<User> users = gson.fromJson(original, new TypeToken<List<User>>() {
        }.getType());
        GameInfo gameInfo = new GameInfo(game, users);
        requestResults.put("game-info-" + gameInfo.getId().toString(), original);

        return gameInfo;
    }


    /**
     * send message from dev
     * message content specification is a must
     * userId and gameId is alternative
     * type is optional
     * <p>
     * when broadcasting to all users of the game, specify broadcast parameter explicitly
     */
    public void sendNotification(String message, Long gameId, Boolean broadcast) throws Exception {
        sendNotification(message, "promotion", gameId, broadcast);
    }

    public void sendNotification(String message, Long userId) throws Exception {
        sendNotification(message, "promotion", userId);
    }

    public void sendNotification(String message, String type, Long gameId, Boolean broadcase) throws Exception {
        map.put("message", message);
        map.put("type", type);
        map.put("game_id", gameId.toString());
        requestManager.sendRequest(RequestType.NOTIFICATION, map);
    }

    public void sendNotification(String message, String type, Long userId) throws Exception {
        map.put("message", message);
        map.put("type", type);
        map.put("user_id", userId.toString());
        requestManager.sendRequest(RequestType.NOTIFICATION, map);
    }


    /**
     * save progress of players or just random files on server
     */
    private static final String tempDir = System.getProperty("java.io.tmpdir");

    public void cloudUpload(Long game_id, Long user_id, String file) throws Exception {
        map.put("game_id", game_id.toString());
        map.put("user_id", user_id.toString());
        map.put("file", file);
        requestManager.sendRequest(RequestType.CLOUD_UPLOAD, map);
    }

    public void cloudUpload(Long game_id, Long user_id, InputStream inputStream, String fileName) throws Exception {
        File targetFile = null;
        try {
            targetFile = new File(tempDir + File.separator + fileName);
            java.nio.file.Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            map.put("game_id", game_id.toString());
            map.put("user_id", user_id.toString());
            map.put("file", targetFile.getAbsolutePath());
            requestManager.sendRequest(RequestType.CLOUD_UPLOAD, map);
            targetFile.delete();
        }
    }

    public InputStream cloudDownload(Long game_id, Long user_id, String file) throws Exception {
        map.put("game_id", game_id.toString());
        map.put("user_id", user_id.toString());
        map.put("file", file);
        return requestManager.sendRequestDownload(RequestType.CLOUD_DOWNLOAD, map);
    }

    public List<String> cloudList(Long game_id, Long user_id) throws Exception {
        delegateMap.put("game_id", game_id.toString());
        delegateMap.put("user_id", user_id.toString());
        requestManager.sendRequest(RequestType.CLOUD_LIST, delegateMap);

        String original = delegateMap.get("json-cloud-list");
        List<String> cloudList = gson.fromJson(original, new TypeToken<List<String>>() {
        }.getType());
        requestResults.put("cloud-list", original);

        return cloudList;

    }

    public void cloudDownload(Long game_id, Long user_id, String file, String path) throws Exception {
        map.put("game_id", game_id.toString());
        map.put("user_id", user_id.toString());
        map.put("file", file);  // cloud upload file name
        map.put("path", path);  // local path to save file

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(path)));
        InputStream is = requestManager.sendRequestDownload(RequestType.CLOUD_DOWNLOAD, map);
        BufferedInputStream bis = new BufferedInputStream(is);
        int inByte;
        while ((inByte = bis.read()) != -1) bos.write(inByte);
        bis.close();
        bos.close();
    }


    //
    //
    //
    //
    // player-oriented, delegate methods


    /*
      This part of the implementation along with the backend design is highly insecure.
      Nevertheless, due to the limitation on me, myself and I,
      the method is applied instead of the OAuth version
     */


    /**
     * default-ruled/dev-defined users
     */
    private final List<User> users = new ArrayList<>();
    private final Map<String, String> delegateMap = new HashMap<>();


    public void placeHolder() {

    }


    public User login(String email, String password, Long game_id) throws Exception {
        delegateMap.put("email", email);
        delegateMap.put("password", password);
        delegateMap.put("game_id", game_id.toString());
        requestManager.sendRequest(RequestType.DELEGATE_LOGIN, delegateMap);

        User player = new User();
        player.setId(Long.valueOf(delegateMap.get("id")));
        player.setName(delegateMap.get("name"));
        player.setToken(delegateMap.get("token"));
        users.add(player);

        return player;
    }


    public UserInfo getPlayerInfo(Long userId) throws Exception {
        Optional<User> player = users.stream().filter(u -> u.getId().equals(userId)).findFirst();
        if (player.isEmpty() || player.get().getToken().length() == 0) throw new Exception("Player not logged in");

        delegateMap.put("token", player.get().getToken());
        requestManager.sendRequest(RequestType.DELEGATE_USER_INFO, delegateMap);

        return gson.fromJson(delegateMap.get("json-player-info"), UserInfo.class);
    }


    public void sendMessage(String message, String type, Long from_id, Long to_id) throws Exception {
        Optional<User> player = users.stream().filter(u -> u.getId().equals(from_id)).findFirst();
        if (player.isEmpty() || player.get().getToken().length() == 0) throw new Exception("Player not logged in");

        delegateMap.put("message", message);
        delegateMap.put("type", type);
        delegateMap.put("token", player.get().getToken());
        delegateMap.put("to_id", to_id.toString());

        requestManager.sendRequest(RequestType.DELEGATE_USER_MESSAGE, delegateMap);
    }


    public void purchaseDLC(Long user_id, Long dlc_id) throws Exception {
        Optional<User> player = users.stream().filter(u -> u.getId().equals(user_id)).findFirst();
        if (player.isEmpty() || player.get().getToken().length() == 0) throw new Exception("Player not logged in");

        delegateMap.put("user_id", user_id.toString());
        delegateMap.put("dlc_id", dlc_id.toString());

        requestManager.sendRequest(RequestType.DELEGATE_PURCHASED_DLC, delegateMap);
    }

    // download dlc content
    public InputStream getDLC(Long user_id, Long dlc_id) throws Exception {
        delegateMap.put("user_id", user_id.toString());
        delegateMap.put("dlc_id", dlc_id.toString());

        return requestManager.sendRequestDownload(RequestType.GAME_DLC_DOWNLOAD, delegateMap);
    }


    //
    //
    //
    //
    // result


    public List<User> getUsers() {
        return users;
    }

    public List<Game> getGames() {
        return games;
    }


    public void persistResults(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) file.delete();
        file.createNewFile();

        if (file.canWrite()) {
            PrintWriter printWriter = new PrintWriter(new FileOutputStream(file));
            try {
                users.stream().map(JSONObject::new).map(JSONObject::toString).forEach(printWriter::println);
                games.stream().map(JSONObject::new).map(JSONObject::toString).forEach(printWriter::println);
                requestResults.entrySet().stream().map(JSONObject::new).map(JSONObject::toString).forEach(printWriter::write);
            } catch (Exception e) {
                e.printStackTrace();
            }
            printWriter.close();
            System.out.println("persisted");
        } else {
            throw new FileNotFoundException("File of path " + path + " is wrong");
        }
    }


    //
    //
    //
    //
    // init


    /**
     * dev's credentials for dev-api privileges
     */
    public DeveloperSDK(String email, String password) throws Exception {
        map.put("email", email);
        map.put("password", password);
        devLogin();
    }

    public DeveloperSDK(String email, String password, String ip) throws Exception {
        map.put("email", email);
        map.put("password", password);
        setIp(ip);
        devLogin();
    }

    public DeveloperSDK() {
    }

    public void setIp(String ip) {
        RequestManager.setIp(ip);
    }

    public void setEmail(String email) {
        map.put("email", email);
    }

    public void setPassword(String password) {
        map.put("password", password);
    }

    public void serRole(String role) {
        map.put("role", role);
    }

    public void devLogin(String email, String password) throws Exception {
        map.put("email", email);
        map.put("password", password);
        devLogin();
    }

    public void devLogin(String email, String password, String role) throws Exception {
        map.put("email", email);
        map.put("password", password);
        map.put("role", role);
        devLogin();
    }

    public void devLogin() throws Exception {
        if (map.containsKey("email") && map.containsKey("password")) {
            requestManager.sendRequest(RequestType.INITIALIZER, map);
        } else {
            throw new Exception("Email and password should be provided");
        }
    }


}
