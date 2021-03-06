package top.mothership.osubot.util;

import cc.plural.jsonij.JSON;
import cc.plural.jsonij.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.mothership.osubot.pojo.BP;
import top.mothership.osubot.pojo.Map;
import top.mothership.osubot.pojo.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class imgUtil {
    //不用this，直接指明类就可以了
    private String cmd = "\"" + rb.getString("path") + "\\data\\image\\resource\\oppai.exe\" "
            + "\"" + rb.getString("path") + "\\data\\image\\resource\\osu\\";
    private static Logger logger = LogManager.getLogger("imgUtil.class");
    private static ResourceBundle rb = ResourceBundle.getBundle("cabbage");
    private static List<BufferedImage> Images;
    private static List<BufferedImage> Nums;
    private static List<BufferedImage> Mods;
    private static BufferedImage A;
    private static BufferedImage B;
    private static BufferedImage C;
    private static BufferedImage D;
    private static BufferedImage X;
    private static BufferedImage XH;
    private static BufferedImage S;
    private static BufferedImage SH;
    private static BufferedImage zPP;
    private static BufferedImage zPPTrick;

    static {
        final Path resultPath = Paths.get(rb.getString("path") + "\\data\\image\\resource\\result");
        //使用NIO扫描文件夹
        final List<File> resultFiles = new ArrayList<>();
        Images = new ArrayList<>();
        Nums = new ArrayList<>();
        Mods = new ArrayList<>();
        SimpleFileVisitor<Path> resultFinder = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                logger.debug(file.getFileName().toString());
                resultFiles.add(file.toFile());
                return super.visitFile(file, attrs);
            }
        };

        try {
            //将所有文件分为三个List
            java.nio.file.Files.walkFileTree(resultPath, resultFinder);
            for (int i = 0; i < 23; i++) {
                Images.add(ImageIO.read(resultFiles.get(i)));
            }
            for (int i = 23; i < 37; i++) {
                Nums.add(ImageIO.read(resultFiles.get(i)));
            }
            for (int i = 37; i < 48; i++) {
                Mods.add(ImageIO.read(resultFiles.get(i)));
            }
            zPP = ImageIO.read(resultFiles.get(48));
            zPPTrick = ImageIO.read(resultFiles.get(49));
        } catch (IOException e) {
            logger.error("读取result相关资源失败");
            logger.error(e.getMessage());
        }

        try {

            A = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("A")));
            B = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("B")));
            C = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("C")));
            D = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("D")));
            X = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("X")));
            XH = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("XH")));
            S = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("S")));
            SH = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("SH")));

        } catch (IOException e) {
            logger.error("读取bp相关资源失败");
            logger.error(e.getMessage());
        }


    }

    /*设计这个方法
    它应该输入username，应该输出一张完整的stat图的路径
    */
    private pageUtil pageUtil = new pageUtil();

    private void draw(Graphics2D g2, String color, String font, String size, String text, String x, String y) {
        //指定颜色
        g2.setPaint(Color.decode(rb.getString(color)));
        //指定字体
        g2.setFont(new Font(rb.getString(font), Font.PLAIN, Integer.decode(rb.getString(size))));
        //指定坐标
        g2.drawString(text, Integer.decode(rb.getString(x)), Integer.decode(rb.getString(y)));

    }

    private String drawImage(BufferedImage bg, User userFromAPI) {
        try {
            ImageIO.write(bg, "png", new File(rb.getString("path") + "\\data\\image\\" + userFromAPI.getUser_id() + ".png"));
            bg.flush();
            return userFromAPI.getUser_id() + ".png";
        } catch (IOException e) {
            logger.error("绘制图片成品失败");
            logger.error(e.getMessage());
            return "error";
        }
    }

    public List<String> convertMOD(Integer bp) {
        String modBin = Integer.toBinaryString(bp);
        //反转mod
        modBin = new StringBuffer(modBin).reverse().toString();
        List<String> mods = new ArrayList<>();
        char[] c = modBin.toCharArray();
        for (int i = c.length - 1; i >= 0; i--) {
            if (c[i] == '1') {
                //字符串中第i个字符是1,意味着第i+1个mod被开启了
                switch (i) {
                    case 0:
                        mods.add("NF");
                        break;
                    case 1:
                        mods.add("EZ");
                        break;
                    case 3:
                        mods.add("HD");
                        break;
                    case 4:
                        mods.add("HR");
                        break;
                    case 5:
                        mods.add("SD");
                        break;
                    case 6:
                        mods.add("DT");
                        break;
                    case 8:
                        mods.add("HT");
                        break;
                    case 9:
                        mods.add("NC");
                        break;
                    case 10:
                        mods.add("FL");
                        break;
                    case 12:
                        mods.add("SO");
                        break;
                    case 14:
                        mods.add("PF");
                        break;
                }

            }
        }
        if (mods.contains("NC")) {
            mods.remove("DT");
        }
        if (mods.contains("PF")) {
            mods.remove("SD");
        }
        return mods;
    }

    public String drawUserInfo(User userFromAPI, User userInDB, String role, int day, boolean near, int scoreRank) {

        //准备资源：背景图和用户头像，以及重画之后的用户头像
        BufferedImage ava = null;
        BufferedImage bg;
        BufferedImage layout = null;
        BufferedImage resizedAva;
        BufferedImage scoreRankBG = null;
        BufferedImage roleImg;
        try {
            layout = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\stat" + rb.getString("layout")));
            roleImg = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\stat\\role-" + role + ".png"));
            scoreRankBG = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\stat" + rb.getString("scoreRankBG")));
        } catch (IOException e) {
            logger.error("读取stat的本地资源失败");
            logger.error(e.getMessage());
            return "error";
        }
        try {
            bg = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\stat\\" + userFromAPI.getUser_id() + ".png"));
        } catch (IOException e) {
            try {
                bg = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\stat\\" + role + ".png"));
            } catch (IOException e1) {
                logger.error("读取stat的本地资源失败");
                logger.error(e1.getMessage());
                return "error";

            }


        }

        //将布局图片初始化
        Graphics2D g2 = (Graphics2D) bg.getGraphics();
        //绘制布局和用户组
        g2.drawImage(roleImg, 0, 0, null);
        g2.drawImage(layout, 0, 0, null);
        logger.info("正在获取头像");
        try {
            //此处传入的应该是用户的数字id
            ava = pageUtil.getAvatar(userFromAPI.getUser_id());
        } catch (IOException | NullPointerException e) {
            logger.error("从官网获取头像失败");
            logger.error(e.getMessage());
        }

        if (ava != null) {
            //进行缩放
            if (ava.getHeight() > 128 || ava.getWidth() > 128) {
                //获取原图比例，将较大的值除以128，然后把较小的值去除以这个f
                int resizedHeight;
                int resizedWidth;
                if (ava.getHeight() > ava.getWidth()) {
                    float f = (float) ava.getHeight() / 128;
                    resizedHeight = 128;
                    resizedWidth = (int) (ava.getWidth() / f);
                } else {
                    float f = (float) ava.getWidth() / 128;
                    resizedHeight = (int) (ava.getHeight() / f);
                    resizedWidth = 128;
                }
                resizedAva = new BufferedImage(resizedWidth, resizedHeight, ava.getType());
                Graphics2D g = (Graphics2D) resizedAva.getGraphics();
                //这么重画有点锯齿严重，试试其他办法
//              g.drawImage(ava, 0, 0, resizedWidth, resizedHeight, null);
                g.drawImage(ava.getScaledInstance(resizedWidth, resizedHeight, Image.SCALE_SMOOTH), 0, 0, resizedWidth, resizedHeight, null);
                g.dispose();
                ava.flush();
            } else {
                //如果不需要缩小，直接把引用转过来
                resizedAva = ava;
            }
            resizedAva.flush();

            //先把头像画上去
            g2.drawImage(resizedAva, Integer.decode(rb.getString("avax")), Integer.decode(rb.getString("avay")), null);
        }


        //绘制文字
        //开启平滑
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //绘制用户名
        draw(g2, "unameColor", "unameFont", "unameSize", userFromAPI.getUsername(), "namex", "namey");

        //绘制Rank
        draw(g2, "defaultColor", "numberFont", "rankSize", "#" + userFromAPI.getPp_rank(), "rankx", "ranky");

        //绘制PP
        draw(g2, "ppColor", "numberFont", "ppSize", String.valueOf(userFromAPI.getPp_raw()), "ppx", "ppy");


        if (scoreRank > 0) {
            //把scorerank用到的bg画到bg上
            g2.drawImage(scoreRankBG, 653, 7, null);
            if (scoreRank < 100) {
                draw(g2, "scoreRankColor", "scoreRankFont", "scoreRankSize", "#" + Integer.toString(scoreRank), "scoreRank2x", "scoreRank2y");
            } else {
                draw(g2, "scoreRankColor", "scoreRankFont", "scoreRankSize", "#" + Integer.toString(scoreRank), "scoreRankx", "scoreRanky");
            }
        }

        //绘制RankedScore
        draw(g2, "defaultColor", "numberFont", "numberSize",
                new DecimalFormat("###,###").format(userFromAPI.getRanked_score()), "rScorex", "rScorey");
        //绘制acc
        draw(g2, "defaultColor", "numberFont", "numberSize",
                new DecimalFormat("##0.00").format(userFromAPI.getAccuracy()) + "%", "accx", "accy");

        //绘制pc
        draw(g2, "defaultColor", "numberFont", "numberSize",
                new DecimalFormat("###,###").format(userFromAPI.getPlaycount()), "pcx", "pcy");

        //绘制tth
        draw(g2, "defaultColor", "numberFont", "numberSize",
                new DecimalFormat("###,###").format(userFromAPI.getCount50() + userFromAPI.getCount100() + userFromAPI.getCount300()), "tthx", "tthy");
        //绘制Level

        draw(g2, "defaultColor", "numberFont", "numberSize",
                Integer.toString((int) Math.floor(userFromAPI.getLevel())) + " (" + (int) ((userFromAPI.getLevel() - Math.floor(userFromAPI.getLevel())) * 100) + "%)", "levelx", "levely");

        //绘制SS计数
        draw(g2, "defaultColor", "numberFont", "countSize", Integer.toString(userFromAPI.getCount_rank_ss()), "ssCountx", "ssCounty");

        //绘制S计数
        draw(g2, "defaultColor", "numberFont", "countSize", Integer.toString(userFromAPI.getCount_rank_s()), "sCountx", "sCounty");

        //绘制A计数
        draw(g2, "defaultColor", "numberFont", "countSize", Integer.toString(userFromAPI.getCount_rank_a()), "aCountx", "aCounty");
        //绘制当时请求的时间

        draw(g2, "timeColor", "timeFont", "timeSize",
                new SimpleDateFormat("yy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()), "timex", "timey");


        //---------------------------以上绘制在线部分完成--------------------------------
        //试图查询数据库中指定日期的user
        if (day > 0) {
                /*
                不带参数：day=1，调用dbUtil拿当天凌晨（数据库中数值是昨天）的数据进行对比
                带day = 0:进入本方法，不读数据库，不进行对比
                day>1，例如day=2，21号进入本方法，查的是19号结束时候的成绩
                */
            if (day > 1) {
                //临时关闭平滑
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                //只有day>1才会出现文字
                if (near) {
                    //如果取到的是模糊数据,输出具体日期
                    draw(g2, "tipColor", "tipFont", "tipSize", "请求的日期没有数据", "tipx", "tipy");
                    //算出天数差别
                    draw(g2, "tipColor", "tipFont", "tipSize", "『对比于" + Long.valueOf(((Calendar.getInstance().getTime().getTime() -
                            userInDB.getQueryDate().getTime()) / 1000 / 60 / 60 / 24)).toString() + "天前』", "tip2x", "tip2y");
                } else {
                    //如果取到的是精确数据
                    draw(g2, "tipColor", "tipFont", "tipSize", "『对比于" + day + "天前』", "tip2x", "tip2y");
                }

            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //这样确保了userInDB不是空的
            //绘制Rank变化
            if (userInDB.getPp_rank() > userFromAPI.getPp_rank()) {
                //如果查询的rank比凌晨中的小
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + Integer.toString(userInDB.getPp_rank() - userFromAPI.getPp_rank()), "rankDiffx", "rankDiffy");
            } else if (userInDB.getPp_rank() < userFromAPI.getPp_rank()) {
                //如果掉了rank
                draw(g2, "downColor", "diffFont", "diffSize",
                        "↓" + Integer.toString(userFromAPI.getPp_rank() - userInDB.getPp_rank()), "rankDiffx", "rankDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + Integer.toString(0), "rankDiffx", "rankDiffy");
            }
            //绘制PP变化
            if (userInDB.getPp_raw() > userFromAPI.getPp_raw()) {
                //如果查询的pp比凌晨中的小
                draw(g2, "downColor", "diffFont", "diffSize",
                        "↓" + new DecimalFormat("##0.00").format(userInDB.getPp_raw() - userFromAPI.getPp_raw()), "ppDiffx", "ppDiffy");
            } else if (userInDB.getPp_raw() < userFromAPI.getPp_raw()) {
                //刷了PP
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + new DecimalFormat("##0.00").format(userFromAPI.getPp_raw() - userInDB.getPp_raw()), "ppDiffx", "ppDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + Integer.toString(0), "ppDiffx", "ppDiffy");
            }

            //绘制RankedScore变化
            if (userInDB.getRanked_score() < userFromAPI.getRanked_score()) {
                //因为RankedScore不会变少，所以不写蓝色部分
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + new DecimalFormat("###,###").format(userFromAPI.getRanked_score() - userInDB.getRanked_score()), "rScoreDiffx", "rScoreDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + Integer.toString(0), "rScoreDiffx", "rScoreDiffy");
            }
            //绘制ACC变化
            //在这里把精度砍掉
            if (Float.valueOf(new DecimalFormat("##0.00").format(userInDB.getAccuracy())) > Float.valueOf(new DecimalFormat("##0.00").format(userFromAPI.getAccuracy()))) {
                //如果acc降低了
                draw(g2, "downColor", "diffFont", "diffSize",
                        "↓" + new DecimalFormat("##0.00").format(userInDB.getAccuracy() - userFromAPI.getAccuracy()) + "%", "accDiffx", "accDiffy");
            } else if (Float.valueOf(new DecimalFormat("##0.00").format(userInDB.getAccuracy())) < Float.valueOf(new DecimalFormat("##0.00").format(userFromAPI.getAccuracy()))) {
                //提高
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + new DecimalFormat("##0.00").format(userFromAPI.getAccuracy() - userInDB.getAccuracy()) + "%", "accDiffx", "accDiffy");

            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + new DecimalFormat("##0.00").format(0.00) + "%", "accDiffx", "accDiffy");
            }

            //绘制pc变化
            if (userInDB.getPlaycount() < userFromAPI.getPlaycount()) {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + new DecimalFormat("###,###").format(userFromAPI.getPlaycount() - userInDB.getPlaycount()), "pcDiffx", "pcDiffy");

            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + Integer.toString(0), "pcDiffx", "pcDiffy");

            }

            //绘制tth变化,此处开始可以省去颜色设置
            if (userInDB.getCount50() + userInDB.getCount100() + userInDB.getCount300()
                    < userFromAPI.getCount50() + userFromAPI.getCount100() + userFromAPI.getCount300()) {
                //同理不写蓝色部分
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + new DecimalFormat("###,###").format(userFromAPI.getCount50() + userFromAPI.getCount100() + userFromAPI.getCount300() - (userInDB.getCount50() + userInDB.getCount100() + userInDB.getCount300())), "tthDiffx", "tthDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + Integer.toString(0), "tthDiffx", "tthDiffy");
            }
            //绘制level变化
            if (Float.valueOf(new DecimalFormat("##0.00").format(userInDB.getLevel())) < Float.valueOf(new DecimalFormat("##0.00").format(userFromAPI.getLevel()))) {
                //同理不写蓝色部分
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + (int) ((userFromAPI.getLevel() - userInDB.getLevel()) * 100) + "%", "levelDiffx", "levelDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "diffSize",
                        "↑" + Integer.toString(0) + "%", "levelDiffx", "levelDiffy");
            }
            //绘制SS count 变化
            //这里需要改变字体大小
            if (userInDB.getCount_rank_ss() > userFromAPI.getCount_rank_ss()) {
                //如果查询的SS比凌晨的少
                draw(g2, "downColor", "diffFont", "countDiffSize",
                        "↓" + Integer.toString(userInDB.getCount_rank_ss() - userFromAPI.getCount_rank_ss()), "ssCountDiffx", "ssCountDiffy");
            } else if (userInDB.getCount_rank_ss() < userFromAPI.getCount_rank_ss()) {
                //如果SS变多了
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "↑" + Integer.toString(userFromAPI.getCount_rank_ss() - userInDB.getCount_rank_ss()), "ssCountDiffx", "ssCountDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "↑" + Integer.toString(0), "ssCountDiffx", "ssCountDiffy");
            }
            //s
            if (userInDB.getCount_rank_s() > userFromAPI.getCount_rank_s()) {
                //如果查询的S比凌晨的少
                draw(g2, "downColor", "diffFont", "countDiffSize",
                        "↓" + Integer.toString(userInDB.getCount_rank_s() - userFromAPI.getCount_rank_s()), "sCountDiffx", "sCountDiffy");
            } else if (userInDB.getCount_rank_s() < userFromAPI.getCount_rank_s()) {
                //如果S变多了
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "↑" + Integer.toString(userFromAPI.getCount_rank_s() - userInDB.getCount_rank_s()), "sCountDiffx", "sCountDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "↑" + Integer.toString(0), "sCountDiffx", "sCountDiffy");
            }
            //a
            if (userInDB.getCount_rank_a() > userFromAPI.getCount_rank_a()) {
                //如果查询的S比凌晨的少
                draw(g2, "downColor", "diffFont", "countDiffSize",
                        "↓" + Integer.toString(userInDB.getCount_rank_a() - userFromAPI.getCount_rank_a()), "aCountDiffx", "aCountDiffy");
            } else if (userInDB.getCount_rank_a() < userFromAPI.getCount_rank_a()) {
                //如果S变多了
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "↑" + Integer.toString(userFromAPI.getCount_rank_a() - userInDB.getCount_rank_a()), "aCountDiffx", "aCountDiffy");
            } else {
                draw(g2, "upColor", "diffFont", "countDiffSize",
                        "↑" + Integer.toString(0), "aCountDiffx", "aCountDiffy");
            }
        }
        g2.dispose();

        return drawImage(bg, userFromAPI);
    }

    public String drawUserBP(User user, java.util.Map<BP, Integer> map) {
        //思路：获取list的大小，把每个list成员的.getbeatmapName信息绘制到图片上
        logger.info("开始绘制" + user.getUsername() + "的今日BP信息");
        BufferedImage bpTop;
        //根据谱面名称+难度的长度，将所有BP分为两个List
        List<BP> bp2 = new ArrayList<>();
        List<BP> bp3 = new ArrayList<>();
        List<BufferedImage> bpmids2 = new ArrayList<>();
        List<BufferedImage> bpmids3 = new ArrayList<>();
        int width = 0;
        int bpTopHeight = 0;
        int bpMid2Height = 0;
        int bpMid3Height = 0;
        try {
            bpTop = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("bptop")));
            for (BP aList : map.keySet()) {
                //准备好和BP数量相同的List
                if (aList.getBeatmap_name().length() <= Integer.valueOf(rb.getString("bplimit"))) {
                    //根据谱面名称+难度的长度读取BG
                    BufferedImage bpmidTmp = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("bpmid2")));
                    bpmids2.add(bpmidTmp);
                    bp2.add(aList);
                    bpMid2Height = bpmidTmp.getHeight();
                } else {
                    BufferedImage bpmidTmp = ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bp" + rb.getString("bpmid3")));
                    bpmids3.add(bpmidTmp);
                    bp3.add(aList);
                    bpMid3Height = bpmidTmp.getHeight();
                }
                width = bpTop.getWidth();
                bpTopHeight = bpTop.getHeight();
            }
        } catch (IOException e) {
            logger.error("读取BP布局图片失败");
            logger.error(e.getMessage());
            return "error";
        }

        //规划出结果图的尺寸(2行BP数量*2行BP图高度+3行BP数量*3行BP高度)
        BufferedImage result = new BufferedImage(width, bpTopHeight + bpMid2Height * bp2.size() + bpMid3Height * +bp3.size(), BufferedImage.TYPE_INT_RGB);
        logger.info("开始绘制头部");

        //在头部图片上绘制用户名
        Graphics2D g2 = (Graphics2D) bpTop.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        draw(g2, "bpUnameColor", "bpUnameFont", "bpUnameSize", "Best Performance of " + user.getUsername(), "bpUnamex", "bpUnamey");
        Calendar c = Calendar.getInstance();
        //日期补丁
        if (c.get(Calendar.HOUR_OF_DAY) < 4) {
            c.add(Calendar.DAY_OF_MONTH, -1);
        }
        draw(g2, "bpQueryDateColor", "bpQueryDateFont", "bpQueryDateSize", new SimpleDateFormat("yy-MM-dd").format(c.getTime()), "bpQueryDatex", "bpQueryDatey");
        g2.dispose();
        //将头部图片转换为数组
        int[] ImageArrayTop = new int[width * bpTopHeight];
        ImageArrayTop = bpTop.getRGB(0, 0, width, bpTopHeight, ImageArrayTop, 0, width);
        //将头部图片先画上去
        result.setRGB(0, 0, width, bpTopHeight, ImageArrayTop, 0, width);
        logger.info("头部图片绘制完成");

        for (int i = 0; i < bp2.size(); i++) {
            String mods;
            if (bp2.get(i).getEnabled_mods() > 0) {
                mods = convertMOD(bp2.get(i).getEnabled_mods()).toString().substring(1, convertMOD(bp2.get(i).getEnabled_mods()).toString().length() - 1);
            } else {
                mods = "None";
            }
            //准备将字符串写入图片
            Graphics2D g = (Graphics2D) bpmids2.get(i).getGraphics();
            //绘制小图
            switch (bp2.get(i).getRank()) {
                case "A":
                    g.drawImage(A, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "B":
                    g.drawImage(B, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "C":
                    g.drawImage(C, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "D":
                    g.drawImage(D, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "X":
                    g.drawImage(X, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "XH":
                    g.drawImage(XH, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "S":
                    g.drawImage(S, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
                case "SH":
                    g.drawImage(SH, Integer.decode(rb.getString("bp2Rankx")), Integer.decode(rb.getString("bp2Ranky")), null);
                    break;
            }

            //开启平滑
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //开始绘制谱面名称，因为是二行的数据所以不需要分割
            draw(g, "bpNameColor", "bpNameFont", "bpNameSize",

                    bp2.get(i).getBeatmap_name() + "(" + new DecimalFormat("###.00").format(100.0 * (6 * bp2.get(i).getCount300() + 2 * bp2.get(i).getCount100() + bp2.get(i).getCount50()) / (6 * (bp2.get(i).getCount50() + bp2.get(i).getCount100() + bp2.get(i).getCount300() + bp2.get(i).getCountmiss()))) + "%)", "bp2Namex", "bp2Namey");
            //绘制日期(给的就是北京时间，不转)
            draw(g, "bpDateColor", "bpDateFont", "bpDateSize",
                    new SimpleDateFormat("MM-dd HH:mm").format(bp2.get(i).getDate().getTime()), "bp2Datex", "bp2Datey");
            //绘制Num和Weight
            draw(g, "bpNumColor", "bpNumFont", "bpNumSize",
                    String.valueOf(map.get(bp2.get(i)) + 1), "bp2Numx", "bp2Numy");

            draw(g, "bpWeightColor", "bpWeightFont", "bpWeightSize",
                    new DecimalFormat("##0.00").format(100 * Math.pow(0.95, map.get(bp2.get(i)))) + "%", "bp2Weightx", "bp2Weighty");

            //绘制MOD
            draw(g, "bpModColor", "bpModFont", "bpModSize", mods, "bp2Modx", "bp2Mody");
            //绘制PP
            draw(g, "bpPPColor", "bpPPFont", "bpPPSize", Integer.toString(Math.round(bp2.get(i).getPp())) + "pp", "bp2PPx", "bp2PPy");
            g.dispose();
            //将它变成数组
            int[] ImageArray = new int[width * bpMid2Height];
            ImageArray = bpmids2.get(i).getRGB(0, 0, width, bpMid2Height, ImageArray, 0, width);
            //横坐标是0，纵坐标是i+1*每个格子的高度，大小是每个格子的宽高
            result.setRGB(0, bpMid2Height * i + bpTopHeight, width, bpMid2Height, ImageArray, 0, width);//将数组写入缓冲图片
            logger.info("绘制" + bp2.get(i).getBeatmap_name() + "完成");
        }
        logger.info("无需使用大背景的BP绘制完成");
        for (int i = 0; i < bp3.size(); i++) {
            String mods;
            if (bp3.get(i).getEnabled_mods() > 0) {
                mods = convertMOD(bp3.get(i).getEnabled_mods()).toString().substring(1, convertMOD(bp3.get(i).getEnabled_mods()).toString().length() - 1);
            } else {
                mods = "None";
            }
            //准备将字符串写入图片
            Graphics2D g = (Graphics2D) bpmids3.get(i).getGraphics();
            //绘制小图
            switch (bp3.get(i).getRank()) {
                case "A":
                    g.drawImage(A, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "B":
                    g.drawImage(B, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "C":
                    g.drawImage(C, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "D":
                    g.drawImage(D, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "X":
                    g.drawImage(X, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "XH":
                    g.drawImage(XH, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "S":
                    g.drawImage(S, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
                case "SH":
                    g.drawImage(SH, Integer.decode(rb.getString("bp3Rankx")), Integer.decode(rb.getString("bp3Ranky")), null);
                    break;
            }

            //开启平滑
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //开始绘制谱面名称，三行BP需要分割字符串
            //取80个字符中的最后一个空格
            draw(g, "bpNameColor", "bpNameFont", "bpNameSize", bp3.get(i).getBeatmap_name().substring(0, bp3.get(i).getBeatmap_name().substring(0, Integer.valueOf(rb.getString("bplimit")) + 1).lastIndexOf(" ") + 1),
                    "bp3Namex", "bp3Namey");
            //第二行
            draw(g, "bpNameColor", "bpNameFont", "bpNameSize", bp3.get(i).getBeatmap_name().substring(bp3.get(i).getBeatmap_name().substring(0, Integer.valueOf(rb.getString("bplimit")) + 1).lastIndexOf(" ") + 1, bp3.get(i).getBeatmap_name().length())
                            + "(" + new DecimalFormat("###.00").format(100.0 * (6 * bp3.get(i).getCount300() + 2 * bp3.get(i).getCount100() + bp3.get(i).getCount50()) / (6 * (bp3.get(i).getCount50() + bp3.get(i).getCount100() + bp3.get(i).getCount300() + bp3.get(i).getCountmiss()))) + "%)",
                    "bp3Name+1x", "bp3Name+1y");
            //绘制日期(给的就是北京时间，不转)
            draw(g, "bpDateColor", "bpDateFont", "bpDateSize",
                    new SimpleDateFormat("MM-dd HH:mm").format(bp3.get(i).getDate().getTime()), "bp3Datex", "bp3Datey");
            //绘制Num和Weight
            draw(g, "bpNumColor", "bpNumFont", "bpNumSize",
                    String.valueOf(map.get(bp3.get(i)) + 1), "bp3Numx", "bp3Numy");

            draw(g, "bpWeightColor", "bpWeightFont", "bpWeightSize",
                    new DecimalFormat("##0.00").format(100 * Math.pow(0.95, map.get(bp3.get(i)))) + "%", "bp3Weightx", "bp3Weighty");

            //绘制MOD
            draw(g, "bpModColor", "bpModFont", "bpModSize", mods, "bp3Modx", "bp3Mody");
            //绘制PP
            draw(g, "bpPPColor", "bpPPFont", "bpPPSize", Integer.toString(Math.round(bp3.get(i).getPp())) + "pp", "bp3PPx", "bp3PPy");
            g.dispose();
            //将它变成数组
            int[] ImageArray = new int[width * bpMid3Height];
            ImageArray = bpmids3.get(i).getRGB(0, 0, width, bpMid3Height, ImageArray, 0, width);
            //横坐标是0，纵坐标是i*每个格子的高度（没有头图不用+1），还要加上bp2占用的高度+1（头图。+1在这里），大小是每个格子的宽高
            result.setRGB(0, bpMid3Height * (i) + bpMid2Height * (bp2.size()) + bpTopHeight, width, bpMid3Height, ImageArray, 0, width);
            logger.info("绘制" + bp3.get(i).getBeatmap_name() + "完成");
        }
        //生成新图片
        return drawImage(result, user);
    }


    public String drawOneBP(User user, BP bp, Map map) {
        logger.info("开始绘制" + map.getArtist() + " - " + map.getTitle() + " [" + map.getVersion() + "]的结算界面");
        String accS = new DecimalFormat("###.00").format(100.0 * (6 * bp.getCount300() + 2 * bp.getCount100() + bp.getCount50()) / (6 * (bp.getCount50() + bp.getCount100() + bp.getCount300() + bp.getCountmiss())));
        float acc = Float.valueOf(accS);
        BufferedImage bg;

        try {
            bg = pageUtil.getBG(bp.getBeatmap_id(), map);
        } catch (IOException | NullPointerException e) {
            logger.error("从血猫抓取谱面背景失败");
            logger.error(e.getMessage());
            return "error";
        }

        logger.info("歌曲BG加载完成");
        List<String> mods = convertMOD(bp.getEnabled_mods());
        String osuFile = pageUtil.getOsuFile(bp.getBeatmap_id(), map);
        BufferedReader bufferedReader;
        int PP;
        int aimPP;
        int speedPP;
        int accPP;
        int progress;
        float speedStar;
        float aimStar;
        try {
            cmd = cmd + osuFile + "\" -ojson ";
//            if (bp.getRank().equals("F")) {
//                Process process = Runtime.getRuntime().exec(cmd);
//                process.waitFor();
//                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
//                String line = bufferedReader.readLine();
//                JSON json = JSON.parse(line);
//
//                //将Fail成绩中没打的note看做miss,这样会导致在acc=4%左右的情况下ACCPP达到4.6415112964168472e+050……
//                acc = Float.valueOf(new DecimalFormat("###.00").format(100.0 * (6 * bp.getCount300() + 2 * bp.getCount100() + bp.getCount50())
//                        / (6 * (bp.getCount50() + bp.getCount100() + bp.getCount300() + json.get("num_circles").getInt() + json.get("num_sliders").getInt() + json.get("num_spinners").getInt() - bp.getCount300() - bp.getCount100() - bp.getCount50()))));
//            }
            List<String> modsWithoutSD = new ArrayList<>();
            if(mods.contains("SD")||mods.contains("PF")){
                for (String mod : mods) {
                    if (!mod.equals("SD") && !mod.equals("PF")) {
                        modsWithoutSD.add(mod);
                    }
                }
            }
            if (modsWithoutSD.size() > 0) {
                cmd = cmd.concat("+" + modsWithoutSD.toString().replaceAll("[\\[\\] ,]", "")+ " ");

            }
//            logger.debug(acc);
            //改为直接计算ACC
            cmd = cmd + acc + "% " + bp.getCountmiss() + "m " + bp.getMaxcombo() + "x";
//            acc = Float.valueOf(accS);
            Process process = Runtime.getRuntime().exec(cmd);
//            logger.debug(cmd);
            process.waitFor();
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            String line = bufferedReader.readLine();
            JSON json = JSON.parse(line);
//            logger.debug(line);
            progress = 300 * (bp.getCount50() + bp.getCount100() + bp.getCount300() + bp.getCountmiss()) / (json.get("num_circles").getInt() + json.get("num_sliders").getInt() + json.get("num_spinners").getInt());
            PP = Math.round(Float.valueOf(json.get("pp").getString()));
            aimPP = Math.round(Float.valueOf(json.get("aim_pp").getString()));
            speedPP = Math.round(Float.valueOf(json.get("speed_pp").getString()));
            accPP = Math.round(Float.valueOf(json.get("acc_pp").getString()));

            speedStar = Float.valueOf(json.get("speed_stars").getString().substring(0, 4));
            aimStar = Float.valueOf(json.get("aim_stars").getString().substring(0, 4));
//            map.setArtist(json.get("artist_unicode").getString());
//            map.setTitle(json.get("title_unicode").getString());
        } catch (InterruptedException | IOException | ParserException e) {
            logger.error("离线计算PP出错");
            logger.error(e.getMessage());
            return "error";
        }
        logger.info("离线计算PP完成");


        Graphics2D g2 = (Graphics2D) bg.getGraphics();


        //画上各个元素，这里Images按文件名排序
        //顶端banner(下方也暗化了20%，JAVA自带算法容易导致某些图片生成透明图片)
        g2.drawImage(Images.get(0), 0, 0, null);
        //右下角两个FPS
        g2.drawImage(Images.get(1), 1300, 699, null);
        g2.drawImage(Images.get(2), 1300, 723, null);
        //左下角返回
        g2.drawImage(Images.get(7), 0, 568, null);
        //右下角OnlineUsers/ShowChat
        g2.drawImage(Images.get(8), 1178, 746, null);
        g2.drawImage(Images.get(9), 1274, 746, null);
        //右下角replay
        g2.drawImage(Images.get(10), 1026 - 58, 549 - 31, null);
        //rank
        switch (bp.getRank()) {
            case "A":
                g2.drawImage(Images.get(11).getScaledInstance(Images.get(11).getWidth(), Images.get(11).getHeight(), Image.SCALE_SMOOTH), 1131 - 245, 341 - 242, null);
                break;
            case "B":
                g2.drawImage(Images.get(12).getScaledInstance(Images.get(12).getWidth(), Images.get(12).getHeight(), Image.SCALE_SMOOTH), 1131 - 245, 341 - 242, null);
                break;
            case "C":
                g2.drawImage(Images.get(13).getScaledInstance(Images.get(13).getWidth(), Images.get(13).getHeight(), Image.SCALE_SMOOTH), 1131 - 245, 341 - 242, null);
                break;
            case "D":
                g2.drawImage(Images.get(14).getScaledInstance(Images.get(14).getWidth(), Images.get(14).getHeight(), Image.SCALE_SMOOTH), 1131 - 245, 341 - 242, null);
                break;
            case "X":
                g2.drawImage(Images.get(21).getScaledInstance(Images.get(21).getWidth(), Images.get(21).getHeight(), Image.SCALE_SMOOTH), 1131 - 245, 341 - 242, null);
                break;
            case "XH":
                g2.drawImage(Images.get(22).getScaledInstance(Images.get(22).getWidth(), Images.get(22).getHeight(), Image.SCALE_SMOOTH), 1131 - 245, 341 - 242, null);
                break;
            case "S":
                g2.drawImage(Images.get(18).getScaledInstance(Images.get(18).getWidth(), Images.get(18).getHeight(), Image.SCALE_SMOOTH), 1131 - 245, 341 - 242, null);
                break;
            case "SH":
                g2.drawImage(Images.get(19).getScaledInstance(Images.get(19).getWidth(), Images.get(19).getHeight(), Image.SCALE_SMOOTH), 1131 - 245, 341 - 242, null);
                break;
            case "F":
                g2.drawImage(Images.get(15).getScaledInstance(Images.get(15).getWidth(), Images.get(15).getHeight(), Image.SCALE_SMOOTH), 1131 - 245, 341 - 242, null);
                break;
        }
        //右上角Ranking
        g2.drawImage(Images.get(20), 1029 - 66, 0, null);

        //RankGraph
        g2.drawImage(Images.get(16), 270 - 14, 613 - 6, null);
        if (bp.getRank().equals("F")) {
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Color.decode("#99cc31"));
            g2.drawLine(262, 615, 262 + progress, 615);
            g2.setColor(Color.decode("#fe0000"));
            g2.drawLine(262 + progress, 615, 262 + progress, 753);
        }
        //FC
        if (bp.getPerfect() == 1) {
            g2.drawImage(Images.get(17), 296 - 30, 675 - 37, null);
        }

        //分数 图片扩大到1.27倍
        //分数是否上e，每个数字的位置都不一样
        if (bp.getScore() > 99999999) {
            char[] Score = String.valueOf(bp.getScore()).toCharArray();
            for (int i = 0; i < Score.length; i++) {
                //第二个参数是数字之间的距离+第一个数字离最左边的距离
                g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(Score[i]))), 55 * i + 128 - 21, 173 - 55, null);
            }
        } else {
            char[] Score = String.valueOf(bp.getScore()).toCharArray();

            for (int i = 0; i < 8; i++) {
                if (Score.length < 8) {
                    //如果分数不到8位，左边用0补全
                    //获取Score的长度和8的差距，然后把i小于等于这个差距的时候画的数字改成0
                    if (i < 8 - Score.length) {
                        g2.drawImage(Nums.get(0), 55 * i + 141 - 6, 173 - 55, null);
                    } else {
                        //第一次应该拿的是数组里第0个字符
                        g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(Score[i - 8 + Score.length]))), 55 * i + 141 - 6, 173 - 55, null);
                    }

                } else {
                    //直接绘制
                    g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(Score[i]))), 55 * i + 141 - 6, 173 - 55, null);
                }
            }
        }
        //combo
        char[] Combo = String.valueOf(bp.getMaxcombo()).toCharArray();
        for (int i = 0; i < Combo.length; i++) {
            //第二个参数是数字之间的距离+第一个数字离最左边的距离
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(Combo[i]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * i + 30 - 7, 576 - 55 + 10, null);
        }
        //画上结尾的x
        g2.drawImage(Nums.get(13).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * Combo.length + 30 - 7, 576 - 55 + 10, null);

        //300 这些图片应该缩小到一半大小
        g2.drawImage(Images.get(5), 40 - 4, 263 - 27, null);
        g2.drawImage(Images.get(5), 360 - 4, 263 - 27, null);
        char[] Count300 = String.valueOf(bp.getCount300()).toCharArray();
        for (int i = 0; i < Count300.length; i++) {
            //第二个参数是数字之间的距离+第一个数字离最左边的距离
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(Count300[i]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * i + 134 - 7, 238 - 7, null);
        }
        //画上结尾的x
        g2.drawImage(Nums.get(13).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * Count300.length + 134 - 7, 238 - 7, null);

        //激
        char[] CountGeki = String.valueOf(bp.getCountgeki()).toCharArray();
        for (int i = 0; i < CountGeki.length; i++) {
            //第二个参数是数字之间的距离+第一个数字离最左边的距离
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(CountGeki[i]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * i + 455 - 8, 238 - 7, null);
        }
        //画上结尾的x
        g2.drawImage(Nums.get(13).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * CountGeki.length + 455 - 8, 238 - 7, null);

        //100
        g2.drawImage(Images.get(4), 44 - 5, 346 - 8, null);
        g2.drawImage(Images.get(4), 364 - 5, 346 - 8, null);
        char[] Count100 = String.valueOf(bp.getCount100()).toCharArray();
        for (int i = 0; i < Count100.length; i++) {
            //第二个参数是数字之间的距离+第一个数字离最左边的距离
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(Count100[i]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * i + 134 - 7, 374 - 55, null);
        }
        //画上结尾的x
        g2.drawImage(Nums.get(13).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * Count100.length + 134 - 7, 374 - 55, null);

        //喝
        char[] CountKatu = String.valueOf(bp.getCountkatu()).toCharArray();
        for (int i = 0; i < CountKatu.length; i++) {
            //第二个参数是数字之间的距离+第一个数字离最左边的距离
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(CountKatu[i]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * i + 455 - 8, 374 - 55, null);
        }
        //画上结尾的x
        g2.drawImage(Nums.get(13).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * CountKatu.length + 455 - 8, 374 - 55, null);

        //50
        g2.drawImage(Images.get(6), 51 - 5, 455 - 21, null);
        char[] Count50 = String.valueOf(bp.getCount50()).toCharArray();
        for (int i = 0; i < Count50.length; i++) {
            //第二个参数是数字之间的距离+第一个数字离最左边的距离
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(Count50[i]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * i + 134 - 7, 470 - 55, null);
        }
        //画上结尾的x
        g2.drawImage(Nums.get(13).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * Count50.length + 134 - 7, 470 - 55, null);

        //x
        g2.drawImage(Images.get(3), 376 - 4, 437 - 5, null);
        char[] Count0 = String.valueOf(bp.getCountmiss()).toCharArray();
        for (int i = 0; i < Count0.length; i++) {
            //第二个参数是数字之间的距离+第一个数字离最左边的距离
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(Count0[i]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * i + 455 - 8, 470 - 55, null);
        }
        //画上结尾的x
        g2.drawImage(Nums.get(13).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * Count0.length + 455 - 8, 470 - 55, null);

        //acc


        if (acc == 100) {
            //从最左边的数字开始，先画出100
            g2.drawImage(Nums.get(1).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * 0 + 317 - 8, 576 - 55 + 10, null);
            g2.drawImage(Nums.get(0).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * 1 + 317 - 8, 576 - 55 + 10, null);
            g2.drawImage(Nums.get(0).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * 2 + 317 - 8, 576 - 55 + 10, null);
            //打点
            g2.drawImage(Nums.get(11).getScaledInstance(20, 45, Image.SCALE_SMOOTH), 37 * 1 + 407 - 8, 576 - 55 + 10, null);
            //从点的右边（+27像素）开始画两个0
            g2.drawImage(Nums.get(0).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 27 * 1 + 37 * 1 + 407 - 8, 576 - 55 + 10, null);
            g2.drawImage(Nums.get(0).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 27 * 1 + 37 * 2 + 407 - 8, 576 - 55 + 10, null);
            g2.drawImage(Nums.get(12).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 27 * 1 + 407 - 8 + 37 * 3, 576 - 55 + 10, null);
        } else {
            //将ACC转化为整数部分、小数点和小数部分
            char[] aa1 = accS.toCharArray();
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(aa1[0]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * 0 + 317 - 8 + 15, 576 - 55 + 10, null);
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(aa1[1]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 37 * 1 + 317 - 8 + 15, 576 - 55 + 10, null);
            //打点
            g2.drawImage(Nums.get(11).getScaledInstance(20, 45, Image.SCALE_SMOOTH), 407 - 8, 576 - 55 + 15, null);

            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(aa1[3]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 27 * 1 + 407 - 8, 576 - 55 + 10, null);
            g2.drawImage(Nums.get(Integer.valueOf(String.valueOf(aa1[4]))).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 27 * 1 + 407 - 8 + 37 * 1, 576 - 55 + 10, null);
            g2.drawImage(Nums.get(12).getScaledInstance(40, 51, Image.SCALE_SMOOTH), 27 * 1 + 407 - 8 + 37 * 2, 576 - 55 + 10, null);
        }

        //MOD


        java.util.Map<String, Integer> modMap = new HashMap<>();
        modMap.put("DT", 0);
        modMap.put("EZ", 1);
        modMap.put("FL", 2);
        modMap.put("HT", 3);
        modMap.put("HR", 4);
        modMap.put("HD", 5);
        modMap.put("NC", 6);
        modMap.put("NF", 7);
        modMap.put("PF", 8);
        modMap.put("SO", 9);
        modMap.put("SD", 10);
        //对mods迭代
        for (int i = 0; i < mods.size(); i++) {
            //第一个mod画在1237，第二个画在1237+30,第三个1237-30
            logger.info("正在绘制mod图标：" + mods.get(i));
            g2.drawImage(Mods.get(modMap.get(mods.get(i))), 1237 - (50 * i), 375, null);
        }
        //底端PP面板
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if ((int) (Math.random() * 20) == 1) {
            g2.drawImage(zPPTrick, 540, 200, null);
            g2.setFont(new Font("Ubuntu Bold", Font.BOLD, 14));
            g2.setPaint(Color.decode("#000000"));
            g2.drawString(" " + String.valueOf(PP), 210 + 540, 76 + 200);

            g2.drawString(String.valueOf(aimPP) + "PP", 349 + 540, 195 + 200);
            g2.drawString(String.valueOf(speedPP) + "PP", 349 + 540, 241 + 200);
            g2.drawString(String.valueOf(accPP) + "PP", 349 + 540, 284 + 200);

            g2.drawString("Aim PP", 46 + 540, 195 + 200);
            g2.drawString("Speed PP", 46 + 540, 241 + 200);
            g2.drawString("Acc PP", 46 + 540, 284 + 200);
            g2.setPaint(Color.decode("#8e8e8d"));

            g2.drawString(String.valueOf(aimPP) + "PP", 142 + 540, 94 + 200);

            g2.drawString("Aim Star: " + String.valueOf(aimStar), 253 + 540, 195 + 200);
            g2.drawString("ACC: " + accS + "%", 253 + 540, 284 + 200);
            g2.drawString("Spd Star: " + String.valueOf(speedStar), 253 + 540, 241 + 200);
        } else {
            g2.drawImage(zPP, 570, 700, null);

            g2.setPaint(Color.decode("#ff66a9"));
            g2.setFont(new Font("Gayatri", 0, 60));
            if (String.valueOf(PP).contains("1")) {
                g2.drawString(String.valueOf(PP), 616, 753);
            } else {
                g2.drawString(String.valueOf(PP), 601, 753);
            }
            g2.setFont(new Font("Gayatri", 0, 48));
            g2.drawString(String.valueOf(aimPP), 834, 758);
            g2.drawString(String.valueOf(speedPP), 932, 758);
            g2.drawString(String.valueOf(accPP), 1030, 758);
        }

        //写字
        //指定颜色
        g2.setPaint(Color.decode("#FFFFFF"));
        //指定字体
        g2.setFont(new Font("Ubuntu", 0, 24));
        //指定坐标
        g2.drawString(map.getArtist() + " - " + map.getTitle() + " [" + map.getVersion() + "]", 7, 26);
        g2.setFont(new Font("Ubuntu", 0, 20));
        g2.drawString("Beatmap by " + map.getCreator(), 7, 52);
        g2.drawString("Played by " + user.getUsername() + " on " + new SimpleDateFormat("yy/MM/dd HH:mm:ss").format(bp.getDate()) + ".", 7, 74);
        g2.dispose();

        //削弱结果图片的颜色
        BufferedImage result = new BufferedImage(1366, 768, BufferedImage.TYPE_USHORT_555_RGB);
        Graphics2D g3 = result.createGraphics();
        g3.clearRect(0, 0, 1366, 768);
        g3.drawImage(bg.getScaledInstance(1366, 768, Image.SCALE_SMOOTH), 0, 0, null);
        g3.dispose();
        result.flush();
//        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
//        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//        jpegParams.setCompressionQuality(1f);
//        (FileImageOutputStream fios = new FileImageOutputStream(
//                new File(rb.getString("path") + "\\data\\image\\" + bp.getBeatmap_id() + "_" + new SimpleDateFormat("yy-MM-dd").format(bp.getDate()) + ".jpg"));)
        try {
//            final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            // specifies where the jpg image has to be written
//            writer.setOutput(fios);
            // writes the file with given compression level
            // from your JPEGImageWriteParam instance
//            writer.write(null, new IIOImage(resizedBG, null, null), jpegParams);
//            writer.dispose();

            ImageIO.write(result, "png", new File(rb.getString("path") + "\\data\\image\\" + bp.getBeatmap_id() + "_" + new SimpleDateFormat("yy-MM-dd").format(bp.getDate()) + ".png"));
//            logger.info("正在压缩图片");
//            Process process = Runtime.getRuntime().exec(cmd + bp.getBeatmap_id() + "_" + new SimpleDateFormat("yy-MM-dd").format(bp.getDate()) + ".png\"");
//            try {
//                process.waitFor();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            return bp.getBeatmap_id() + "_" + new SimpleDateFormat("yy-MM-dd").format(bp.getDate()) + ".png";
        } catch (IOException e) {
            logger.error("绘制图片成品失败");
            logger.error(e.getMessage());
            return "error";
        }
    }

}
