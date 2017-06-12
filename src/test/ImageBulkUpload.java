package test;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;


public class ImageBulkUpload {
//	public static String readFilePath = "H:/contarctFile/readFilePath";
//	public static String readFilePath = "H:/contarctFile/20170525收入合同全部附件";
//	public static String readFilePath = "H:/contarctFile/支出合同+其他合同/2017052506支出合同";
	public static String readFilePath = "H:/contarctFile/支出合同+其他合同/othercontract";
	
	public static String renameFilePath = "H:/contarctFile/renameFilePath";
	public static Integer totalFile = 0;
	public static Set<String> contactFilePathSet;
	
	

	public static void main(String[] args) {
		contactFilePathSet = new HashSet<String>();
		traverseFolder1(readFilePath);
		System.out.println("合同上传完成");
		for (String contactFilePath : contactFilePathSet) {
//			Logger.log("未保存合同:"+contactFilePath);
			getContract(new File(contactFilePath));
		}
		
	}

	public static void traverseFolder1(String path) {

		File file = new File(path);
		if (file.exists()) {
			// 一级子目录
			File[] subFiles = file.listFiles();
			int i =0;
			for (File subFile : subFiles) {
				i++;
				Logger.log(i+"-----------------------------------------------------------------------------------------------");
				Logger.log("处理第"+i+"个合同:"+subFile.getAbsolutePath());
				saveFile(subFile);
			}
		} else {
			System.out.println("文件不存在!");
		}
	}

	/**
	 * 保存文件
	 * 
	 * @param subFile
	 */
	private static void saveFile(File file) {

		// 根据文件查出对应合同
		Contract contract = getContract(file);

		// 一级子目录
		File[] subFiles = file.listFiles();
		for (File subFile : subFiles) {
			// 保存合同归档扫描件
			if (null != contract &&( "合同附件".equals(subFile.getName())|| "att".equals(subFile.getName()))) {
				saveArchiveAttachment(subFile,contract);
			}
			if (null == contract &&( "合同附件".equals(subFile.getName())|| "att".equals(subFile.getName()))) {
				logArchiveAttachment(subFile);
			}
			// 保存合同盖章扫描件
			if (null != contract &&( "合同盖章".equals(subFile.getName())|| "zhang".equals(subFile.getName()))) {
				saveStampAttachment(subFile,contract);
			}
			// log未保存合同盖章扫描件
			if (null == contract &&( "合同盖章".equals(subFile.getName())|| "zhang".equals(subFile.getName()))) {
				logStampAttachment(subFile);
			}
			
		}

	

	}

	private static void logArchiveAttachment(File file) {
		// 二级目录下合同文件
		File[] subFiles = file.listFiles();
		for (File subFile : subFiles) {
			Logger.log("未保存合同附件文件 :"+subFile.getAbsolutePath()+";");
			contactFilePathSet.add(LoaclStringUtil.getBeforString(subFile.getAbsolutePath(), "\\\\"));
		}

	}

	/**
	 * log未保存合同盖章扫描件
	 * @param subFile
	 */
	private static void logStampAttachment(File file) {
		// 二级目录下合同文件
		File[] subFiles = file.listFiles();
		for (File subFile : subFiles) {
			Logger.log("未保存合同盖章文件 :"+subFile.getAbsolutePath()+";");
			contactFilePathSet.add(LoaclStringUtil.getBeforString(subFile.getAbsolutePath(),  "\\\\"));
		}

	}

	/**
	 * 保存合同盖章扫描件
	 * @param subFile
	 * @param contract
	 */
	private static void saveStampAttachment(File file, Contract contract) {
		// 二级目录下合同文件
		File[] subFiles = file.listFiles();
		int i=0;
		for (File subFile : subFiles) {
			i++;
			Integer isStampSubRefrence = 0;
			// 文件关联列为空,设置文件关联列
			if (null==contract.getStampSubRefrence()|| 0 == contract.getStampSubRefrence()) {
				Long longUUID = UUIDLong.longUUID();
				contract.setStampSubRefrence(longUUID);
				isStampSubRefrence = updateContractStampSubRefrence(contract);
			} else {
				isStampSubRefrence = 1;
			}
			// 文件关联列不为空,保存文件关联和文件信息,并保存文件
			if (1 == isStampSubRefrence) {
				saveStampSubRefrenceFile(contract, subFile);
			}
			Logger.log(subFile.getAbsolutePath());
		}
		Logger.log("合同盖章扫描件个数为:"+i+";");
		totalFile = totalFile +i;
		Logger.log("现在处理文件总数为:"+totalFile+";");
	}
	
