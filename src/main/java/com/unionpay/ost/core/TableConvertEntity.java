package com.unionpay.ost.core;

import com.unionpay.ost.bean.EntityMeta;
import com.unionpay.ost.bean.FieldMeta;
import com.unionpay.ost.bean.SequenceMeta;
import com.unionpay.ost.config.Configuration;
import com.unionpay.ost.config.DataBaseRelateConstant;
import com.unionpay.ost.exception.MyException;
import com.unionpay.ost.table.ColumnInfo;
import com.unionpay.ost.table.TableInfo;
import com.unionpay.ost.utils.DatabaseTypeConvertUtil;
import com.unionpay.ost.utils.JDBCUtils;
import com.unionpay.ost.utils.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 将表信息转换为对应的实体类信息
 * Created by jsf on 16/8/6..
 */
public class TableConvertEntity {


    /**
     * 根据不同的数据库类型将数据库对应的数据表转化为实体类对象
     *
     * @return 实体类对象集合
     */
    public static List<EntityMeta> accordDBTypeObtainEntityMetas() {
        List<EntityMeta> entityMetaList = new ArrayList<EntityMeta>();
        String jdbcUrl = Configuration.getJdbcUrl();
        if (jdbcUrl.contains(DataBaseRelateConstant.MYSQL)) {
            entityMetaList = obtainEntityMetas(DataBaseRelateConstant.MYSQL);
        } else if (jdbcUrl.contains(DataBaseRelateConstant.DB2)) {
            entityMetaList = obtainEntityMetas(DataBaseRelateConstant.DB2);
        }
        return entityMetaList;
    }

