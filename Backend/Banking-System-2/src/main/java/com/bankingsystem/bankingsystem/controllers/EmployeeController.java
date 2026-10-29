package com.bankingsystem.bankingsystem.controllers;

import com.bankingsystem.bankingsystem.entity.Employee;
import com.bankingsystem.bankingsystem.services.EmployeeServiceImp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeServiceImp employeeService;

    public EmployeeController(EmployeeServiceImp employeeService) {
        this.employeeService = employeeService;
    }

    // -------------------- Register --------------------

    @PostMapping("/register")
    public ResponseEntity<Employee> registerEmployee(
            @RequestBody Employee employee) {
        return ResponseEntity.ok(
                employeeService.registerEmployee(employee));
    }

    // -------------------- Get --------------------

    @GetMapping("/{employeeId}")
    public ResponseEntity<Employee> getEmployeeById(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                employeeService.getEmployeeById(employeeId));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Employee> getEmployeeByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                employeeService.getEmployeeByUserId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(
                employeeService.getAllEmployees());
    }

    // -------------------- Update --------------------

    @PutMapping("/update/{employeeId}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long employeeId,
            @RequestBody Employee employee) {
        return ResponseEntity.ok(
                employeeService.updateEmployee(employeeId, employee));
    }

    // -------------------- Delete --------------------

    @DeleteMapping("/delete/{employeeId}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable Long employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok(
                "Employee deleted successfully");
    }

    // -------------------- Exists --------------------

    @GetMapping("/exists/user")
    public ResponseEntity<Boolean> existsByUserId(
            @RequestParam Long userId) {
        return ResponseEntity.ok(
                employeeService.existsByUserId(userId));
    }
}