	/**
	 * 保存文件关联和文件信息,并保存文件
	 * 
	 * @param contract
	 * @param subFile
	 */
	private static void saveStampSubRefrenceFile(Contract contract, File subFile) {
		Connection con = null;// 创建一个数据库连接
		PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement
		ResultSet result = null;// 创建一个结果集对象
		Integer i = 0;
		try {

			con = getConn();// 获取连接
			if(null == con){return;}
			con.setAutoCommit(false);

			long fileUrl = UUIDLong.longUUID();
			// 保存ctp_attachment
			i = saveStampCtpAttachment(con, pre, contract, fileUrl, subFile);
			if (1 != i) {
				con.rollback();
				return;
			}
			// 保存CTP_FILE
			i = saveCtpFile(con, pre, fileUrl, subFile);
			if (1 != i) {
				con.rollback();
				return;
			}
			// 修改文件名
			renameFile(subFile, fileUrl);
			con.commit();

		} catch (Exception e) {
			Logger.logStackTrace(e.getStackTrace());
			try {
				con.rollback();
			} catch (SQLException e1) {
				Logger.logStackTrace(e.getStackTrace());
			}
		} finally {
			DBUtil.finallyClose(result, pre, con);
		}

	}

	
	/**
	 * 保存附件信息
	 * 
	 * @param con
	 * @param pre2
	 * @param contract
	 * @param fileUrl
	 * @param subFile
	 * @return
	 * @throws SQLException
	 */
	private static int saveStampCtpAttachment(Connection con, PreparedStatement pre, Contract contract, long fileUrl,
			File subFile) throws SQLException {
		String sql = "INSERT INTO CTP_ATTACHMENT " + "(" + "ID, " + "REFERENCE," + " SUB_REFERENCE, " + "CATEGORY, "
				+ "TYPE, " + "FILENAME, " + "FILE_URL," + " MIME_TYPE," + " CREATEDATE, " + "ATTACHMENT_SIZE, SORT"
				+ ") " + "VALUES (" + "?, " + "?," + " ?, " + "'2'," + " '0', " + "?, " + "?, "
				+ "?," + " ?, ?, '3')";

		pre = con.prepareStatement(sql);
		pre.setLong(1, UUIDLong.longUUID());
		pre.setLong(2, contract.getId());
		pre.setLong(3, contract.getStampSubRefrence());
		pre.setString(4, subFile.getName());
		pre.setLong(5, fileUrl);
		pre.setString(6,  new MimetypesFileTypeMap().getContentType(subFile));
		pre.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
		pre.setLong(8, subFile.length());
		int i = pre.executeUpdate();
		return i;

	}
	
	
	/**
	 * 更新归档文件关联信息
	 * @param contract
	 * @return
	 */
	private static Integer updateContractStampSubRefrence(Contract contract) {
		Connection con = null;// 创建一个数据库连接
		PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement
		ResultSet result = null;// 创建一个结果集对象
		Integer i = 0;
		try {

			con = getConn();// 获取连接
			if(null == con){return null;}
			String sql = "update formmain_0924 set field0042 = ?  where ID =?";// 预编译语句，“？”代表参数
			pre = con.prepareStatement(sql);// 实例化预编译语句
			pre.setLong(1, contract.getStampSubRefrence());
			pre.setLong(2, contract.getId());
			i = pre.executeUpdate();

		} catch (Exception e) {
			Logger.logStackTrace(e.getStackTrace());
		} finally {
			DBUtil.finallyClose(result, pre, con);
		}
		return i;
	}

	/**
	 * 保存 合同归档扫描件
	 * 
	 * @param subFile
	 * @param contract
	 */
	private static void saveArchiveAttachment(File file, Contract contract) {
		// 二级目录下合同文件
		File[] subFiles = file.listFiles();
		int i = 0;
		for (File subFile : subFiles) {
			i++;
			Integer isArchiveSubRefrence = 0;
			// 文件关联列为空,设置文件关联列
			if (null==contract.getArchiveSubRefrence()|| 0 == contract.getArchiveSubRefrence()) {
				Long longUUID = UUIDLong.longUUID();
				contract.setArchiveSubRefrence(longUUID);
				isArchiveSubRefrence = updateContractArchiveSubRefrence(contract);
			} else {
				isArchiveSubRefrence = 1;
			}
			// 文件关联列不为空,保存文件关联和文件信息,并保存文件
			if (1 == isArchiveSubRefrence) {
				saveArchiveSubRefrenceFile(contract, subFile);
			}
			Logger.log(subFile.getAbsolutePath());
		}
		Logger.log("合同归档扫描件个数为:"+i+";");
		totalFile = totalFile+i;
		Logger.log("现在处理文件总数为:"+totalFile+";");

	}

