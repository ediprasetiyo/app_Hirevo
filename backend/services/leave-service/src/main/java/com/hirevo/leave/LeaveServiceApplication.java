package com.hirevo.leave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.hirevo.leave",
    "com.hirevo.core",
    "com.hirevo.security",
    "com.hirevo.tenant",
    "com.hirevo.audit",
    "com.hirevo.messaging"
})
public class LeaveServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(LeaveServiceApplication.class, args);
  }
}
