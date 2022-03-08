package com.zhiji.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zhiji.config.Contains;
import com.zhiji.functions.*;
import com.zhiji.utils.ConfigUtil;
import com.zhiji.utils.MysqlUtil;
import com.zhiji.utils.TokenUtil;

import java.util.Properties;


/**
 * @author finnwg
 * @version 1.0
 * @desc
 * @date 2022/2/25 10:29
 */
public class Action {
    //private static final String ACCESS_TOKEN = "4142f4373c2365453f74b292fd404be46e4c5540";
    //private static final String PATH = "/open_api/2/report/creative/get/";

    //定义参数
    public static ObjectMapper mapper = new ObjectMapper();
    public static Properties properties;
    public static String getTokenUrl;
    public static Long appId;
    public static String secret;
    public static String grantType;



    public static void main(String[] args) throws Exception {

        //读取配置文件，参数赋值
        String path = args[0];

        //参数对象
        properties = ConfigUtil.gerConfig(path);
        //如果参数个数大于等于三个，则用传参时间更新配置中的时间
        if (args.length >= 3) {
            properties.setProperty("start_date",args[1]);
            properties.setProperty("end_date",args[2]);
        }

        //post请求url，用于刷新token
        getTokenUrl = properties.getProperty(Contains.POST_URL);
        //post请求参加
        appId = Long.valueOf(properties.getProperty(Contains.APP_ID));
        secret = properties.getProperty(Contains.SECRET);
        grantType = properties.getProperty(Contains.GRANT_TYPE);




        //通过mysql查询refreshToken
        String refreshToken = MysqlUtil.getRefreshToken(properties);
        //System.out.println(refreshToken);

        //拼接请求参数
        String jsonStr = "{\"app_id\": " + appId + ",\"secret\": \"" + secret + "\",\"grant_type\": \"" + grantType + "\",\"refresh_token\": \"" + refreshToken + "\"}";
        //System.out.println(jsonStr);
        //通过发送请求获取新的token
        String tokenJsonStr = TokenUtil.getToken(getTokenUrl, jsonStr);
        System.out.println(tokenJsonStr);

        //转换为josn对象，获取accessToken
        ObjectNode tokenJson = mapper.readValue(tokenJsonStr, ObjectNode.class);
        String accessToken = tokenJson.get("data").get("access_token").asText();
        //System.out.println(accessToken);

        //获取refresh_token
        String rTooken = tokenJson.get("data").get("refresh_token").toString();
        //将新的refresh_token更新到mysql中
        MysqlUtil.insertRToken(properties,rTooken);


        //String accessToken = "afcc4d804e8aee74db09459841348cfdcce62971";
        //广告数据插入myql
        System.out.println("字节广告计划插入开始");
        AdProgram.adPorgram(properties,accessToken);
        System.out.println("字节广告计划插入结束");

        Thread.sleep(30000);

        System.out.println("字节受众兴趣插入开始");
        AudInterest.audInterest(properties,accessToken);
        System.out.println("字节受众兴趣插入结束");

        Thread.sleep(30000);

        System.out.println("字节受众分析插入结束");
        AudInsights.audInsights(properties,accessToken);
        System.out.println("字节受众分析插入结束");

        Thread.sleep(30000);

        System.out.println("腾讯日报插入开始");
        AdDaily.adDaily(properties);
        System.out.println("腾讯日报插入完成");

        Thread.sleep(30000);

        System.out.println("微信腾讯日报插入开始");
        WeChatAdDaily.weChatAdDaily(properties);
        System.out.println("微信腾讯日报插入完成");

        Thread.sleep(30000);

        System.out.println("腾讯推广插入开始");
        QQCampaigns.qqCampaigns(properties);
        System.out.println("腾讯推广插入结束");

        Thread.sleep(30000);

        System.out.println("腾讯人群插入开始");
        Crowd.crowd(properties);
        System.out.println("腾讯人群插入完成");

    }
}



