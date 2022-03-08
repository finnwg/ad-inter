package com.zhiji.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author finnwg
 * @version 1.0
 * @desc
 * @date 2022/2/27 16:57
 */
@Slf4j
public class ConfigUtil {

    public static Properties pro = new Properties();
    public static BufferedReader bufferedReader;

    public static Properties gerConfig(String path) {


        try {
            bufferedReader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            log.info("文件不存在");
            e.printStackTrace();
        }


        try {
            pro.load(bufferedReader);
        } catch (IOException e) {
            log.info("配置文件加载失败");
            e.printStackTrace();
        }


        return pro;

    }
}
