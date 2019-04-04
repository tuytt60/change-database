package com.casicloud.db;

import com.casicloud.utils.JdbcUtil;
import org.junit.Test;

import java.lang.annotation.Target;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by admin on 2019/4/4.userId改为10000054381916
 */
public class ChangeDb {


    /**
     * 将sys_model表的userName改为iotconfadmin，
     */
    @Test
    public void changeUsername(){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //获取连接
            conn = JdbcUtil.getConnection();

            String sql = "UPDATE sys_model SET userName=?,userId=?";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            stmt.setString(1,"iotconfadmin");
            stmt.setLong(2,10000054381916L);
            int i = stmt.executeUpdate();
            System.out.println(i);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.close(conn, stmt ,rs);
        }
    }
    /**
     * 将operation_log表的operator改为iotconfadmin，
     */
    @Test
    public void changeOperator(){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //获取连接
            conn = JdbcUtil.getConnection();

            String sql = "UPDATE operation_log SET operator='iotconfadmin'";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            int i = stmt.executeUpdate();
            System.out.println(i);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.close(conn, stmt ,rs);
        }
    }


    /**
     * 将operation_log表的operateDate时间进行调整
     */
    @Test
    public void changeOperateDate(){
//        Connection modelConn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
        try {
            //获取连接
            Connection modelConn = JdbcUtil.getConnection();

            String sql = "select distinct modelCode from sys_model";
            //预编译
            PreparedStatement modelStmt = modelConn.prepareStatement(sql);


            ResultSet modelResultSet = modelStmt.executeQuery();

            int i = 0;
            while (modelResultSet.next()){
                System.out.println(++i);
                String modelCode = modelResultSet.getString("modelCode");
                sql = "select * from sys_model WHERE modelCode = '"+modelCode+"'";
                Connection modelChildConn = JdbcUtil.getConnection();
                PreparedStatement modelChildStmt = modelChildConn.prepareStatement(sql);
                ResultSet modelChildResultSet = modelChildStmt.executeQuery();
                Long modelCreatTime = null;
                while (modelChildResultSet.next()){
                    //从sys_model表获取到的创建时间对应的时间戳
                    modelCreatTime = modelChildResultSet.getTimestamp("creatTime").getTime();
                }
                sql = "select * from operation_log WHERE modelCode = '"+modelCode+"'";
                Connection logConn = JdbcUtil.getConnection();
                PreparedStatement logStmt = logConn.prepareStatement(sql);
                ResultSet logResultSet = logStmt.executeQuery();
                while (logResultSet.next()){
                    //从operation_log表获取到的操作时间对应的时间戳
                    Long logOperateDate = logResultSet.getTimestamp("operateDate").getTime();
                    int id = logResultSet.getInt("id");
                    //如果时间是在3月31日之前的。下面的“1553961600000L”是3月31日的时间戳
                    if(logOperateDate<1553961600000L){
                        sql = "UPDATE operation_log SET operateDate=? WHERE id ="+id+"";
                        Connection logChildConn = JdbcUtil.getConnection();
                        PreparedStatement logChildStmt = logChildConn.prepareStatement(sql);
                        logChildStmt.setDate(1,new java.sql.Date(modelCreatTime));
                        logChildStmt.executeUpdate();
                        JdbcUtil.close(logChildConn, logChildStmt);
                    }

                    if(logOperateDate>=1553961600000L){
                        sql = "UPDATE operation_log SET operateDate=? WHERE id ="+id+"";
                        Connection logChildConn = JdbcUtil.getConnection();
                        PreparedStatement logChildStmt = logChildConn.prepareStatement(sql);
                        logChildStmt.setDate(1,new java.sql.Date(modelCreatTime + 86400000));
                        logChildStmt.executeUpdate();
                        JdbcUtil.close(logChildConn, logChildStmt);
                    }
                }
                JdbcUtil.close(logConn, logStmt,logResultSet);
            }
            JdbcUtil.close(modelConn, modelStmt,modelResultSet);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
//            JdbcUtil.close(modelConn, stmt ,rs);
        }
    }


    /**
     * 将sys_model表的creatTime
     */
    @Test
    public void changeCreatTime(){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //获取连接
            conn = JdbcUtil.getConnection();

//            String sql = "UPDATE sys_model SET creatTime=? WHERE id = 128 ";
//            //预编译
//            stmt = conn.prepareStatement(sql);
//
//            //设置参数
//            stmt.setTimestamp(1,new Timestamp(1554360938000L));
//            int i = stmt.executeUpdate();
//            System.out.println(i);


            //sys_model表的id128~262的creatTime为2018-03-12
            String sql = "SELECT * FROM sys_model WHERE id >= 128 AND id <= 262";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            ResultSet resultSet = stmt.executeQuery();
            int i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-03-12 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


            /*
            *121~150（id263~293）条数据改为2018-03-27 HH:MM:SS保持不变
             */

            sql = "SELECT * FROM sys_model WHERE id >= 263 AND id <= 293";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-03-27 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


            /*
            *（id294~345）条数据改为2018-04-16 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 294 AND id <= 345";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-04-16 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }

            /*
            *（id348~413）条数据改为2018-05-10 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 348 AND id <= 413";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-05-10 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }

            /*
            *（id414~451）条数据改为2018-05-23 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 414 AND id <= 451";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-05-23 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }

            /*
            *（id452~511）条数据改为 2018-06-06 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 452 AND id <= 511";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-06-06 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


            /*
            *（id512~576）条数据改为2018-06-12 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 512 AND id <= 576";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-06-12 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }

            /*
            *(id577~646）条数据改为2018-06-14 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 577 AND id <= 646";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-06-14 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


            /*
            *（id647~701）条数据改为2018-07-11 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 647 AND id <= 701";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-07-11 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


            /*
            *（id702~812）条数据改为2018-07-23 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 702 AND id <= 812";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-07-23 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }



            /*
            *（id813~892）条数据改为2018-07-30 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 813 AND id <= 892";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-07-30 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }



            /*
            *（id893~932）条数据改为2018-08-08 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 893 AND id <= 932";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-08-08 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }

            /*
            *（id933~1002）条数据改为2018-08-21 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 933 AND id <= 1002";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-08-21 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


            /*
            *（id1003~1052）条数据改为2018-08-28 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 1003 AND id <= 1052";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-08-28 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }

            /*
            *（id1053~1117）条数据改为2018-09-05 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 1053 AND id <= 1117";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-09-05 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


            /*
            *（id1118~1152）条数据改为2018-09-18 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 1118 AND id <= 1152";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-09-18 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }

            /*
            *（id1153~1333）条数据改为2018-09-26 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 1153 AND id <= 1333";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-09-26 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }

            /*
            *（id1334~1450）条数据改为2018-10-15 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 1334 AND id <= 1450";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-10-15 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


            /*
            *（id1451~1525）条数据改为2018-10-23 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 1451 AND id <= 1525";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-10-23 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


            /*
            *（id1526~1605）条数据改为2018-11-05 HH:MM:SS保持不变
             */
            sql = "SELECT * FROM sys_model WHERE id >= 1526 AND id <= 1605";
            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            resultSet = stmt.executeQuery();
            i=0;
            while (resultSet.next()){
                System.out.println(++i);
                Timestamp creatTime = resultSet.getTimestamp("creatTime");
                long id = resultSet.getLong("id");
//                SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String creatTimeText = sdf.format(new java.util.Date(creatTime.getTime()));
                String shiFenMiao = creatTimeText.split(" ")[1];
                sql = "UPDATE sys_model SET creatTime=? WHERE id = "+id+" ";
                Date newDate = sdf.parse("2018-11-05 " + shiFenMiao);
                stmt = conn.prepareStatement(sql);
                long time = newDate.getTime();
                stmt.setTimestamp(1,new Timestamp(time));
                stmt.executeUpdate();
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.close(conn, stmt ,rs);
        }
    }

    public static void main(String[] args) {

        try {
//            String string = "2019-10-24 21:59:06";
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            System.out.println(sdf.parse(string));
            Date date = new Date(1553961600000L);
            System.out.println(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testByPreparedStatement1(){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //获取连接
            conn = JdbcUtil.getConnection();

            String sql = "select * from sys_model WHERE id >= ? and id <= ?";

            //预编译
            stmt = conn.prepareStatement(sql);

            //设置参数
            stmt.setInt(1,128);
            stmt.setInt(2,262);

            rs = stmt.executeQuery();
            int i = 0;
            while (rs.next()){
                System.out.println(i++);
                String userName = rs.getString("userName");
                System.out.println(userName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.close(conn, stmt ,rs);
        }
    }
}
