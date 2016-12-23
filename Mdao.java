/**
 * JAVA JDBC 数据库操作基础类
 * @author mick
 * @version 1.0.1
 */
package com.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface Mdao
{
	/**
	 * 设置要操作的表
	 * @param _tbname 表名
	 * @return
	 */
	public Mdao table(String _tbname);
	
	/**
	 * 设置要添加或者编辑的数据集
	 * @param _data 数据集
	 * @return
	 */
	public Mdao data(Map<String,Object> _data);
	
	/**
	 * 设置条件
	 * @param _map 条件集
	 * @return
	 */
	public Mdao where(Map<String,Object> _map);
	
	/**
	 * 设置要读取的字段,仅在select下有效
	 * @param _field 字段
	 * @return
	 */
	public Mdao field(String _field);
	
	public Mdao order(String _order);
	
	/**
	 * 设置读取的数量
	 * @param _limit 数量
	 * @return
	 */
	public Mdao limit(int _limit);
	
	/**
	 * 偏移量
	 * @param _offset 偏移量
	 * @return
	 */
	public Mdao offset(int _offset);
	/**
	 * 执行查找
	 * @return
	 * @throws SQLException 
	 */
	public Map<String,String> find() throws SQLException;
	
	/**
	 * 通过ID查找数据
	 * @param table 表名
	 * @param id ID号
	 * @return 单数据集
	 * @throws SQLException 
	 */
	public Map<String,String> find(int id) throws SQLException;
	
	/**
	 * 执行读取
	 * @return
	 * @throws SQLException 
	 */
	public List<Map<String,String>> select() throws SQLException;
	/**
	 * 查询数据
	 * @param sql 要查询的SQL语句
	 * @param map 参数数组
	 * @return 数据列表
	 * @throws SQLException 
	 */
	public List<Map<String,String>> select(String sql,Map<String,Object> map) throws SQLException;
	
	/**
	 * 执行写入
	 * @return
	 * @throws SQLException 
	 */
	public int add() throws SQLException;
	
	/**
	 * 批量添加
	 * @param table	表名
	 * @param data_list 数据列表
	 * @return 成功的条数
	 */
	public int addAll(List<Map<String,Object>> data_list) throws SQLException;

	/**
	 * 执行编辑
	 * @return
	 * @throws SQLException 
	 */
	public int save() throws SQLException;
	/**
	 * 执行删除
	 * @return
	 * @throws SQLException 
	 */
	public int delete() throws SQLException;
	
	/**
	 * 执行统计
	 * @return
	 * @throws SQLException 
	 */
	public int count() throws SQLException;
	
	/**
	 * 执行sql指令
	 * @param sql sql语句
	 * @param map 参数数组
	 * @return 影响的条数
	 * @throws SQLException 
	 */
	public int execut(String sql,Map<String,Object> map) throws SQLException;
	
	/**
	 * 重置条件
	 */
	public void reSet();
	
}
