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
                } catch (java.lang.NumberFormatException e) {
                    sendGroupMsg("天数超过整型上限。");
                    logger.info("天数超过整型上限");
                    logger.info("线程" + this.getName() + "处理完毕，已经退出");
                    return;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                username = msg.substring(6, index - 1);
            } else {
                username = msg.substring(6);
            }
            if (day < 0) {
                sendGroupMsg("天数不能为负值。");
                logger.info("天数不能为负值");
                logger.info("线程" + this.getName() + "处理完毕，已经退出");
                return;
            }


            logger.info("接收到玩家" + username + "的查询请求");
            if ("a".equals(msg.substring(5, 6))) {
                //后期做一个文本版本的，暂时这两个命令没什么区别
                if (msg.contains("#")) {
                    index = msg.indexOf("#");
                    day = Integer.valueOf(msg.substring(index + 1));
                    username = msg.substring(7, index - 1);
                } else {
                    username = msg.substring(8);
                }
            }

            User userFromAPI;

            try {
                logger.info("开始调用API查询" + username + "的信息");
                userFromAPI = apiUtil.getUser(username);
                //优化流程，在此处直接判断用户存不存在
                if (userFromAPI == null) {
                    sendGroupMsg("没有在官网查到这个玩家。");
                    throw new IOException("没有这个玩家");
                }
            } catch (IOException e) {
                logger.error("从api获取玩家" + username + "信息失败");
                logger.error(e.getMessage());
                logger.info("线程" + this.getName() + "处理完毕，已经退出");
                return;
            }

            if (dbUtil.getUserName(userFromAPI.getUsername()) == 0) {
                //玩家初次使用本机器人，直接在数据库登记当天数据
                logger.info("玩家" + userFromAPI.getUsername() + "初次使用本机器人，开始登记");
                dbUtil.addUserName(userFromAPI.getUsername());
                dbUtil.addUserInfo(userFromAPI);

            }

            boolean near = false;
            User userInDB = dbUtil.getUserInfo(username, day);
            if (userInDB == null) {
                //如果第一次没取到
                userInDB = dbUtil.getNearestUserInfo(username, day);
                near = true;
            }
            String role = dbUtil.getUserRole(username);

            String filename = imgUtil.drawUserInfo(userFromAPI, userInDB, role, day, near);
            if (filename.equals("error")) {
                sendGroupMsg("绘图过程中发生致命错误。");
            }
            //由于带[]的玩家，生成的文件名会导致返回出错，直接在imgUtil改为用数字id生成文件
            sendGroupMsg("[CQ:image,file=" + filename + "]");
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


        if ("bp".equals(msg.substring(1, 3))) {
            String username = msg.substring(4);
            logger.info("接收到玩家" + username + "的BP查询请求");
            List<BP> list;
            User user;
            //代码复用性还是低了点，如果以后再加功能考虑重构
            //调用getNearestUserInfo方法，查询数据库中是否有该用户名的记录
            if (dbUtil.getUserName(username) != 0) {

                user = dbUtil.getNearestUserInfo(username, 1);
            } else {
                //是个没用过白菜的人呢
                try {
                    user = apiUtil.getUser(username);
                    if (user == null) {
                        sendGroupMsg("没有在官网查到这个玩家。");
                        throw new IOException("没有这个玩家");
                    }
                } catch (IOException e) {
                    logger.error("从api获取玩家" + username + "信息失败");
                    logger.error(e.getMessage());
                    logger.info("线程" + this.getName() + "处理完毕，已经退出");
                    return;
                }
                //玩家初次使用本机器人，直接在数据库登记当天数据
                logger.info("玩家" + username + "初次使用本机器人，开始登记");
                dbUtil.addUserName(username);
                dbUtil.addUserInfo(user);


            }


            try {
                logger.info("开始获取玩家" + user.getUsername() + "的今日BP");
                list = apiUtil.getTodayBP(user.getUsername());
                if (list.size() == 0) {
                    sendGroupMsg("玩家" + user.getUsername() + "今天没有更新BP。");
                    throw new IOException("没有查到该玩家今天更新的BP");
                }
            } catch (IOException e) {
                logger.error("从api获取玩家" + user.getUsername() + "今日BP信息失败");
                logger.error(e.getMessage());
                logger.info("线程" + this.getName() + "处理完毕，已经退出");
                return;
            }


            //保证list不为空
            for (BP aList : list) {
                //对BP进行遍历，请求API将名称写入
                String name = null;
                int retry = 0;
                while (retry < 5) {
                    try {
                        logger.info("正在获取Beatmap id为" + aList.getBeatmap_id() + "的谱面的名称");
                        name = apiUtil.getMapName(aList.getBeatmap_id());
                        //如果成功就跳出循环
                        break;
                    } catch (IOException e) {
                        logger.error("从api获取谱面" + aList.getBeatmap_id() + "的名称失败");
                        logger.error(e.getMessage());
                        logger.error("开始重试，第" + (retry + 1) + "次");
                        retry++;
                    }
                }
                if (retry == 5) {
                    logger.error("从api获取谱面" + aList.getBeatmap_id() + "的名称失败");
                    sendGroupMsg("从api获取谱面" + aList.getBeatmap_id() + "的名称失败");
                    return;
                }
                aList.setBeatmap_name(name);
            }
            logger.info("正在绘制今日BP");
            String filename = imgUtil.drawUserBP(user, list);
            if (filename.equals("error")) {
                sendGroupMsg("绘图过程中发生致命错误：本地资源读取失败。");
            }
            sendGroupMsg("[CQ:image,file=" + filename + "]");
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