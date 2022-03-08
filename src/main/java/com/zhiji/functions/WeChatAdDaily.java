package com.zhiji.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zhiji.config.Contains;
import com.zhiji.utils.MysqlUtil;
import com.zhiji.utils.TokenUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * @author finnwg
 * @version 1.0
 * @desc
 * @date 2022/3/3 9:39
 */
public class WeChatAdDaily {
    public static ObjectMapper mapper = new ObjectMapper();
    public static String httpUrl;
    public static String startDate;
    public static String endDate;
    public static String groupBy;
    public static String level;
    public static String timeLine;
    public static String pageSize;
    public static String fields;
    public static String goalTable;
    public static String[] tokenAndAccountId;


    public static void weChatAdDaily(Properties properties) throws IOException, SQLException, ClassNotFoundException {

        //get请求url
        httpUrl = properties.getProperty("http.qq.we.daily.url");
        //开始结束时间
        startDate = properties.getProperty(Contains.START_DATE);
        endDate = properties.getProperty(Contains.END_DATE);

        //拼接时间范围
        String date_range = "{\"start_date\":\"" + startDate + "\",\"end_date\":\"" + endDate + "\"}";


        //get请求url参数
        groupBy = properties.getProperty("http.qq.we.daily.group_by");
        level = properties.getProperty("http.qq.we.daily.level");
        timeLine = properties.getProperty("http.qq.we.daily.time_line");
        pageSize = properties.getProperty("http.qq.we.daily.page_size");
        fields = properties.getProperty("http.qq.we.daily.fields");


        //目标表名称
        goalTable = properties.getProperty("http.qq.we.daily.table");


        //过去token和accountId，遍历
        tokenAndAccountId = properties.getProperty("http.qq.we.daily.token&account_id").split(",");
        for (int i = 0; i < tokenAndAccountId.length; i++) {
            //拆解token和accountId
            String token = tokenAndAccountId[0].split("-")[0];
            String accountId = tokenAndAccountId[0].split("-")[1];

            //获取当前时间戳
            long timestamp = System.currentTimeMillis() / 1000;


            //创建全局唯一nonce
            Random random = new Random();
            String nonce = (timestamp / 100000 + random.nextInt(100000) + "").hashCode() + "";


            String url = httpUrl + "access_token=" + token + "&timestamp=" + timestamp + "&nonce=" + nonce + "&account_id=" + accountId + "&level=" + level + "&date_range=" + date_range + "&time_line=" + timeLine + "&group_by=" + groupBy + "&page_size=" + pageSize + "&fields=" + fields;
            //System.out.println(url);
            String jsonResult = TokenUtil.qqGet(url);
            //System.out.println(jsonResult);

            //解析返回结果
            ObjectNode jsonNodes = mapper.readValue(jsonResult, ObjectNode.class);
            if (!"{}".equals(jsonNodes.get("data").toString())) {
                //获取页数
                long total_page = jsonNodes.get("data").get("page_info").get("total_page").asLong();

                //遍历所有页
                for (int j = 1; j < total_page + 1 ; j++) {
                    //拼接带页码的url，获取结果数据，转换为json对象
                    //重新生成nonce
                    nonce = nonce.hashCode() + "";
                    url = httpUrl + "access_token=" + token + "&timestamp=" + timestamp + "&nonce=" + nonce + "&account_id=" + accountId + "&level=" + level + "&date_range=" + date_range + "&time_line=" + timeLine + "&group_by=" + groupBy + "&page_size=" + pageSize + "&page=" + j + "&fields=" + fields;

                    //System.out.println(url);
                    //获取返回结果
                    jsonResult = TokenUtil.qqGet(url);
                    //System.out.println(jsonResult);
                    jsonNodes = mapper.readValue(jsonResult, ObjectNode.class);


                    //遍历list数组，拿到所有数据
                    Iterator<JsonNode> listJson = jsonNodes.get("data").get("list").iterator();
                    while (listJson.hasNext()) {
                        JsonNode json = listJson.next();

                        //定义列名和对应值
                        String values = "";
                        String columns = "";

                        Iterator<Map.Entry<String, JsonNode>> fields = json.fields();
                        //遍历所有字段。给字段名和值赋值
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> next = fields.next();
                            String key = next.getKey();
                            String value = next.getValue().toString();

                            columns = columns + key + ",";
                            values = values + value + ",";
                        }
                        //拼接字段名和值
                        values = values.substring(0,values.length() -1);
                        columns = columns.substring(0,columns.length() -1);

                        //String sql = "INSERT INTO " + "tablename" + "(" + columns + ")" + " values (" + values + ")";
                        //System.out.println(sql);
                        MysqlUtil.mysqlFunction(properties,columns,values,goalTable);
                    }
                }
            }
        }
    }
}
