package top.mothership.osubot.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.User;
import top.mothership.osubot.util.apiUtil;
import top.mothership.osubot.util.dbUtil;
import top.mothership.osubot.util.imgUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


public class playerThread extends Thread {
    private String msg;
    private String groupId;
    private WebSocketClient cc;
    private Logger logger = LogManager.getLogger(this.getClass());
    //直接new好不习惯
    //最后大概是调用imgUtil，在imgUtil里调用api工具
    private imgUtil imgUtil = new imgUtil();
    private apiUtil apiUtil = new apiUtil();
    private dbUtil dbUtil = new dbUtil();
    private ResourceBundle rb;

    public playerThread(String msg, String groupId, WebSocketClient cc) {
        this.msg = msg;
        this.groupId = groupId;
        this.cc = cc;
        rb = ResourceBundle.getBundle("cabbage");
    }

    public void sendGroupMsg(String text) {
        String resp = "{\"act\": \"101\", \"groupid\": \"" + groupId + "\", \"msg\":\"" + text + "\"}";
        cc.send(resp);
    }

    public void run() {
        //如果感叹号后面紧跟stat
        if ("stat".equals(msg.substring(1, 5))) {
            int index = 0;
            //默认初始化为1
            int day = 1;
            String username;
            //把stata砍了，日后做其他功能再说
            if (msg.contains("#")) {
                //潜在风险：如果消息长度超过整型限制会出异常，而且不会有任何回复，考虑到这种情况实在太少见不做处理
                index = msg.indexOf("#");
                try {
                    day = Integer.valueOf(msg.substring(index + 1));
                    if (day > (int) ((new Date().getTime() - new SimpleDateFormat("yyyy-MM-dd").parse("2007-09-16").getTime()) / 1000 / 60 / 60 / 24)) {
                        sendGroupMsg("你要找史前时代的数据吗。");
                        logger.info("指定的日期早于osu!首次发布日期");
                        logger.info("线程" + this.getName() + "处理完毕，已经退出");
                        return;
                    }
                    if (day < 0) {
                        sendGroupMsg("白菜不会预知未来。");
                        logger.info("天数不能为负值");
                        logger.info("线程" + this.getName() + "处理完毕，已经退出");
                        return;
                    }
                } catch (java.lang.NumberFormatException e) {
                    sendGroupMsg("假使这些完全……不能用的参数，你再给他传一遍，你等于……你也等于……你也有泽任吧？");
                    logger.info("给的天数不是int值");
                    logger.info("线程" + this.getName() + "处理完毕，已经退出");
                    return;
                } catch (ParseException e) {
                    //由于解析的是固定字符串，不会出异常，无视
                }
                //因为本来id末空格也不影响
                //多读一位可以支持id接#不加空格

                username = msg.substring(6, index);
            } else {
                username = msg.substring(6);
            }
            if("白菜".equals(username)){
                sendGroupMsg("唉，没人疼没人爱，我是地里一颗小白菜。");
                return;
            }
            logger.info("开始调用API查询" + username + "的信息");
            User userFromAPI = apiUtil.getUser(username, 0);
            if (userFromAPI == null) {
                sendGroupMsg("没有在官网查到这个玩家。");
                return;
            }
            if (userFromAPI.getUser_id() == 3) {
                sendGroupMsg("你们总是想查BanchoBot。\\n可是BanchoBot已经很累了，她不想被查。\\n她想念自己的小ppy，而不是被逼着查PP。\\n你有考虑过这些吗？没有！你只考虑过你自己。");
                return;
            }

            if (dbUtil.getUserRole(userFromAPI.getUser_id()).equals("notFound")) {
                //是个没用过白菜的人呢
                //玩家初次使用本机器人，直接在数据库登记当天数据
                logger.info("玩家" + username + "初次使用本机器人，开始登记");
                dbUtil.addUserId(userFromAPI.getUser_id());
                dbUtil.addUserInfo(userFromAPI);
            }

            boolean near = false;
            User userInDB = dbUtil.getUserInfo(userFromAPI.getUser_id(), day);
            if (userInDB == null) {
                //如果第一次没取到
                userInDB = dbUtil.getNearestUserInfo(userFromAPI.getUser_id(), day);
                near = true;
            }
            String role = dbUtil.getUserRole(userFromAPI.getUser_id());

            String filename = imgUtil.drawUserInfo(userFromAPI, userInDB, role, day, near);
            if (filename.equals("error")) {
                sendGroupMsg("绘图过程中发生致命错误。");
            }
            //由于带[]的玩家，生成的文件名会导致返回出错，直接在imgUtil改为用数字id生成文件
            sendGroupMsg("[CQ:image,file=" + filename + "]");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //删掉生成的文件
            File f = new File(rb.getString("path") + "\\data\\image\\" + filename);
            f.delete();

        }


        if ("bp".equals(msg.substring(1, 3))) {
            String username = msg.substring(4);
            logger.info("接收到玩家" + username + "的BP查询请求");
            String param = null;
            if (msg.contains("#")) {
               int index = msg.indexOf("#");
               param = msg.substring(index + 1);
            }

            User user = apiUtil.getUser(username, 0);
            if (user == null) {
                sendGroupMsg("没有在官网查到这个玩家。");
                return;
            }

            if (dbUtil.getUserRole(user.getUser_id()).equals("notFound")) {
                //是个没用过白菜的人呢
                //玩家初次使用本机器人，直接在数据库登记当天数据
                logger.info("玩家" + username + "初次使用本机器人，开始登记");
                dbUtil.addUserId(user.getUser_id());
                dbUtil.addUserInfo(user);
            }
            logger.info("开始获取玩家" + user.getUsername() + "的今日BP");
            List<BP> list = apiUtil.getTodayBP(user.getUsername(), 0);
            if (list.size() == 0) {
                sendGroupMsg("玩家" + user.getUsername() + "今天没有更新BP。");
                logger.info("没有查到该玩家今天更新的BP");
                return;
            }

            for (BP aList : list) {
                //对BP进行遍历，请求API将名称写入
                logger.info("正在获取Beatmap id为" + aList.getBeatmap_id() + "的谱面的名称");
                String name = apiUtil.getMapName(aList.getBeatmap_id());
                aList.setBeatmap_name(name);
            }
            String filename = null;
            if(param==null) {
                logger.info("正在绘制今日BP");
                filename = imgUtil.drawUserBP(user, list);
                if (filename.equals("error")) {
                    sendGroupMsg("绘图过程中发生致命错误：本地资源读取失败。");
                }
                sendGroupMsg("[CQ:image,file=" + filename + "]");
            }else{
                //nearest
                if("n".equals(param)){
                    logger.info("正在绘制最近BP");
                    //对list中的bp按日期排序
                    BP temp = null;
                    int size = list.size();
                    for(int i = 0 ; i < size-1; i ++)
                    {
                        for(int j = 0 ;j < size-1-i ; j++)
                        {   //如果j比j+1晚
                            if(list.get(j).getDate().after(list.get(j+1).getDate()))
                            {   //把j+1给j 把j给j+1
                                temp = list.get(j);
                                list.set(j,list.get(j+1));
                                list.set(j+1,temp);
                            }
                        }
                    }
                    BP bp = list.get(0);
                    filename = imgUtil.drawOneBP(user, bp);
                    if (filename.equals("error")) {
                        sendGroupMsg("绘图过程中发生致命错误：本地资源读取失败。");
                    }
                    sendGroupMsg("[CQ:image,file=" + filename + "]");
                }else{
                    sendGroupMsg("不，不行，不能这样");
                    logger.info("传入了没有用的参数");
                    logger.info("线程" + this.getName() + "处理完毕，已经退出");
                    return;
                }
            }
            try {
                logger.info("线程暂停两秒，以免发送成功前删除文件");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //删掉生成的文件
            File f = new File(rb.getString("path") + "\\data\\image\\" + filename);
            f.delete();
        }



        logger.info("线程" + this.getName() + "处理完毕，已经退出");
    }

}
