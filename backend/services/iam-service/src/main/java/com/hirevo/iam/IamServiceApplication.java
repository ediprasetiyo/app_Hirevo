package com.hirevo.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.hirevo.iam",
    "com.hirevo.core",
    "com.hirevo.security",
    "com.hirevo.tenant",
    "com.hirevo.audit",
    "com.hirevo.messaging"
})
public class IamServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(IamServiceApplication.class, args);
  }
}