	/**
	 * 更新归档文件关联信息
	 * @param contract
	 * @return
	 */
	private static Integer updateContractArchiveSubRefrence(Contract contract) {
		Connection con = null;// 创建一个数据库连接
		PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement
		ResultSet result = null;// 创建一个结果集对象
		Integer i = 0;
		try {

			con = getConn();// 获取连接
			String sql = "update formmain_0924 set field0029 = ?  where ID =?";// 预编译语句，“？”代表参数
			pre = con.prepareStatement(sql);// 实例化预编译语句
			pre.setLong(1, contract.getArchiveSubRefrence());
			pre.setLong(2, contract.getId());
			i = pre.executeUpdate();

		} catch (Exception e) {
			Logger.logStackTrace(e.getStackTrace());
		} finally {
			DBUtil.finallyClose(result, pre, con);
		}
		return i;
	}

	/**
	 * 保存文件关联和文件信息,并保存文件
	 * 
	 * @param contract
	 * @param subFile
	 */
	private static void saveArchiveSubRefrenceFile(Contract contract, File subFile) {
		Connection con = null;// 创建一个数据库连接
		PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement
		ResultSet result = null;// 创建一个结果集对象
		Integer i = 0;
		try {

			con = getConn();// 获取连接
			if(null == con){return;}
			con.setAutoCommit(false);

			long fileUrl = UUIDLong.longUUID();
			// 保存ctp_attachment
			i = saveCtpAttachment(con, pre, contract, fileUrl, subFile);
			if (1 != i) {
				con.rollback();
				return;
			}
			// 保存CTP_FILE
			i = saveCtpFile(con, pre, fileUrl, subFile);
			if (1 != i) {
				con.rollback();
				return;
			}
			// 修改文件名
			renameFile(subFile, fileUrl);
			con.commit();

		} catch (Exception e) {
			Logger.logStackTrace(e.getStackTrace());
			try {
				con.rollback();
			} catch (SQLException e1) {
				Logger.logStackTrace(e.getStackTrace());
				e1.printStackTrace();
			}
		} finally {
			DBUtil.finallyClose(result, pre, con);
		}

	}

	/**
	 * 修改文件名
	 * 
	 * @param subFile
	 * @param fileUrl
	 */
	private static void renameFile(File subFile, long fileUrl) {
//		File mm = new File( renameFilePath+"/"+fileUrl);
//		if (!subFile.renameTo(mm)) {
//			System.out.println("文件修改失败:"+subFile.getAbsolutePath());
//		}
	}

	/**
	 * 保存CTP_FILE
	 * 
	 * @param con
	 * @param pre
	 * @param fileUrl
	 * @param subFile
	 * @return
	 * @throws SQLException
	 */
	private static Integer saveCtpFile(Connection con, PreparedStatement pre, long fileUrl, File subFile)
			throws SQLException {

		String sql = "INSERT INTO CTP_FILE " + "(" + "ID," + " CATEGORY," + " TYPE," + " FILENAME," + " MIME_TYPE,"
				+ " CREATE_DATE, " + "CREATE_MEMBER, " + "FILE_SIZE, " + "UPDATE_DATE," + " ACCOUNT_ID) " + "VALUES ("
				+ "?," + " '2'," + " '0', " + "?, " + "?, " + "?," + " '1489815604318915427', "
				+ "?," + " ?," + " '-3956215810425142489') ";

		pre = con.prepareStatement(sql);
		pre.setLong(1, fileUrl);
		pre.setString(2, subFile.getName());
		pre.setString(3, new MimetypesFileTypeMap().getContentType(subFile));
		pre.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
		pre.setLong(5, subFile.length());
		pre.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
		int i = pre.executeUpdate();
		return i;

	}

