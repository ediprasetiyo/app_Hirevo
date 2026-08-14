package com.hirevo.reimbursement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.hirevo.reimbursement",
    "com.hirevo.core",
    "com.hirevo.security",
    "com.hirevo.tenant",
    "com.hirevo.audit",
    "com.hirevo.messaging"
})
public class ReimbursementServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ReimbursementServiceApplication.class, args);
  }
}
