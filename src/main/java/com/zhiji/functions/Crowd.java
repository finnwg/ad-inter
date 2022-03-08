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
 * @date 2022/3/3 11:02
 */
public class Crowd {
    public static ObjectMapper mapper = new ObjectMapper();
    public static String httpUrl;
    public static String startDate;
    public static String endDate;
    //public static String groupBy;
    public static String dimensionType;
    public static String audienceId;
    //public static String level;
    //public static String timeLine;
    //public static String pageSize;
    public static String fields;
    public static String goalTable;
    public static String[] tokenAndAccountId;


    public static void crowd(Properties properties) throws IOException, SQLException, ClassNotFoundException {

        //get请求url
        httpUrl = properties.getProperty("http.qq.crowd.url");
        //开始结束时间
        startDate = properties.getProperty(Contains.START_DATE);
        endDate = properties.getProperty(Contains.END_DATE);



        //get请求url参数
        audienceId = properties.getProperty("http.qq.crowd.audience_id");
        dimensionType = properties.getProperty("http.qq.crowd.dimension_type");
        fields = properties.getProperty("http.qq.crowd.fields");


        //目标表名称
        goalTable = properties.getProperty("http.qq.crowd.table");


        //过去token和accountId，遍历
        tokenAndAccountId = properties.getProperty("http.qq.crowd.token&account_id").split(",");
        for (int i = 0; i < tokenAndAccountId.length; i++) {
            //拆解token和accountId
            String token = tokenAndAccountId[0].split("-")[0];
            String accountId = tokenAndAccountId[0].split("-")[1];

            String[] dimTypes = dimensionType.split(",");
            for (int h = 0; h < dimTypes.length; h++) {
                String dimType = dimTypes[h];
                //获取当前时间戳
                long timestamp = System.currentTimeMillis() / 1000;


                //创建全局唯一nonce
                Random random = new Random();
                String nonce = (timestamp / 100000 + random.nextInt(100000) + "").hashCode() + "";


                String url = httpUrl + "access_token=" + token + "&timestamp=" + timestamp + "&nonce=" + nonce + "&account_id=" + accountId + "&audience_id=" + audienceId + "&dimension_type=" + dimType + "&time_line=" + "&fields=" + fields;
                //System.out.println(url);
                String jsonResult = TokenUtil.qqGet(url);
                //System.out.println(jsonResult);

                //解析返回结果
                ObjectNode jsonNodes = mapper.readValue(jsonResult, ObjectNode.class);
                if (!"{}".equals(jsonNodes.get("data").toString())) {

                    Iterator<JsonNode> jsonList = jsonNodes.get("data").get("list").iterator();
                    while (jsonList.hasNext()) {
                        JsonNode subJson = jsonList.next();
                        String type = subJson.get("dimension_type").toString();
                        String matchRate = subJson.get("match_rate").toString();
                        Iterator<JsonNode> distribution = subJson.get("distribution").iterator();
                        while (distribution.hasNext()) {
                            JsonNode next = distribution.next();

                            String values = "";
                            String columns = "";

                            Iterator<Map.Entry<String, JsonNode>> fields = next.fields();
                            while (fields.hasNext()) {
                                Map.Entry<String, JsonNode> field = fields.next();
                                String key = field.getKey();
                                String value = field.getValue().toString();

                                columns = columns + key + ",";
                                values = values + value + ",";
                            }

                            columns = columns + "dimension_type,match_rate,account_id,start_date,end_date";
                            values = values + type + "," + matchRate + "," + accountId + ",\"" + startDate + "\",\"" + endDate + "\"";


                            //String sql = "INSERT INTO " + "tablename" + "(" + columns + ")" + " values (" + values + ")";
                            //System.out.println(sql);
                            MysqlUtil.mysqlFunction(properties,columns,values,goalTable);
                        }
                    }
                }
            }
        }
    }
}
