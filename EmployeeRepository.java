package com.example.leavemgmt.repository;

import com.example.leavemgmt.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);
    long countByRole(Employee.Role role);
}
