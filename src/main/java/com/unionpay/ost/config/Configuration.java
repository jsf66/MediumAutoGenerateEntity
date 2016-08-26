package com.unionpay.ost.config;

import com.unionpay.ost.exception.MyException;
import com.unionpay.ost.utils.StringUtils;

import java.io.*;
import java.util.Properties;

/**
 * 管理配置信息
 * Created by jsf on 16/8/3..
 */
public class Configuration {
    /**
     * 驱动类
     */
    private static String driverName;
    /**
     * 数据库用户名
     */
    private static String userName;
    /**
     * 数据库密码
     */
    private static String passWord;
    /**
     * jdbc的Url
     */
    private static String jdbcUrl;
    /**
     * 项目的源码路径
     */
    private static String srcPath;
    /**
     * 生成的Entity类所在包名
     */
    private static String projectPackageName;
    /**
     * 数据库名
     */
    private static String schema;
    /**
     * 作者
     */
    private static String author = "ost";
    /**
     * 用以支持单个或多个数据库表实体类的生成,数据库表之间使用逗号分隔
     */
    private static String[] tableNames;



    public Configuration() {

    }

    /**
     * 将连接数据的相关配置文件加载
     */
    static {
        //对于打成jar包后这种获取配置文件的方式,无法获取到配置文件
//       InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties");
        //只能采用这种方式来获取相应的配置文件内容
        File file=new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        InputStream in = null;
        try {
            in = new FileInputStream(file.getParent()+"/db.properties");
        } catch (FileNotFoundException e) {
            throw new MyException(e,"无法读取数据库配置文件");
        }
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new MyException(e, "无法加载配置文件");
        }
        Configuration.setDriverName(properties.getProperty("driverName"));
        Configuration.setJdbcUrl(properties.getProperty("jdbcUrl"));
        Configuration.setUserName(properties.getProperty("userName"));
        Configuration.setPassWord(properties.getProperty("passWord"));
        Configuration.setProjectPackageName(properties.getProperty("projectPackageName"));
        Configuration.setSrcPath(properties.getProperty("srcPath"));
        Configuration.setSchema(properties.getProperty("schema"));
        String tableNameStr = properties.getProperty("tableNames");
        Configuration.setTableNames(handleTableNameStr(tableNameStr));
    }

    //处理表名字符串
    private static String[] handleTableNameStr(String str) {
        String[] tableNameArray = null;
        if (!StringUtils.isEmpty(str)) {
            String handleTableNameStr = str.toUpperCase().trim();
            try {
                if (handleTableNameStr.contains(",")) {
                    tableNameArray = handleTableNameStr.split(",");
                    if (tableNameArray != null && tableNameArray.length != 0) {
                        Configuration.setTableNames(tableNameArray);
                    }
                } else {
                    tableNameArray=new String[]{handleTableNameStr};
                }

            } catch (Exception e) {
                throw new MyException(e,"有非法字符,无法进行分隔");
            }
        }
        return tableNameArray;

    }

    public static String getDriverName() {
        return driverName;
    }

    public static void setDriverName(String driverName) {
        Configuration.driverName = driverName;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        Configuration.userName = userName;
    }

    public static String getPassWord() {
        return passWord;
    }

    public static void setPassWord(String passWord) {
        Configuration.passWord = passWord;
    }

    public static String getJdbcUrl() {
        return jdbcUrl;
    }

    public static void setJdbcUrl(String jdbcUrl) {
        Configuration.jdbcUrl = jdbcUrl;
    }

    public static String getSrcPath() {
        return srcPath;
    }

    public static void setSrcPath(String srcPath) {
        Configuration.srcPath = srcPath;
    }

    public static String getProjectPackageName() {
        return projectPackageName;
    }

    public static void setProjectPackageName(String projectPackageName) {
        Configuration.projectPackageName = projectPackageName;
    }

    public static String getSchema() {
        return schema;
    }

    public static void setSchema(String schema) {
        Configuration.schema = schema;
    }

    public static String getAuthor() {
        return author;
    }

    public static String[] getTableNames() {
        return tableNames;
    }

    public static void setTableNames(String[] tableNames) {
        Configuration.tableNames = tableNames;
    }
}
