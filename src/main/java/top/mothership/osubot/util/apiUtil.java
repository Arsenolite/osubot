package top.mothership.osubot.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.Map;
import top.mothership.osubot.pojo.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class apiUtil {
    private final String getUserURL = "https://osu.ppy.sh/api/get_user";
    private final String getBPURL = "https://osu.ppy.sh/api/get_user_best";
    private final String getMapURL = "https://osu.ppy.sh/api/get_beatmaps";
    private final String getRecentURL = "https://osu.ppy.sh/api/get_user_recent";
    private Logger logger = LogManager.getLogger(this.getClass());
    private String key;

    //构造器内读取配置文件
    public apiUtil() {
        ResourceBundle rb = ResourceBundle.getBundle("cabbage");
        this.key = rb.getString("key");
    }


    //用来请求API，获取用户数据的方法
    public User getUser(String username, int userId) {
        String URL;
        if(username!=null&&userId==0){

            URL = getUserURL + "?k=" + key + "&type=string&u=" + username.replaceAll(" ","_");
        }else if(username==null&&userId!=0){
            URL = getUserURL + "?k=" + key + "&type=id&u=" + userId;
        }else{
            logger.error("不可同时指定用户名和用户id。");
            return null;
        }

        HttpURLConnection httpConnection;
        String output = null;
        int retry = 0;
        while (retry < 8) {
            try {
                httpConnection =
                        (HttpURLConnection) new URL(URL).openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Accept", "application/json");

                httpConnection.setConnectTimeout((int)Math.pow(2,retry)*1000);
                httpConnection.setReadTimeout((int)Math.pow(2,retry)*1000);
                if (httpConnection.getResponseCode() != 200) {
                    logger.error("HTTP GET请求失败: " + httpConnection.getResponseCode() + "，正在重试第" + (retry + 1) + "次");
                    retry++;
                    continue;
                }
                //读取返回结果
                BufferedReader responseBuffer =
                        new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
                output = responseBuffer.readLine();
                //手动关闭流
                httpConnection.disconnect();
                responseBuffer.close();
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            }

        }
        if (retry == 8) {
            logger.error("玩家" + userId + "请求API获取数据，失败五次");
            return null;
        }
        //去掉两侧的中括号
        output = output.substring(1, output.length() - 1);
        //么个叽，什么jsonlib什么org.json，连个api文档都没有，用Gson算了
        return new Gson().fromJson(output, User.class);
    }

    //用来请求API获取BP的方法
    public List<BP> getAllBP(String username, int userId) {
        String URL;
        if(username!=null&&userId==0){
            URL = getBPURL + "?k=" + key + "&type=string&limit=100&u=" + username.replaceAll(" ","_");
        }else if(username==null&&userId!=0){
            URL = getBPURL + "?k=" + key + "&type=id&limit=100&u=" + userId;
        }else{
            logger.error("不可同时指定用户名和用户id。");
            return null;
        }
        HttpURLConnection httpConnection = null;
        List<BP> list = null;
        int retry = 0;
        while (retry < 8) {
            try {
                httpConnection =
                        (HttpURLConnection) new URL(URL).openConnection();
                //设置请求头
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Accept", "application/json");
                httpConnection.setConnectTimeout((int)Math.pow(2,retry)*1000);
                httpConnection.setReadTimeout((int)Math.pow(2,retry)*1000);
                if (httpConnection.getResponseCode() != 200) {
                    logger.info("HTTP GET请求失败: " + httpConnection.getResponseCode() + "，正在重试第" + (retry + 1) + "次");
                    retry++;
                    continue;
                }
                //读取返回结果
                BufferedReader responseBuffer =
                        new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
                //BP的返回结果有时候会有换行，必须手动拼接
                StringBuilder output = new StringBuilder();
                String tmp;
                while ((tmp = responseBuffer.readLine()) != null) {
                    output.append(tmp);
                }
                //手动关闭流
                httpConnection.disconnect();
                responseBuffer.close();
                logger.info("获得了" + username + "玩家的前100BP");
                //定义返回的List
                Type listType = new TypeToken<List<BP>>() {
                }.getType();
                list = new Gson().fromJson(output.toString(), listType);
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            }
        }
        if (retry == 8) {
            logger.error("玩家" + username + "请求API获取BP，失败五次");
            return null;
        }
        //传入的是北京时间……


        return list;
    }


    public Map getMapDetail(int bid){
        HttpURLConnection httpConnection;
        String output = null;
        int retry = 0;
        while (retry < 8) {
            try {
                httpConnection =
                        (HttpURLConnection) new URL(getMapURL + "?k=" + key + "&b=" + bid).openConnection();
                //设置请求头
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Accept", "application/json");
                httpConnection.setConnectTimeout((int)Math.pow(2,retry)*1000);
                httpConnection.setReadTimeout((int)Math.pow(2,retry)*1000);
                //如果ppy的泡面撒了
                if (httpConnection.getResponseCode() != 200) {
                    logger.info("HTTP GET请求失败: " + httpConnection.getResponseCode() + "，正在重试第" + (retry + 1) + "次");
                    retry++;
                    continue;
                }
                //读取返回结果
                BufferedReader responseBuffer =
                        new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
                output = responseBuffer.readLine();
                //手动关闭流
                httpConnection.disconnect();
                responseBuffer.close();
                //去掉两侧的中括号
                output = output.substring(1, output.length() - 1);
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            }
        }
        if (retry == 8) {
            logger.error("谱面" + bid + "的名称获取失败");
            return null;
        }

        //组装实体类
        //组装返回字符串
        return new Gson().fromJson(output, Map.class);

    }
    public BP getRecentScore(String username, int userId) {
        String URL;
        if(username!=null&&userId==0){
            URL = getRecentURL + "?k=" + key + "&type=string&limit=1&u=" + username.replaceAll(" ","_");
        }else if(username==null&&userId!=0){
            URL = getRecentURL + "?k=" + key + "&type=id&limit=1&u=" + userId;
        }else{
            logger.error("不可同时指定用户名和用户id。");
            return null;
        }
        String output = null;
        HttpURLConnection httpConnection;
        int retry = 0;
        while (retry < 8) {
            try {
                httpConnection =
                        (HttpURLConnection) new URL(URL).openConnection();
                //设置请求头
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Accept", "application/json");
                httpConnection.setConnectTimeout((int)Math.pow(2,retry)*1000);
                httpConnection.setReadTimeout((int)Math.pow(2,retry)*1000);
                if (httpConnection.getResponseCode() != 200) {
                    logger.info("HTTP GET请求失败: " + httpConnection.getResponseCode() + "，正在重试第" + (retry + 1) + "次");
                    retry++;
                    continue;
                }
                //读取返回结果
                BufferedReader responseBuffer =
                        new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));
                //BP的返回结果有时候会有换行，必须手动拼接
                output = responseBuffer.readLine();
                //去掉两侧的中括号
                output = output.substring(1, output.length() - 1);
                //手动关闭流
                httpConnection.disconnect();
                responseBuffer.close();
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            }
        }
        if (retry == 8) {
            logger.error("玩家" + username + "请求API获取最近游戏记录，失败五次");
            return null;
        }


        //组装实体类
        //组装返回字符串
        return new Gson().fromJson(output, BP.class);
    }
}
