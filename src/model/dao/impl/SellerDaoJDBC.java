package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.cj.xdevapi.Result;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao{

	private Connection conn;
	
	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public void insert(Seller obj) {
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(
					"INSERT INTO seller "
					+ "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
					+ "Values "
					+ "(?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			
			stmt.setString(1, obj.getName());
			stmt.setString(2, obj.getEmail());
			stmt.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			stmt.setDouble(4, obj.getBaseSalary());
			stmt.setInt(5, obj.getDepartment().getId());
			
					
			int rowsAffected = stmt.executeUpdate();
			if(rowsAffected > 0) {
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next()) {
					int id = rs.getInt(1);
					obj.setId(id);
				}
				DB.closeResultSet(rs);
			}
			else {
				throw new DbException("Unexpected error! No Rows Affected!");
			}
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(stmt);
		}
		
		
	}

	@Override
	public void update(Seller obj) {
		PreparedStatement stmt = null;
				
				try {
					stmt = conn.prepareStatement(
							"UPDATE seller "
							+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary= ?, DepartmentId = ? "
							+ "WHERE Id = ? ");
					
					stmt.setString(1, obj.getName());
					stmt.setString(2, obj.getEmail());
					stmt.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
					stmt.setDouble(4, obj.getBaseSalary());
					stmt.setInt(5, obj.getDepartment().getId());
					stmt.setInt(6, obj.getId());
					
					stmt.executeUpdate();
					
				}
				catch(SQLException e) {
					throw new DbException(e.getMessage());
				}
				finally {
					DB.closeStatement(stmt);
				}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement stmt = null;
		
		try {
			stmt = conn.prepareStatement(
				"DELETE FROM seller "
				+ "WHERE Id = ? ");
			
			stmt.setInt(1, id);
			
			stmt.executeUpdate();
			
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(stmt);
		}
		
	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "
					+ "WHERE seller.Id = ? ");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if(rs.next()) {
				Department dep = instantiateDepartment(rs);
				
				Seller obj = instantiateSeller(dep, rs);
				return obj;
			}
			return null;
					
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(stmt);
			DB.closeResultSet(rs);
		}
	}

	private Seller instantiateSeller(Department dep, ResultSet rs) throws SQLException {
		Seller obj = new Seller();
		obj.setId(rs.getInt("Id"));
		obj.setName(rs.getString("Name"));
		obj.setEmail(rs.getString("Email"));
		obj.setBaseSalary(rs.getDouble("BaseSalary"));
		obj.setBirthDate(rs.getDate("BirthDate"));
		obj.setDepartment(dep);
		return obj;
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department dep = new Department();
		dep.setId(rs.getInt("DepartmentId"));
		dep.setName(rs.getString("DepName"));
		return dep;
	}

	@Override
	public List<Seller> findAll() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "
					+ "ORDER BY Name ");
			
			rs = stmt.executeQuery();
			
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			while(rs.next()) {
				Department dep = map.get(rs.getInt("DepartmentId"));
				
				if(dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				Seller obj = instantiateSeller(dep, rs);
				list.add(obj);
			}
			return list;
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeResultSet(rs);
			DB.closeStatement(stmt);
		}
	}

	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(
					"SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "On seller.DepartmentId = department.Id "
					+ "WHERE DepartmentId = ? "
					+ "ORDER BY Name ");
			
			stmt.setInt(1, department.getId());
			
			rs = stmt.executeQuery();
			
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			while(rs.next()) {
				Department dep = map.get(rs.getInt("DepartmentId"));
				
				if(dep == null) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				Seller obj = instantiateSeller(dep, rs);
				list.add(obj);
			}
			return list;
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(stmt);
			DB.closeResultSet(rs);
		}
	}

}
