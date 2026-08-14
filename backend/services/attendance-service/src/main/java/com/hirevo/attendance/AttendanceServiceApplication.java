package com.hirevo.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.hirevo.attendance",
    "com.hirevo.core",
    "com.hirevo.security",
    "com.hirevo.tenant",
    "com.hirevo.audit",
    "com.hirevo.messaging"
})
public class AttendanceServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(AttendanceServiceApplication.class, args);
  }
}
