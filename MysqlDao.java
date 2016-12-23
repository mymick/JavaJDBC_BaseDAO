/**
 * JAVA JDBC MYSQL数据库操作基础类
 * @author mick
 * @version 1.0.1
 */
package com.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MysqlDao implements Mdao
{
	Connection con;//数据库连接
	String tbname="";//表名
	String order="";//排序
	String field="";//读取的字段
	int limit=0;//limit长度
	int offset=0;//偏移长度 
	
	Map<String,Object> data;//要写入或者编辑的数据集
	Map<String,Object> map;//条件数据集
	
	/**
	 * 构造
	 */
	public MysqlDao()
	{
		
	}
	
	/**
	 * 初始化构造
	 * @param con
	 */
	public MysqlDao(Connection _con)
	{
		this.con=_con;
	}
	
	/**
	 * 设置要操作的表
	 * @param _tbname 表名
	 * @return
	 */
	public Mdao table(String _tbname)
	{
		this.tbname=_tbname;
		return this;
	}
	
	/**
	 * 设置要添加或者编辑的数据集
	 * @param _data 数据集
	 * @return
	 */
	public Mdao data(Map<String,Object> _data)
	{
		this.data=_data;
		return this;
	}
	
	/**
	 * 设置条件
	 * @param _map 条件集
	 * @return
	 */
	public Mdao where(Map<String,Object> _map)
	{
		this.map=_map;
		return this;
	}
	
	/**
	 * 设置要读取的字段,仅在select下有效
	 * @param _field 字段
	 * @return
	 */
	public Mdao field(String _field)
	{
		this.field=_field;
		return this;
	}
	
	public Mdao order(String _order)
	{
		this.order=_order;
		return this;
	}
	
	/**
	 * 设置读取的数量
	 * @param _limit 数量
	 * @return
	 */
	public Mdao limit(int _limit)
	{
		this.limit=_limit;
		return this;
	}
	
	/**
	 * 偏移量
	 * @param _offset 偏移量
	 * @return
	 */
	public Mdao offset(int _offset)
	{
		this.offset=_offset;
		return this;
	}
	
	/**
	 * 执行查找
	 * @return
	 * @throws SQLException 
	 */
	public Map<String,String> find() throws SQLException
	{
		if(this.tbname.equals(""))
		{
			return null;
		}
		
		String sql="SELECT "+this.fieldStr()+" FROM `"+this.tbname+"` "+this.whereStr();
		
		if(!this.order.equals(""))
		{
			sql+=" order by "+this.order;
		}
		
		sql+=" limit 1";
		
		PreparedStatement pst=this.bindParams(sql,this.data,this.map);
		
		List<Map<String,String>> data_list=this.resToMap(pst.executeQuery());
		
		if(data_list.size()>0)
		{
			this.reSet();
			return data_list.get(0);
		}
		else
		{
			this.reSet();
			return null;
		}
	}
	
	/**
	 * 通过ID查找数据
	 * @param table 表名
	 * @param id ID号
	 * @return 单数据集
	 * @throws SQLException 
	 */
	public Map<String,String> find(int id) throws SQLException
	{
		String sql="SELECT "+this.fieldStr()+" FROM `"+this.tbname+"` WHERE id="+id+" LIMIT 1";

		PreparedStatement pst=con.prepareStatement(sql);
		
		ResultSet res = pst.executeQuery();
		
		List<Map<String,String>> data_list=this.resToMap(res);
		
		if(data_list.size()>0)
		{
			this.reSet();
			return data_list.get(0);
		}
		else
		{
			this.reSet();
			return null;
		}
	}
	
	/**
	 * 执行读取
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String,String>> select() throws SQLException
	{
		if(this.tbname.equals(""))
		{
			return null;
		}
		
		String sql="SELECT "+this.fieldStr()+" FROM `"+this.tbname+"` "+this.whereStr();
		
		if(!this.order.equals(""))
		{
			sql+=" order by "+this.order;
		}
		
		if(this.limit>0)
		{
			sql+=" limit "+this.limit;
		}
		
		if(this.limit>0 && this.offset>0)
		{
			sql+=","+this.offset;
		}
		
		PreparedStatement pst=this.bindParams(sql,this.data,this.map);
		
		this.reSet();
		return this.resToMap(pst.executeQuery());
	}
	
	/**
	 * 查询数据
	 * @param sql 要查询的SQL语句
	 * @param map 参数数组
	 * @return 数据列表
	 * @throws SQLException 
	 */
	public List<Map<String,String>> select(String sql,Map<String,Object> map) throws SQLException
	{
		PreparedStatement pst=this.bindParams(sql,null,map);
		return this.resToMap(pst.executeQuery());
	}
	
	/**
	 * 执行写入
	 * @return
	 * @throws SQLException 
	 */
	public int add() throws SQLException
	{
		if(this.data==null || this.data.size()==0)
		{
			return 0;
		}
		
		if(this.tbname.equals(""))
		{
			return 0;
		}
		
		String sql="INSERT INTO `"+this.tbname+"` set "+this.dataStr();
		
		PreparedStatement pst=this.bindParams(sql,this.data,this.map);
		
		pst.executeUpdate();
		
		List<Map<String,String>> ms=this.resToMap(pst.getGeneratedKeys());
		
		this.reSet();
		return Integer.valueOf(ms.get(0).get("GENERATED_KEY"));
	}
	
	/**
	 * 批量添加--还未实现
	 * @param table	表名
	 * @param data_list 数据列表
	 * @return 成功的条数
	 */
	public int addAll(List<Map<String,Object>> data_list) throws SQLException
	{
		if(data_list==null || data_list.size()==0)
		{
			return 0;
		}
		
		this.data=data_list.get(0);
		
		if(this.tbname.equals(""))
		{
			return 0;
		}
		
		String sql="INSERT INTO `"+this.tbname+"` set "+this.dataStr();
		
		this.con.setAutoCommit(false);
		
		int total=0;
		for(int i=0;i<data_list.size();i++)
		{
			PreparedStatement pst=this.bindParams(sql,data_list.get(i),this.map);
		
			total+=pst.executeUpdate();
		}
		
		con.commit();
		this.con.setAutoCommit(true);
		
		this.reSet();
		return total;
	}

	/**
	 * 执行编辑
	 * @return
	 * @throws SQLException 
	 */
	public int save() throws SQLException
	{
		if(this.data==null || this.data.size()==0)
		{
			return 0;
		}
		
		if(this.tbname.equals(""))
		{
			return 0;
		}
		
		String sql="UPDATE `"+this.tbname+"` set "+this.dataStr()+" "+this.whereStr();
		
		PreparedStatement pst=this.bindParams(sql,this.data,this.map);
		
		this.reSet();
		return pst.executeUpdate();
	}

	/**
	 * 执行删除
	 * @return
	 * @throws SQLException 
	 */
	public int delete() throws SQLException
	{
		if(this.tbname.equals(""))
		{
			return 0;
		}
		
		String sql="DELETE FROM `"+this.tbname+"` "+this.whereStr();
		
		PreparedStatement pst=this.bindParams(sql,this.data,this.map);
		
		this.reSet();
		return pst.executeUpdate();
	}
	
	/**
	 * 执行统计
	 * @return
	 * @throws SQLException 
	 */
	public int count() throws SQLException
	{	
		if(this.tbname.equals(""))
		{
			return 0;
		}
		
		String sql="SELECT count(*) as c FROM `"+this.tbname+"` "+this.whereStr();
		
		PreparedStatement pst=this.bindParams(sql,this.data,this.map);
		
		List<Map<String,String>> data_list=this.resToMap(pst.executeQuery());
		
		if(data_list.size()>0)
		{
			this.reSet();
			return Integer.valueOf(data_list.get(0).get("c"));
		}
		else
		{
			this.reSet();
			return 0;
		}
	}
	
	/**
	 * 执行sql指令
	 * @param sql sql语句
	 * @param map 参数数组
	 * @return 影响的条数
	 * @throws SQLException 
	 */
	public int execut(String sql,Map<String,Object> map) throws SQLException
	{
		PreparedStatement pst=this.bindParams(sql,null,map);
		
		this.reSet();
		return pst.executeUpdate();
	}
	
	/**
	 * 重置所有条件
	 */
	public void reSet()
	{
		this.tbname="";
		this.order="";
		this.order="";
		this.field="";
		this.limit=0;
		this.offset=0;
		this.data=null;
		this.map=null;
	}
	
	/**
	 * 参数字符生成
	 * @return
	 */
	private String dataStr()
	{
		String str="";
		
		Iterator iter = this.data.entrySet().iterator();
        while (iter.hasNext())
        {
	        Entry<String,Object> e = (Entry) iter.next();
	        String key = e.getKey();
	        
	        str+="`"+key+"`=:d_"+key+",";
        }
		
        str=str.substring(0, str.length()-1);
        
		return str;
	}
	
	/**
	 * 字段字符生成
	 * @return
	 */
	private String fieldStr()
	{
		if(this.field==null || this.field.equals(""))
		{
			return "*";
		}
		else
		{
			return this.field;
		}
	}
	
	/**
	 * 条件字符生成
	 * @return
	 */
	private String whereStr()
	{
		if(map==null || map.size()==0)
		{
			return "";
		}
		
		String str=" where 1=1 ";
		
		Iterator iter = this.map.entrySet().iterator();
        while (iter.hasNext())
        {
	        Entry<String,Object> e = (Entry) iter.next();
	        String key = e.getKey();
	        
	        str+=" and `"+key+"`=:w_"+key+" ";
        }
	
		return str;
	}
	
	/**
	 * 参数绑定和sql格式化
	 * @param sql 要执行的sql
	 * @return 
	 * @throws SQLException
	 */
	private PreparedStatement bindParams(String sql,Map<String,Object> data,Map<String,Object> map) throws SQLException
	{
		List<Object> params=new ArrayList<Object>();
		
		String regex = ":d_\\w+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(sql);

        while (m.find())
        {
        	String p_name=m.group(0).replace(":d_","");
        	params.add(data.get(p_name));
        }
        sql= sql.replaceAll(regex, "?");
        
        regex = ":w_\\w+";
        p = Pattern.compile(regex);
        m = p.matcher(sql);
        while (m.find())
        {
        	String p_name=m.group(0).replace(":w_","");
        	params.add(map.get(p_name));
        }
        sql= sql.replaceAll(regex, "?");
        
        regex = ":\\w+";
        p = Pattern.compile(regex);
        m = p.matcher(sql);
        while (m.find())
        {
        	String p_name=m.group(0).replace(":","");
        	params.add(map.get(p_name));
        }
        sql= sql.replaceAll(regex, "?");
        
        PreparedStatement pst=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        
        for(int i=0;i<params.size();i++)
        {
        	pst.setObject(i+1, params.get(i));
        }
        
        return pst;
	}
	
	/**
	 * 读取RS结果集的内容组成map列表
	 * @param res
	 * @return
	 * @throws SQLException 
	 */
 	private List<Map<String,String>> resToMap(ResultSet res) throws SQLException
	{
		List<Map<String,String>> temp_list=new ArrayList<Map<String,String>>();
		
		while(res.next())
		{
			Map<String, String> temp = new HashMap<String, String>();
	        ResultSetMetaData rsmd = res.getMetaData();
	        int count = rsmd.getColumnCount();
	        for (int i = 1; i <= count; i++)
	        {
	            String key = rsmd.getColumnLabel(i);
	            String value = res.getString(i);
	            temp.put(key, value);
	        }
	        
	        temp_list.add(temp);
		}
		res.close();
		return temp_list;
	}
}
