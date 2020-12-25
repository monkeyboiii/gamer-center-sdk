package util;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.Asserts;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RequestManager {

    private static String ip = "47.115.50.249";
    private static final Map<RequestType, URI> uris;


    public RequestManager() {
    }


    static {
        uris = new HashMap<>();
        putURIs();
    }


    private static void putURIs() {
        try {
            uris.put(RequestType.INITIALIZER, new URI("http://" + ip + "/api/user/login"));
            uris.put(RequestType.DELEGATE_LOGIN, new URI("http://" + ip + "/api/user/login/game"));
//                uris.put(RequestType.DELEGATE_LOGIN, new URI("http://" + ip + "/api/user/login"));

            uris.put(RequestType.GAME, new URI("http://" + ip + "/api/developer/game"));
            uris.put(RequestType.GAMES, new URI("http://" + ip + "/api/developer/games"));
            uris.put(RequestType.GAME_DLC, new URI("http://" + ip + "/game/dlc/list"));
            uris.put(RequestType.GAME_DLC_DOWNLOAD, new URI("http://" + ip + "/game/dlc/download"));

            uris.put(RequestType.PLAYER_OF_GAME, new URI("http://" + ip + "/api/developer/player/to/game?game_id="));
            uris.put(RequestType.NOTIFICATION, new URI("http://" + ip + "/api/developer/player/notify"));

            uris.put(RequestType.CLOUD_LIST, new URI("http://" + ip + "/game/cloud/list"));
            uris.put(RequestType.CLOUD_UPLOAD, new URI("http://" + ip + "/game/cloudUpload"));
            uris.put(RequestType.CLOUD_DOWNLOAD, new URI("http://" + ip + "/game/cloudDownload"));

            uris.put(RequestType.DELEGATE_USER_INFO, new URI("http://" + ip + "/api/user/info"));
            uris.put(RequestType.DELEGATE_USER_MESSAGE, new URI("http://" + ip + "/api/user/friend/chat"));
            uris.put(RequestType.DELEGATE_PURCHASED_DLC, new URI("http://" + ip + "/game/dlc/purchase"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public static void setIp(String ip) {
        RequestManager.ip = ip;
        putURIs();
    }


    public enum RequestType {
        INITIALIZER,  // set token in request manager

        GAME,
        GAMES,  // return List<Game>
        GAME_DLC,  // return List<GameDLC>
        GAME_DLC_DOWNLOAD, // return inputstream

        PLAYER_OF_GAME,  // return GameInfo
        NOTIFICATION,

        CLOUD_LIST,  // return List<String>
        CLOUD_UPLOAD,  // return String(receipt)
        CLOUD_DOWNLOAD, // return file

        // delegate methods for user login
        DELEGATE_LOGIN,  //
        DELEGATE_USER_INFO,  //
        DELEGATE_USER_MESSAGE,  //
        DELEGATE_PURCHASED_DLC  //
        ;
    }


    //
    //
    //
    //
    //


    /**
     * dev's credentials
     */
    private Long id;
    private String name;
    private String token;


    /**
     * handles multiple type of request
     * refresh map b4 and after each call
     */
    public void sendRequest(RequestType type, Map<String, String> map) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();


        // log in
        if (type.equals(RequestType.INITIALIZER) || type.equals(RequestType.DELEGATE_LOGIN)) {
            // illegal form
            if (!map.containsKey("email") || !map.containsKey("password")) {
                client.close();
                throw new Exception("Email and password needed in login");
            } else if (type.equals(RequestType.DELEGATE_LOGIN) && !map.containsKey("game_id")) {
                client.close();
                throw new Exception("Game id need needed in delegate login");
            }

            // initate request object
            HttpPost request = new HttpPost(uris.get(type));

            // form
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("email", map.get("email")));
            params.add(new BasicNameValuePair("password", map.get("password")));
            if (map.containsKey("role")) params.add(new BasicNameValuePair("role", map.get("role")));
            if (map.containsKey("game_id")) params.add(new BasicNameValuePair("game_id", map.get("game_id")));
            request.setEntity(new UrlEncodedFormEntity(params));

            // get response
            JSONObject json = getJsonObjectSafe(client.execute(request));

            // return to map
            map.put("token", json.getJSONObject("data").get("token").toString());
            map.put("id", json.getJSONObject("data").get("user_id").toString());
            map.put("name", json.getJSONObject("data").get("user_name").toString());

            // set dev info
            if (map.containsKey("role") && map.get("role").equals("d")) {
                token = map.get("token");
                id = Long.valueOf(map.get("id"));
                name = map.get("name");
            }


        }


        // check dev token health
        if (token.length() == 0) {
            client.close();
            throw new Exception("Dev not logged in");
        }


        // various apis
        if (type.equals(RequestType.GAME)) {
            HttpGet request;

            List<NameValuePair> params = new ArrayList<>();
            if (map.containsKey("game_name")) params.add(new BasicNameValuePair("name", map.get("game_name")));
            else if (map.containsKey("game_id")) params.add(new BasicNameValuePair("id", map.get("game_id")));
            else throw new Exception("Either game name or game id is needed");

            String paramsEncoded = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
            request = new HttpGet(uris.get(type) + "?" + paramsEncoded);
            request.setHeader("token", token);

            JSONObject json = getJsonObjectSafe(client.execute(request));

            map.put("json-game", json.get("data").toString());


        } else if (type.equals(RequestType.GAMES)) {
            List<NameValuePair> params = new ArrayList<>();
            if (map.containsKey("game_tag")) params.add(new BasicNameValuePair("tag", map.get("game_tag")));
            String paramsEncoded = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));

            HttpGet request = new HttpGet(uris.get(type) + "?" + paramsEncoded);
            request.setHeader("token", token);

            JSONObject json = getJsonObjectSafe(client.execute(request));

            map.put("json-dev-games", json.get("data").toString());


        } else if (type.equals(RequestType.GAME_DLC)) {
            HttpGet request;

            if (!map.containsKey("game_id")) {
                client.close();
                throw new Exception("Game id required for DLCs");
            }

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("game_id", map.get("game_id")));
            String paramsEncoded = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
            request = new HttpGet(uris.get(type) + "?" + paramsEncoded);
            request.setHeader("token", token);

            JSONObject json = getJsonObjectSafe(client.execute(request));

            map.put("json-dlc-list", json.get("data").toString());


        } else if (type.equals(RequestType.NOTIFICATION)) {
            // illegal form
            if (!map.containsKey("game_id") && !map.containsKey("user_id")) {
                client.close();
                throw new Exception("Either game id or user id is needed to sent notification");
            }

            HttpPost request = new HttpPost(uris.get(type));

            // support delegate method
            request.setHeader("token", map.get("token"));

            // form
            List<NameValuePair> params = new ArrayList<>();
            if (map.containsKey("type")) params.add(new BasicNameValuePair("type", map.get("type")));
            if (map.containsKey("game_id")) params.add(new BasicNameValuePair("game_id", map.get("game_id")));
            params.add(new BasicNameValuePair("message", map.get("message")));
            if (map.containsKey("user_id")) params.add(new BasicNameValuePair("user_id", map.get("user_id")));
            request.setEntity(new UrlEncodedFormEntity(params));

            JSONObject json = getJsonObjectSafe(client.execute(request));


        } else if (type.equals(RequestType.PLAYER_OF_GAME)) {
            // illegal parameter
            if (!map.containsKey("game_id")) {
                client.close();
                throw new Exception("Parameters not fully supplied in player of game, need game id");
            }

            HttpGet request = new HttpGet(uris.get(type) + map.get("game_id"));
            request.setHeader("token", token);

            JSONObject json = getJsonObjectSafe(client.execute(request));
            map.put("json-player-of-game", json.get("data").toString());


        } else if (type.equals(RequestType.CLOUD_LIST)) {
            HttpGet request;

            if (!map.containsKey("game_id") || !map.containsKey("user_id")) {
                client.close();
                throw new Exception("Game id and user id required in cloud list");
            }

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("game_id", map.get("game_id")));
            params.add(new BasicNameValuePair("user_id", map.get("user_id")));
            String paramsEncoded = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
            request = new HttpGet(uris.get(type) + "?" + paramsEncoded);
            request.setHeader("token", map.get("token"));

            JSONObject json = getJsonObjectSafe(client.execute(request));

            map.put("json-cloud-list", json.get("data").toString());


        } else if (type.equals(RequestType.CLOUD_UPLOAD)) {
            // illegal form
            if (!map.containsKey("game_id") || !map.containsKey("user_id") || !map.containsKey("file")) {
                client.close();
                throw new Exception("Parameters not fully supplied in cloud save");
            }

            HttpPost request = new HttpPost(uris.get(type));
            request.setHeader("token", token);

            // form
            StringBody gameId = new StringBody(map.get("game_id"), ContentType.TEXT_PLAIN);
            StringBody userId = new StringBody(map.get("user_id"), ContentType.TEXT_PLAIN);

            HttpEntity requestEntity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.RFC6532)
                    .addBinaryBody("upload_file", new File(map.get("file")))
                    .addPart("game_id", gameId)
                    .addPart("user_id", userId)
                    .build();
            request.setEntity(requestEntity);

            JSONObject json = getJsonObjectSafe(client.execute(request));


        } else if (type.equals(RequestType.CLOUD_DOWNLOAD)) {
            // illegal parameters
            if (!map.containsKey("game_id") || !map.containsKey("user_id") || !map.containsKey("file")) {
                client.close();
                throw new Exception("Parameters not fully supplied in cloud download");
            }

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("game_id", map.get("game_id")));
            params.add(new BasicNameValuePair("user_id", map.get("user_id")));
            params.add(new BasicNameValuePair("name", map.get("file")));
            String paramsEncoded = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));

            HttpGet request = new HttpGet(uris.get(type) + "?" + paramsEncoded);
            request.setHeader("token", token);

            CloseableHttpResponse response = client.execute(request);
            Asserts.check(response.getStatusLine().getStatusCode() == 200, response.getEntity().getContent().toString());
            InputStream inputStream = response.getEntity().getContent();
            JSONObject json = new JSONObject(IOUtils.toString(inputStream));
            Asserts.check(json.getInt("code") == 0, json.get("msg").toString());


        } else if (type.equals(RequestType.DELEGATE_USER_INFO)) {
            if (!map.containsKey("token")) {
                client.close();
                throw new Exception("Need user login token");
            }

            HttpGet request = new HttpGet(uris.get(type));
            request.setHeader("token", map.get("token"));

            CloseableHttpResponse response = client.execute(request);
            Asserts.check(response.getStatusLine().getStatusCode() == 200, response.getEntity().getContent().toString());
            InputStream inputStream = response.getEntity().getContent();
            JSONObject json = new JSONObject(IOUtils.toString(inputStream));
            Asserts.check(json.getInt("code") == 0, json.get("msg").toString());

            map.put("json-player-info", json.get("data").toString());


        } else if (type.equals(RequestType.DELEGATE_USER_MESSAGE)) {
            // illegal form
            if (!map.containsKey("token") || !map.containsKey("to_id")) {
                client.close();
                throw new Exception("Incomplete parameters in delegate message");
            }

            HttpPost request = new HttpPost(uris.get(type));

            // support delegate method
            request.setHeader("token", map.get("token"));

            // form
            List<NameValuePair> params = new ArrayList<>();
            if (map.containsKey("type")) params.add(new BasicNameValuePair("type", map.get("type")));
            params.add(new BasicNameValuePair("message", map.get("message")));
            params.add(new BasicNameValuePair("to_id", map.get("to_id")));
            request.setEntity(new UrlEncodedFormEntity(params));

            JSONObject json = getJsonObjectSafe(client.execute(request));


        } else if (type.equals(RequestType.DELEGATE_PURCHASED_DLC)) {
            // illegal form
            if (!map.containsKey("user_id") || !map.containsKey("dlc_id")) {
                client.close();
                throw new Exception("Incomplete parameters in delegate purchase DLC");
            }

            HttpPost request = new HttpPost(uris.get(type));

            // support delegate method
            request.setHeader("token", map.get("token"));

            // form
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("id", map.get("dlc_id")));
            request.setEntity(new UrlEncodedFormEntity(params));

            JSONObject json = getJsonObjectSafe(client.execute(request));

        }


        client.close();
    }


    public InputStream sendRequestDownload(RequestType type, Map<String, String> map) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();

        if (type.equals(RequestType.CLOUD_DOWNLOAD)) {
            // illegal parameters
            if (!map.containsKey("game_id") || !map.containsKey("user_id") || !map.containsKey("file")) {
                client.close();
                throw new Exception("Parameters not fully supplied in cloud download");
            }

            // encode parameters into url-acceptable formats
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("game_id", map.get("game_id")));
            params.add(new BasicNameValuePair("user_id", map.get("user_id")));
            params.add(new BasicNameValuePair("name", map.get("file")));
            String paramsEncoded = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));

            HttpGet request = new HttpGet(uris.get(type) + "?" + paramsEncoded);
            request.setHeader("token", token);

            return getInputStreamSafe(client.execute(request));

        } else if (type.equals(RequestType.GAME_DLC_DOWNLOAD)) {
            // illegal parameters
            if (!map.containsKey("dlc_id")) {
                client.close();
                throw new Exception("DLC id not specified in DLC download");
            }

            // encode parameters into url-acceptable formats
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("id", map.get("dlc_id")));
            String paramsEncoded = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));

            HttpGet request = new HttpGet(uris.get(type) + "?" + paramsEncoded);
            request.setHeader("token", map.get("token"));

            return getInputStreamSafe(client.execute(request));
        }


        return null;
    }


    //
    //
    //
    //
    // util


    private JSONObject getJsonObjectSafe(CloseableHttpResponse response) throws IOException {
        Asserts.check(response.getStatusLine().getStatusCode() == 200, response.getEntity().getContent().toString());
        JSONObject json = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
        Asserts.check(json.getInt("code") == 0, json.get("msg").toString());
        return json;
    }


    private InputStream getInputStreamSafe(CloseableHttpResponse response) throws Exception {
        Asserts.check(response.getStatusLine().getStatusCode() == 200, response.getEntity().getContent().toString());
        InputStream inputStream = response.getEntity().getContent();

        ByteArrayOutputStream baos = cloneInputStream(inputStream);

        // two streams for response validation
        assert baos != null;
        InputStream stream1 = new ByteArrayInputStream(baos.toByteArray());
        InputStream stream2 = new ByteArrayInputStream(baos.toByteArray());

        //response should be just InputStream, not JSON
        try {
            JSONObject json = new JSONObject(IOUtils.toString(stream1));
            Asserts.check(json.getInt("code") == 0, json.get("msg").toString());
        } catch (JSONException | IOException e) {
            return stream2;
        }

        throw new Exception("Nothing valid returned");
    }


    private static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
