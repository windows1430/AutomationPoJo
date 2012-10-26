package automation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import analytic.Propery;
import createpojo.CreatePojo;

public class Automation {
	
	private static Logger logger=Logger.getLogger(Automation.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Automation automation = new Automation();
		Map<String, String> map = new Propery().analytic("jdbc.properties");
		Map<String,String> datamap = automation.DB(map);
		new CreatePojo().createPOJO(datamap,map);
	}

	/**
	 * 
	 * @param map
	 *            存放数据库连接信息
	 * @return 返回用户创建的表信息与字段和类型
	 */
	public Map<String, String> DB(Map<String, String> map) {
		String driver = map.get("jdbc.driverClassName");
		String url = map.get("jdbc.url");
		String user = map.get("jdbc.username");
		String password = map.get("jdbc.password");
		String selectpackage = map.get("jdbc.selectPackage");
		Connection cn = null;
		Statement statement = null, statement2 = null;
		ResultSet tableResult = null, fieldResult = null;
		// 存放用户表表名对应的字段与类型
		Map<String, String> fieldMap = new HashMap<String, String>();
		try {
			Class.forName(driver);
			cn = DriverManager.getConnection(url, user, password);
			statement = cn.createStatement();
			statement2 = cn.createStatement();
			String sql = "", tablesql = "", filedsql = "";
			if (driver.contains("DB2")) {
				sql = "SELECT COUNT(*) FROM  syscat.tables   WHERE   tabschema= 'DB2ADMIN' AND TYPE NOT IN ('V')";
				tablesql = "SELECT TABNAME FROM   "
						+ "syscat.tables   WHERE   tabschema= 'DB2ADMIN' AND TYPE NOT IN ('V')";
				filedsql = "SELECT  TYPENAME ,COLNAME FROM SYSCAT.columns WHERE TABSCHEMA='DB2ADMIN' AND TABNAME=";
				if (selectpackage != null && !selectpackage.equals("")) {
					if (selectpackage.contains(",") || selectpackage.contains("|")) {
						int i=selectpackage.indexOf("|");
						String in="",str=selectpackage;
						if(i!=-1){
							str="";
							str=selectpackage.substring(0,i).toUpperCase();
						}
						for(String name:str.split(",")){
							in+="'"+name+"',";
						}
						sql += "AND TABNAME IN ("+in.substring(0,in.length()-1)+")";
						tablesql += "AND TABNAME IN ("+in.substring(0,in.length()-1)+")";
					}else{
						if((selectpackage.indexOf("%")!=-1)){
							sql += "AND TABNAME LIKE '" + selectpackage.toUpperCase()+ "'";
							tablesql += "AND TABNAME LIKE '"+ selectpackage.toUpperCase() + "'";		
						}else{
							sql += "AND TABNAME ='"+selectpackage.toUpperCase()+"'";
							tablesql += "AND TABNAME ='"+selectpackage.toUpperCase()+"'";
						}
					}
				}
			} else if (driver.contains("sqlserver")) {
				sql = "SELECT COUNT(*) FROM sysobjects WHERE xtype='U'";
				tablesql = "SELECT a.[name] FROM SysObjects a  "
						+ "LEFT JOIN Syscolumns c ON a.id=c.id "
						+ "LEFT JOIN Systypes t ON t.xusertype=c.xusertype WHERE  a.XType='U'";
				filedsql = "SELECT t.[name],c.[name] FROM SysObjects a  "
						+ "LEFT JOIN Syscolumns c ON a.id=c.id "
						+ "LEFT JOIN Systypes t ON t.xusertype=c.xusertype WHERE  a.XType='U' AND a.NAME=";
				if (selectpackage != null && !selectpackage.equals("")) {
					if (selectpackage.contains(",") || selectpackage.contains("|")) {
						int i=selectpackage.indexOf("|");
						String in="",str=selectpackage;
						if(i!=-1){
							str="";
							str=selectpackage.substring(0,i).toUpperCase();
						}
						for(String name:str.split(",")){
							in+="'"+name+"',";
						}
						sql += "AND NAME IN (" + in.substring(0,in.length()-1)+")";
						tablesql += "AND NAME IN ("+ in.substring(0,in.length()-1)+")";
					}else{
						if((selectpackage.indexOf("%")!=-1)){
							sql += "AND NAME LIKE '%" + selectpackage.toUpperCase()+"%'";
							tablesql += "AND NAME LIKE '%"+ selectpackage.toUpperCase() + "%'";
						}else{
							sql += "AND NAME ='" + selectpackage.toUpperCase()+"'";
							tablesql += "AND ='"+ selectpackage.toUpperCase() + "'";
						}
						
					}	
				}

			} else if (driver.contains("mysql")) {

			} else if (driver.contains("Oracle")) {
				sql = "SELECT COUNT(*) FROM  all_tables   WHERE owner = '"+user.toUpperCase()+"'";
				tablesql="select table_name from all_tables where owner ='"+user.toUpperCase()+"'";
				filedsql="select data_type,column_name from all_tab_columns where table_name=";
				if (selectpackage != null && !selectpackage.equals("")) {
					if (selectpackage.contains(",") || selectpackage.contains("|")) {
						int i=selectpackage.indexOf("|");
						String in="",str=selectpackage;
						if(i!=-1){
							str="";
							str=selectpackage.substring(0,i).toUpperCase();
						}
						for(String name:str.split(",")){
							in+="'"+name+"',";
						}
						sql+="AND table_name IN ( "+in.substring(0,in.length()-1)+")";
						tablesql+="AND table_name IN ("+in.substring(0,in.length()-1)+")"; 
					}else {
						if((selectpackage.indexOf("%")!=-1)){
							sql+="AND table_name like '%"+selectpackage.toUpperCase()+"%'";
							tablesql+="AND table_name like '%"+selectpackage.toUpperCase()+"%'"; 
						}
							sql+="AND table_name ='"+selectpackage.toUpperCase()+"'";
							tablesql+="AND table_name ='"+selectpackage.toUpperCase()+"'"; 
					}
				}	
			}
			System.out.println(sql);
			tableResult = statement.executeQuery(sql);
			tableResult.next();
			if (tableResult.getInt(1) > 0) {
				// 获取数据库中DB2ADMIN用户创建的表名
				System.out.println(tablesql);
				tableResult = statement.executeQuery(tablesql);
				while (tableResult.next()) {
					// 获取表名
					String tabname = tableResult.getString(1);
					String fieldANDtype = "";
					fieldResult = statement2.executeQuery(filedsql + "'"
							+ tabname + "'");
					tabname = tabname.substring(0, 1)
							+ tabname.substring(1, tabname.length())
									.toLowerCase();
					// 获取表中的字段与类型进行连接
					while (fieldResult.next()) {
						fieldANDtype = fieldANDtype
								+ fieldResult.getString(1).toUpperCase() + ","
								+ fieldResult.getString(2).toLowerCase() + ",";
					}
					fieldResult.close();
					fieldMap.put(tabname, fieldANDtype);
				}
			}

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(driver + "驱动不存在");
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				tableResult.close();
				statement.close();
				statement2.close();
				cn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Map<String, String> tablemap = new HashMap<String, String>();
		tablemap.putAll(fieldMap);
		return tablemap;
	}
}
