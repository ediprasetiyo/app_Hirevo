package com.hirevo.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.hirevo.payroll",
    "com.hirevo.core",
    "com.hirevo.security",
    "com.hirevo.tenant",
    "com.hirevo.audit",
    "com.hirevo.messaging"
})
public class PayrollServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(PayrollServiceApplication.class, args);
  }
}
