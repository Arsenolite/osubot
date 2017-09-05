package top.mothership.osubot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by QHS on 2017/8/18.
 */
public class pageUtil {
    private Logger logger = LogManager.getLogger(this.getClass());
    private final String getAvaURL = "https://a.ppy.sh/";
    private final String getUserURL = "https://osu.ppy.sh/u/";
    private final String getUserProfileURL = "https://osu.ppy.sh/pages/include/profile-general.php?u=";
    private final String getBGURL = "http://bloodcat.com/osu/i/";
    private final String getOsuURL = "https://osu.ppy.sh/osu/";
    private HashMap<Integer,Document> map = new HashMap<>();
    private static ResourceBundle rb = ResourceBundle.getBundle("cabbage");
    private apiUtil apiUtil = new apiUtil();
    //后续在这个类里解析dom树获取网页内容
    //将异常抛出给调用者
    public BufferedImage getAvatar(int uid) throws IOException {
        URL avaurl = new URL(getAvaURL + uid + "?.png");
        return ImageIO.read(avaurl);
    }

    public BufferedImage getBG(int bid, top.mothership.osubot.pojo.Map map)throws IOException {
        HttpURLConnection httpConnection;
        int retry = 0;
        BufferedImage bg;
        BufferedImage resizedBG = null;
        File bgFile = new File(rb.getString("path") + "\\data\\image\\resource\\bg\\"+bid+".png");

        if(bgFile.length()>0&&(map.getApproved()==1||map.getApproved()==2)){
            //如果osu文件大小大于0，并且状态是ranked
            return ImageIO.read(new File(rb.getString("path") + "\\data\\image\\resource\\bg\\"+bid+".png"));
        }
        while (retry < 8) {
            try {

                httpConnection =
                        (HttpURLConnection) new URL(getBGURL + bid).openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setConnectTimeout((int)Math.pow(2,retry)*1000);
                httpConnection.setReadTimeout((int)Math.pow(2,retry)*1000);
                httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.40 Safari/537.36");
                if (httpConnection.getResponseCode() != 200) {
                    logger.error("HTTP GET请求失败: " + httpConnection.getResponseCode() + "，正在重试第" + (retry + 1) + "次");
                    retry++;
                    continue;
                }
                //读取返回结果
                bg = ImageIO.read(httpConnection.getInputStream());
                //获取bp原分辨率，将宽拉到1366，然后算出高，减去768除以二然后上下各减掉这部分
                int resizedWeight = 1366;
                int resizedHeight = (int) Math.ceil((float) bg.getHeight() / bg.getWidth() * 1366);
                int heightDiff = ((resizedHeight - 768) / 2);
                int widthDiff = 0;
                //如果算出重画之后的高<768(遇到金盏花这种特别宽的)
                if (resizedHeight < 768) {
                    resizedWeight = (int) Math.ceil((float) bg.getWidth() / bg.getHeight() * 768);
                    resizedHeight = 768;
                    heightDiff = 0;
                    widthDiff = ((resizedWeight - 1366) / 2);
                }
                //把BG横向拉到1366;
                //忘记在这里处理了
                BufferedImage resizedBGTmp = new BufferedImage(resizedWeight, resizedHeight, bg.getType());
                Graphics2D g = resizedBGTmp.createGraphics();
                g.drawImage(bg.getScaledInstance(resizedWeight, resizedHeight, Image.SCALE_SMOOTH), 0, 0, resizedWeight, resizedHeight, null);
                g.dispose();

                //切割图片
                resizedBG = new BufferedImage(1366, 768, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < 1366; x++) {
                    //这里之前用了原bg拉伸之前的分辨率，难怪报错
                    for (int y = 0; y < 768; y++) {
                        resizedBG.setRGB(x, y, resizedBGTmp.getRGB(x + widthDiff, y + heightDiff));
                    }
                }
                //刷新掉bg以及临时bg的缓冲，将其作废
                resizedBGTmp.flush();

                bg.flush();


                //同时写入硬盘
                ImageIO.write(resizedBG,"png",new File(rb.getString("path") + "\\data\\image\\resource\\bg\\"+bid+".png"));
                //手动关闭流
                httpConnection.disconnect();

                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            }

        }
        if (retry == 8) {
            logger.error("获取" + bid + "的背景图，失败五次");
            throw new IOException();
        }
        return resizedBG;

    }

