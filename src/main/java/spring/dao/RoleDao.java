package spring.dao;

import spring.model.Role;

import java.sql.SQLException;
import java.util.List;

public interface RoleDao {
    void insertRole(Role role);

    List<Role> getAllRole();

    void updateRole(Role role);

    void deleteRole(int roleId) throws SQLException;

    Role getRoleById(int roleId) throws SQLException;
}
