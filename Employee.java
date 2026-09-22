package com.example.leavemgmt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "employees")
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String employeeCode;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    private String department;
    private String designation;

    // Leave balances (days)
    private int casualLeave = 12;
    private int sickLeave = 10;
    private int earnedLeave = 15;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "VARCHAR(20)")
    private Role role = Role.EMPLOYEE;

    public enum Role {
        EMPLOYEE, MANAGER, ADMIN
    }
}