    public int getRepWatched(int uid) {
        int retry = 0;
        Document doc = null;
        while (retry < 8) {
            try {
                logger.info("正在获取" + uid + "的Replays被观看次数");
                doc = Jsoup.connect(getUserProfileURL + uid).timeout((int)Math.pow(2,retry)*1000).get();
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1)  + "次");
                retry++;
            }
        }
        if (retry == 8) {
            logger.error("玩家" + uid + "请求API获取数据，失败五次");
            return 0;
        }
        Elements link = doc.select("div[title*=replays]");
        String a = link.text();
        a = a.substring(27).replace(" times", "").replace(",", "");
        return Integer.valueOf(a);
    }

    public int getRank(long rScore, int start, int end){
        long endValue = getScore(end);
        if (rScore < endValue||endValue==0) {
            map.clear();
            return 0;
        }
        if (rScore == endValue) {
            map.clear();
            return end;
        }
        //第一次写二分法……不过大部分时间都花在算准确页数，和拿页面元素上了
        while (start <= end) {
            int middle = (start + end) / 2;
            long middleValue = getScore(middle);

            if (middleValue == 0) {
                map.clear();
                return 0;
            }
            if (rScore == middleValue) {
                // 等于中值直接返回
                //清空掉缓存
                map.clear();
                return middle;
            } else if (rScore > middleValue) {
                //rank和分数成反比，所以大于反而rank要在前半部分找
                end = middle - 1;
            } else {
                start = middle + 1;
            }
        }
        map.clear();
        return 0;
    }


    private long getScore(int rank){
        Document doc = null;
        int retry = 0;
        logger.info("正在抓取#"+rank+"的玩家的分数");
        //一定要把除出来的值强转
        //math.round好像不太对，应该是ceil
        int p = (int)Math.ceil((float) rank/50);
        //获取当前rank在当前页的第几个
        int num = (rank-1)%50;
        //避免在同一页内的连续查询，将上次查询的doc和p缓存起来
        if(map.get(p)==null) {
            while (retry < 8) {
                try {
                    doc = Jsoup.connect("https://osu.ppy.sh/rankings/osu/score?page=" + p).timeout((int)Math.pow(2,retry)*1000).get();
                    break;
                } catch (IOException e) {
                    logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                    retry++;
                }

            }
            if (retry == 8) {
                logger.error("查询分数失败五次");
                return 0;
            }
            map.put(p, doc);
        }else{
            doc = map.get(p);
        }
        String score = doc.select("td[class*=focused]").get(num).child(0).attr("title");
        return Long.valueOf(score.replace(",",""));

    }

    public Date getLastActive(int uid){
        int retry = 0;
        Document doc = null;
        while (retry < 8) {
            try {
                logger.info("正在获取" + uid + "的上次活跃时间");
                doc = Jsoup.connect(getUserURL + uid).timeout((int)Math.pow(2,retry)*1000).get();
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            }
        }
        if (retry == 8) {
            logger.error("玩家" + uid + "请求API获取数据，失败五次");
            return null;
        }
        Elements link = doc.select("time[class*=timeago]");
        String a = link.get(1).text();
        a = a.substring(0,19);
        try {
            //转换为北京时间
            return new Date(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(a).getTime()+8*3600*1000);
        } catch (ParseException e) {
            logger.error("将时间转换为Date对象出错");
        }
        return null;
    }

    public String getOsuFile(int bid,top.mothership.osubot.pojo.Map map){
        HttpURLConnection httpConnection = null;
        int retry = 0;
        File osu = new File(rb.getString("path") + "\\data\\image\\resource\\osu\\"+bid+".osu");

        if(osu.length()>0&&(map.getApproved()==1||map.getApproved()==2)){
            //如果osu文件大小大于0，并且状态是ranked
            return bid+".osu";
        }
        while (retry < 8) {
            try(
                    FileOutputStream fs = new FileOutputStream(osu)
            ) {

                httpConnection =
                        (HttpURLConnection) new URL(getOsuURL + bid).openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.setConnectTimeout((int)Math.pow(2,retry)*1000);
                httpConnection.setReadTimeout((int)Math.pow(2,retry)*1000);
                if (httpConnection.getResponseCode() != 200) {
                    logger.error("HTTP GET请求失败: " + httpConnection.getResponseCode() + "，正在重试第" + (retry + 1) + "次");
                    retry++;
                    continue;
                }
                //读取返回结果
                fs.write(readInputStream(httpConnection.getInputStream()));
                //手动关闭流
                httpConnection.disconnect();
                break;
            } catch (IOException e) {
                logger.error("出现IO异常：" + e.getMessage() + "，正在重试第" + (retry + 1) + "次");
                retry++;
            }

        }
        if (retry == 8) {
            logger.error("获取" + bid + "的背景图，失败五次");
            return null;
        }
        return bid+".osu";
    }


    private byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

}