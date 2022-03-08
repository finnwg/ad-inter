package com.zhiji.functions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zhiji.config.Contains;
import com.zhiji.utils.MysqlUtil;
import com.zhiji.utils.TokenUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
/**
 * @author finnwg
 * @version 1.0
 * @desc
 * @date 2022/3/3 17:49
 */
public class AudInsights {
    public static ObjectMapper mapper = new ObjectMapper();
    public static String httpUrl;
    public static String startDate;
    public static String endDate;
    public static String groupBy;
    public static String pageSize;
    public static String goalTable;
    public static String fields;
    public static String advertiser_id;

    public static void audInsights(Properties properties , String accessToken) throws IOException, SQLException, ClassNotFoundException {

        //get请求url
        httpUrl = properties.getProperty("http.program.url");
        //开始结束时间
        startDate = properties.getProperty(Contains.START_DATE);
        endDate = properties.getProperty(Contains.END_DATE);
        //System.out.println(startDate);
        //System.out.println(endDate);

        //get请求url参数
        pageSize = properties.getProperty(Contains.PAGE_SIZE);

        //目标表名称
        goalTable = properties.getProperty("http.audInsights.table");
        String[] dimSub = properties.getProperty("http.audInsights.dimensions").split(",");
/*
        String[] cols = "cost,show,avg_show_cost,click,avg_click_cost,ctr,convert,convert_cost,convert_rate,phone,form,button,view,redirect,total_play,valid_play,valid_play_cost,valid_play_rate,play_25_feed_break,play_50_feed_break,play_75_feed_break,play_100_feed_break,average_play_time_per_play,play_over_rate,play_duration_sum,card_show,share,comment,like,follow,home_visited,message_action,click_landing_page,click_shopwindow,click_website,click_download".split(",");
        List<String> list = Arrays.asList(cols);
*/

        //int count = 0;
        for (int h = 0; h < dimSub.length; h++) {

            groupBy = dimSub[h];
            fields = properties.getProperty("http.audInsights.fields") ;
            //System.out.println(fields);
            //System.out.println(groupBy);
            //获取所有的广告id
            String[] adIds = properties.getProperty(Contains.ADVER_ID).split(",");

            for (int i = 0; i < adIds.length; i++) {
                advertiser_id=adIds[i];
                //遍历广告id，拼接url
                String url = httpUrl + "advertiser_id=" + advertiser_id + "&start_date=" + startDate + "&end_date=" + endDate + "&group_by=" +  "[\"" + groupBy + "\"]" + "&page_size=" + pageSize + "&fields=" + fields;
                //System.out.println(url);

                //通过url获取json结果，转换为json对象
                String jsonResult = TokenUtil.get(url, accessToken);
                //System.out.println(jsonResult);
                ObjectNode jsonNodes = mapper.readValue(jsonResult, ObjectNode.class);
                //System.out.println(jsonNodes.toString());

                //获取页数
                long total_page = jsonNodes.get("data").get("page_info").get("total_page").asLong();
                //System.out.println(total_page);
                //遍历所有页
                for (int j = 1; j < total_page + 1; j++) {

                    //拼接带页码的url，获取结果数据，转换为json对象
                    url = httpUrl + "advertiser_id=" + advertiser_id + "&start_date=" + startDate + "&end_date=" + endDate + "&group_by=" +  "[\"" + groupBy + "\"]" + "&page_size=" + pageSize + "&page=" + j+ "&fields=" + fields;
                    //System.out.println(url);
                    jsonResult = TokenUtil.get(url, accessToken);
                    jsonNodes = mapper.readValue(jsonResult, ObjectNode.class);

                    //遍历list数组，拿到所有数据
                    Iterator<JsonNode> listJson = jsonNodes.get("data").get("list").iterator();
                    while (listJson.hasNext()) {
                        JsonNode json = listJson.next();

                        //定义列名和对应值
                        String values = "";
                        String columns = "";

                        JsonNode metrics = json.get("metrics");
                        //遍历所有字段。给字段名和值赋值
                        Iterator<Map.Entry<String, JsonNode>> metFields = metrics.fields();
                        while (metFields.hasNext()) {
                            Map.Entry<String, JsonNode> next = metFields.next();
                            String key = next.getKey();
                            String value = next.getValue().toString();
                            values = values + value + ",";
                            columns = columns + "`" + key + "`,";
                        }

                        JsonNode dimensions = json.get("dimensions");
                        Iterator<Map.Entry<String, JsonNode>> dimFields = dimensions.fields();
                        while (dimFields.hasNext()) {
                            Map.Entry<String, JsonNode> next = dimFields.next();
                            String key = next.getKey();
                            String value = next.getValue().toString();
                            values = values + value + ",";
                            columns = columns + "`" + key + "`,";
                        }

                        //拼接字段名和值
                        values = values.substring(0,values.length() -1)+ ",\"" + startDate + "\",\"" + endDate + "\",\"" + advertiser_id + "\",\"" + groupBy + "\"" ;
                        columns = columns.substring(0,columns.length() -1)+ ",start_date,end_date,advertiser_id,group_by";

                        //拼接sql。打印
                        //String insertSql = "INSERT INTO " + goalTable + "(" + columns + ")" + " values (" + values + ")";
                        //System.out.println(insertSql);
                        //插入mysql对应表中
                        MysqlUtil.mysqlFunction(properties,columns,values,goalTable);
                        //count ++;
                    }
                }
            }
        }
    }
}
