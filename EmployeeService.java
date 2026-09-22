package com.example.leavemgmt.service;

import com.example.leavemgmt.dto.EmployeeDTO;
import com.example.leavemgmt.entity.Employee;
import com.example.leavemgmt.exception.BadRequestException;
import com.example.leavemgmt.exception.ResourceNotFoundException;
import com.example.leavemgmt.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public EmployeeDTO.Response register(EmployeeDTO.RegisterRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPassword(hash(request.getPassword()));
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setRole(request.getRole() == Employee.Role.MANAGER
            ? Employee.Role.MANAGER : Employee.Role.EMPLOYEE);
        employee.setEmployeeCode("EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        return EmployeeDTO.Response.from(employeeRepository.save(employee));
    }

    public EmployeeDTO.Response login(EmployeeDTO.LoginRequest request) {
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!employee.getPassword().equals(hash(request.getPassword()))) {
            throw new BadRequestException("Invalid email or password");
        }
        return EmployeeDTO.Response.from(employee);
    }

    public List<EmployeeDTO.Response> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeDTO.Response::from)
                .collect(Collectors.toList());
    }

    public EmployeeDTO.Response getById(Long id) {
        return EmployeeDTO.Response.from(findById(id));
    }

    public EmployeeDTO.Response update(Long id, EmployeeDTO.UpdateRequest request) {
        Employee employee = findById(id);
        if (request.getName() != null) employee.setName(request.getName());
        if (request.getDepartment() != null) employee.setDepartment(request.getDepartment());
        if (request.getDesignation() != null) employee.setDesignation(request.getDesignation());
        return EmployeeDTO.Response.from(employeeRepository.save(employee));
    }

    public void delete(Long id) {
        findById(id);
        employeeRepository.deleteById(id);
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(password.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed");
        }
    }
}