    /**
     * 将数据库对应的数据表转化为实体类对象
     *
     * @return 实体类对象集合
     */
    private static List<EntityMeta> obtainEntityMetas(String dbType) {
        Connection connection = DataBaseManager.openConnection();
        System.out.println("已经连接到" + dbType + "数据库,开始生成数据库表对应的entity类");
        PreparedStatement ps = null;
        ResultSet rs = null;
        String tableName = null;
        List<EntityMeta> entityMetaList = new ArrayList<EntityMeta>();

        //如果没有配置数据库表,则执行该段代码,直接连接数据库,将数据库中的所有表直接生成entity类
        if (StringUtils.isStrArrayEmpty(Configuration.getTableNames())) {

            try {
                if (DataBaseRelateConstant.MYSQL.equalsIgnoreCase(dbType)) {
                    System.out.println("查询" + Configuration.getSchema() + "下的数据表");
                    System.out.println("***********************************************************");
                    ps = connection.prepareStatement("show tables from " + Configuration.getSchema());
                } else if (DataBaseRelateConstant.DB2.equalsIgnoreCase(dbType)) {
                    System.out.println("查询" + Configuration.getSchema() + "下的数据表");
                    System.out.println("***********************************************************");
                    ps = connection.prepareStatement("select * from syscat.tables where tabschema=?");
                    ps.setString(1, Configuration.getSchema());
                }
            } catch (SQLException e) {
                JDBCUtils.shutDownDataBaseResource(null, ps, connection, true);
                throw new MyException(e, "无法创建PrepareStatement对象");
            }
            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    EntityMeta entityMeta = new EntityMeta();
                    TableInfo tableInfo = new TableInfo();
                    if (DataBaseRelateConstant.MYSQL.equalsIgnoreCase(dbType)) {
                        tableName = rs.getString("Tables_in_" + Configuration.getSchema());
                        System.out.println("开始组装" + tableName + "表对应的Entity类");
                    } else if (DataBaseRelateConstant.DB2.equalsIgnoreCase(dbType)) {
                        tableName = rs.getString("TABNAME");
                        System.out.println("开始组装" + tableName + "表对应的Entity类");
                    }
                    //这里主要用到是数据表名
                    tableInfo.setTableName(tableName);
                    //组装表实体对象模型
                    entityMeta.setEntityName(StringUtils.capInMark(tableName, DataBaseRelateConstant.TABLESPILTMARK, true));
                    if (DataBaseRelateConstant.MYSQL.equalsIgnoreCase(dbType)) {
                        System.out.println("<开始组装" + entityMeta.getEntityName() + "实体类>");
                        entityMeta.setFieldMetaList(MySqlTableConvertAssist.obtainFieldMetasForMySQL(connection, tableName, DataBaseRelateConstant.MYSQL, entityMetaList));
                    } else if (DataBaseRelateConstant.DB2.equalsIgnoreCase(dbType)) {
                        System.out.println("<开始组装" + entityMeta.getEntityName() + "实体类>");
                        entityMeta.setFieldMetaList(Db2TableConvertAssist.obtainFieldMetasForDB2(connection, tableName, DataBaseRelateConstant.DB2, entityMetaList));
                    }
                    entityMeta.setTableInfo(tableInfo);
                    System.out.println("成功组装" + tableName + "表对应的实体类" + entityMeta.getEntityName());
                    System.out.println("***********************************************************");
                    entityMetaList.add(entityMeta);
                }
            } catch (SQLException e) {
                throw new MyException(e, "无法执行查询数据库表的SQL语句");
            } finally {
                //关闭相关资源
                JDBCUtils.shutDownDataBaseResource(rs, ps, connection);
            }

        } else {

            try {
                //如果配置数据库表,则执行该段代码,用于单表生成代码
                for (String tabName : Configuration.getTableNames()) {
                    if (DataBaseCommonAssist.isExistTable(connection, tabName, dbType) != 0) {
                        if (DataBaseRelateConstant.MYSQL.equalsIgnoreCase(dbType)) {
                            System.out.println("开始组装" + tabName + "表对应的Entity类");
                            EntityMeta entityMeta = new EntityMeta();
                            TableInfo tableInfo = new TableInfo();
                            //这里主要用到是数据表名
                            tableInfo.setTableName(tabName);
                            //组装表实体对象模型
                            entityMeta.setEntityName(StringUtils.capInMark(tabName, DataBaseRelateConstant.TABLESPILTMARK, true));
                            System.out.println("<开始组装" + entityMeta.getEntityName() + "实体类>");
                            entityMeta.setFieldMetaList(MySqlTableConvertAssist.obtainFieldMetasForMySQL(connection, tabName, DataBaseRelateConstant.MYSQL, entityMetaList));
                            entityMeta.setTableInfo(tableInfo);
                            System.out.println("成功组装" + tabName + "表对应的实体类" + entityMeta.getEntityName());
                            System.out.println("***********************************************************");
                            entityMetaList.add(entityMeta);
                        } else if (DataBaseRelateConstant.DB2.equalsIgnoreCase(dbType)) {
                            System.out.println("开始组装" + tabName + "表对应的Entity类");
                            EntityMeta entityMeta = new EntityMeta();
                            TableInfo tableInfo = new TableInfo();
                            //这里主要用到是数据表名
                            tableInfo.setTableName(tabName);
                            //组装表实体对象模型
                            entityMeta.setEntityName(StringUtils.capInMark(tabName, DataBaseRelateConstant.TABLESPILTMARK, true));
                            System.out.println("<开始组装" + entityMeta.getEntityName() + "实体类>");
                            entityMeta.setFieldMetaList(Db2TableConvertAssist.obtainFieldMetasForDB2(connection, tabName, DataBaseRelateConstant.DB2, entityMetaList));
                            entityMeta.setTableInfo(tableInfo);
                            System.out.println("成功组装" + tabName + "表对应的实体类" + entityMeta.getEntityName());
                            System.out.println("***********************************************************");
                            entityMetaList.add(entityMeta);
                        }
                    }else{
                        throw new MyException("不存在该表,请查看表名是否写正确");
                    }
                }

            } catch (Exception e) {
                throw new MyException(e, "单表生成代码过程中发生异常");
            } finally {
                //关闭相关资源
                JDBCUtils.shutDownDataBaseResource(rs, ps, connection);
            }
        }
        return entityMetaList;
    }
}
