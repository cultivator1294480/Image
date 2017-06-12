package test;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

public class DBUtil {
    //连接池对象
    private static BasicDataSource ds;
    //加载参数
    static{
        //创建连接池
		ds = new BasicDataSource();
		//设置参数
		ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		ds.setUrl("jdbc:oracle:" + "thin:@10.1.3.132:1521:orcl");
		ds.setUsername("v5xuser");
		ds.setPassword("catr654321");
		ds.setInitialSize(1);
		ds.setMaxActive(10);
    }
    /*
     * 以上就是将配置文件里的参数全部读取出来，接下来就是要
     * 写两个方法，一个是用来创建连接的，一个关闭连接
     * */
    public static Connection getConnection() throws SQLException{
        return ds.getConnection();
    }
    
    public static void close(Connection conn){
    	//如果有未提交的事务,回滚,关闭 
    	try {
			if(!conn.getAutoCommit()){
				conn.rollback();
				conn.setAutoCommit(true);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("关闭连接失败",e);
            }
        }
    }
    public static void finallyClose(ResultSet result, PreparedStatement pre,Connection conn){
    	
    	//如果有未提交的事务,回滚,关闭 
    	try {
			// 逐一将上面的几个对象关闭，因为不关闭的话会影响性能、并且占用资源
			// 注意关闭的顺序，最后使用的最先关闭
			if (result != null)
				result.close();
			if (pre != null)
				pre.close();
			if(conn != null&&!conn.getAutoCommit()){
				conn.setAutoCommit(true);
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			Logger.logStackTrace(e.getStackTrace());
		}
       
    }
}