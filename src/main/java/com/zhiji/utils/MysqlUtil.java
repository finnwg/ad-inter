package com.zhiji.utils;

import com.zhiji.config.Contains;

import java.sql.*;
import java.util.Properties;

/**
 * @author finnwg
 * @version 1.0
 * @desc
 * @date 2022/2/28 13:13
 */
public class MysqlUtil {

    public static String url;
    public static String userName;
    public static String password;
    public static String tableName;
    public static Connection connection;
    public static Statement statement;
    public static ResultSet resultSet;
    //public static String accessToken;
    public static String refreshToken;

    public static String goalUrl;
    public static String goalUserName;
    public static String goalPassword;





    public static String getRefreshToken(Properties properties) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        url = properties.getProperty(Contains.MYSQL_URL);
        userName = properties.getProperty(Contains.MYSQL_USER);
        password = properties.getProperty(Contains.MYSQL_PW);
        tableName = properties.getProperty(Contains.MYSQL_TABLE);

        connection = DriverManager.getConnection(url, userName, password);

        //从mysql中查询token
        String querySql = "select * from " + tableName;

        statement = connection.createStatement();
        resultSet = statement.executeQuery(querySql);


        while (resultSet.next()) {
            //accessToken = resultSet.getString("access_token");
            refreshToken = resultSet.getString("refresh_token");
        }


        resultSet.close();
        statement.close();
        connection.close();

        return refreshToken;
    }

    public static void insertRToken(Properties properties,String rToken) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        url = properties.getProperty(Contains.MYSQL_URL);
        userName = properties.getProperty(Contains.MYSQL_USER);
        password = properties.getProperty(Contains.MYSQL_PW);
        tableName = properties.getProperty(Contains.MYSQL_TABLE);

        connection = DriverManager.getConnection(url, userName, password);

        String updateSql = "UPDATE " + tableName + " set refresh_token=" + rToken;
        statement = connection.createStatement();
        statement.executeUpdate(updateSql);

        statement.close();
        connection.close();
    }

    /**
     * @description  mysql插入数据方法
     * @author      finnwg
     * @updateTime 2022/2/28 17:01
     * @param
     * @return
     */
    public static void mysqlFunction(Properties properties,String columns,String values,String tableName) throws ClassNotFoundException, SQLException {

        //定义myql参数
        Class.forName("com.mysql.cj.jdbc.Driver");
        goalUrl = properties.getProperty(Contains.MYSQL_GOAL_URL);
        goalUserName = properties.getProperty(Contains.MYSQL_GOAL_USER);
        goalPassword = properties.getProperty(Contains.MYSQL_GOAL_PW);

        Connection connection = DriverManager.getConnection(goalUrl, goalUserName, goalPassword);



        //4.定义并初始化SQL语句
        String insertSql = "INSERT INTO " + tableName + "(" + columns + ")" + " values (" + values + ")";
        //System.out.println(insertSql);
        PreparedStatement pst = connection.prepareStatement(insertSql);


        pst.addBatch();


        pst.executeBatch();

        pst.close();
        connection.close();
    }
}