	/**
	 * 保存附件信息
	 * 
	 * @param con
	 * @param pre2
	 * @param contract
	 * @param fileUrl
	 * @param subFile
	 * @return
	 * @throws SQLException
	 */
	private static int saveCtpAttachment(Connection con, PreparedStatement pre, Contract contract, long fileUrl,
			File subFile) throws SQLException {
		String sql = "INSERT INTO CTP_ATTACHMENT " + "(" + "ID, " + "REFERENCE," + " SUB_REFERENCE, " + "CATEGORY, "
				+ "TYPE, " + "FILENAME, " + "FILE_URL," + " MIME_TYPE," + " CREATEDATE, " + "ATTACHMENT_SIZE, SORT"
				+ ") " + "VALUES (" + "?, " + "?," + " ?, " + "'2'," + " '0', " + "?, " + "?, "
				+ "?," + " ?, ?, '3')";

		pre = con.prepareStatement(sql);
		pre.setLong(1, UUIDLong.longUUID());
		pre.setLong(2, contract.getId());
		pre.setLong(3, contract.getArchiveSubRefrence());
		pre.setString(4, subFile.getName());
		pre.setLong(5, fileUrl);
		pre.setString(6,  new MimetypesFileTypeMap().getContentType(subFile));
		pre.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
		pre.setLong(8, subFile.length());
		int i = pre.executeUpdate();
		return i;

	}

	

	/**
	 * 对应合同信息
	 * 
	 * @param file
	 * @return
	 */
	private static Contract getContract(File file) {
		Contract contract = new Contract();
		contract.setContractNo(getContractNo(file));
		contract.setName(getContractName(file));
		Connection con = null;// 创建一个数据库连接
		PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement
		ResultSet result = null;// 创建一个结果集对象
		try {

			con = getConn();// 获取连接
			if(null == con){return null;}
			// 合同归档扫描件: field0029
			// 合同盖章扫描件: field0042
//			String sql = "select ID,field0029,field0042  from formmain_0924 where field0006 = ? and field0001 = ?";
			String sql = "select ID,field0029,field0042  from formmain_0924 where field0043 = ? and field0001 = ?";
			pre = con.prepareStatement(sql);// 实例化预编译语句
			pre.setString(1, contract.getName());
			pre.setString(2, contract.getContractNo());
			result = pre.executeQuery();// 执行查询，注意括号中不需要再加参数
			// 如果查询结果为多条,打印错误
			int rowCount = 0;
			while (result.next()) {
				rowCount++;
				if (rowCount == 1) {
					contract = setContract(result);
				}
			}
			if (rowCount > 1) {
				Logger.log(file.getAbsolutePath()+"--匹配多条");
			}
			if (rowCount == 0) {
				Logger.log(file.getAbsolutePath()+"--匹配不到");
			}
			if (rowCount != 1) {
				return null;
			}
			Logger.log(file.getAbsolutePath()+"--匹配到");
		} catch (Exception e) {
			Logger.logStackTrace(e.getStackTrace());
		} finally {
			DBUtil.finallyClose(result, pre, con);
		}

		return contract;
	}

	/**
	 * 获得合同名称
	 * 
	 * @param subFile
	 * @return
	 */
	private static String getContractName(File subFile) {
		String str =  subFile.getName();
		return str.substring(str.indexOf("_")+1);
//		String[] subFileStrs = subFile.getName().split("_");
//		return subFileStrs[subFileStrs.length - 1];
	}

	/**
	 * 获得合同编号
	 * 
	 * @param subFile
	 * @return
	 */
	private static String getContractNo(File subFile) {
		String str =  subFile.getName();
		return str.substring(0, str.indexOf("_"));
		
//		String[] subFileStrs = subFile.getName().split("_");
//		return subFileStrs[subFileStrs.length - 2];
	}

	/**
	 * 从result中获得合同信息
	 * 
	 * @param result
	 * @param contract
	 * @return
	 * @throws SQLException
	 */
	private static Contract setContract(ResultSet result) throws SQLException {
		Contract contract = new Contract();
		contract.setId(result.getLong("ID"));
		// 归档
		contract.setArchiveSubRefrence(result.getLong("field0029"));
		// 盖章
		contract.setStampSubRefrence(result.getLong("field0042"));
		return contract;
	}

	private static Connection getConn() {
		try {
			return DBUtil.getConnection();
		} catch (SQLException e) {
			Logger.logStackTrace(e.getStackTrace());
			e.printStackTrace();
		}
		return null;

	}

}
