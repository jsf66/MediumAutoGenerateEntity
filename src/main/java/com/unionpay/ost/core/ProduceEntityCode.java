package com.unionpay.ost.core;

import com.unionpay.ost.bean.EntityMeta;
import com.unionpay.ost.config.Configuration;
import com.unionpay.ost.config.VelocityTemplStyle;
import com.unionpay.ost.exception.MyException;
import com.unionpay.ost.utils.FileUtils;
import com.unionpay.ost.utils.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Created by jsf on 16/8/8..
 * Version 1.1
 */
public class ProduceEntityCode {
    private static Template template=null;
    static VelocityEngine velocityEngine=null;
    //初始化相关的模板参数
    static{
        Properties properties=new Properties();
        //设置velocity资源加载方式为class
        properties.setProperty("resource.loader", "class");
        //设置velocity资源加载方式为file时的处理类
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        //设置模板编码
        properties.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
        properties.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        properties.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
        //实例化一个VelocityEngine对象
        velocityEngine=new VelocityEngine(properties);
    }

    public static void main(String[] args){
        constructTemplate(TableConvertEntity.accordDBTypeObtainEntityMetas());
    }
    //构造模板
    private static void constructTemplate(List<EntityMeta>  entityMetaList){
        VelocityContext context = new VelocityContext();
        context.put("StringUtils", StringUtils.class);
        context.put("Configuration",Configuration.class);
        if(entityMetaList!=null&&entityMetaList.size()!=0){
            //先删除原来已经存在的文件
            FileUtils.deleteAllFile(new File(Configuration.getSrcPath()+File.separator+Configuration.getProjectPackageName().replace('.','/')));
            for(EntityMeta entityMeta:entityMetaList){
                context.put("entity",entityMeta);
                generateEntityFile(context);
            }
        }

    }
    //生成相应的实体类文件
    private static void generateEntityFile(VelocityContext context){
        EntityMeta entityMeta=(EntityMeta)context.get("entity");
        File file = new File(Configuration.getSrcPath()+File.separator+Configuration.getProjectPackageName().replace('.','/')+File.separator+entityMeta.getEntityName()+".java");
        FileUtils.deleteExistFile(file);
        FileWriter fileWriter=null;
        //实例化一个StringWriter
        StringWriter writer=new StringWriter();
        velocityEngine.mergeTemplate("com/unionpay/ost/model/entity.vm",VelocityTemplStyle.TEMPLATECODE, context, writer);
        try {
//            fileWriter=new FileWriter(file);
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file, true),"UTF-8");
            osw.write(writer.toString());
            osw.flush();
            osw.close();
        } catch (IOException e) {
            throw new MyException(e,"写出文件异常");
        }finally {
            try {
                if(writer!=null){
                    writer.close();
                }
                if(fileWriter!=null){
                    fileWriter.close();
                }
            } catch (IOException e) {
                throw new MyException(e,"无法关闭流");
            }
        }

    }

}
