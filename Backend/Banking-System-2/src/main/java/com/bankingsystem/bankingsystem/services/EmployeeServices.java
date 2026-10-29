

package com.bankingsystem.bankingsystem.services;

import com.bankingsystem.bankingsystem.entity.Employee;
import com.bankingsystem.bankingsystem.exceptions.ResourceNotFoundException;
import com.bankingsystem.bankingsystem.repositories.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServices implements EmployeeServiceImp {

    private static final String EMPLOYEE_NOT_FOUND = "Employee not found with id: ";


    private final EmployeeRepository employeeRepository;

    public EmployeeServices(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // -------------------- Employee Registration --------------------

    @Override
    @Transactional
    public Employee registerEmployee(Employee employee) {

        if (employee.getUser() == null ||
                employee.getUser().getUserid() == null) {
            throw new IllegalArgumentException("userId is required");
        }


        if (employeeRepository.existsByUser_Userid(
                employee.getUser().getUserid())) {
            throw new IllegalArgumentException(
                    "Employee already exists for this userId");
        }

        return employeeRepository.save(employee);
    }

    // -------------------- Fetch --------------------

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                EMPLOYEE_NOT_FOUND+ employeeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Employee getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUser_Userid(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(EMPLOYEE_NOT_FOUND + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // -------------------- Update --------------------

    @Override
    @Transactional
    public Employee updateEmployee(Long employeeId, Employee updated) {

        Employee existing = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(EMPLOYEE_NOT_FOUND+ employeeId)
                );

        if (updated.getFullname() != null) {
            existing.setFullname(updated.getFullname());
        }
        if (updated.getDesignation() != null) {
            existing.setDesignation(updated.getDesignation());
        }
        if (updated.getBranch() != null) {
            existing.setBranch(updated.getBranch());
        }

        return employeeRepository.save(existing);
    }


    // -------------------- Delete --------------------

    @Override
    @Transactional
    public void deleteEmployee(Long employeeId) {

        Employee existing = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(EMPLOYEE_NOT_FOUND+ employeeId)
                );

        employeeRepository.delete(existing);
    }

    // -------------------- Exists --------------------

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        return employeeRepository.existsByUser_Userid(userId);
    }
}