package com.bankingsystem.bankingsystem.services;

import com.bankingsystem.bankingsystem.entity.Employee;

import java.util.List;

public interface EmployeeServiceImp {

    Employee registerEmployee(Employee employee);

    Employee getEmployeeById(Long employeeId);

    Employee getEmployeeByUserId(Long userId);

    List<Employee> getAllEmployees();

    Employee updateEmployee(Long employeeId, Employee updated);

    void deleteEmployee(Long employeeId);

    boolean existsByUserId(Long userId);
}