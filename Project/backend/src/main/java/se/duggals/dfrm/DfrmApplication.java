package se.duggals.dfrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Huvudklass för DFRM (Duggals Fastigheter Management System)
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@SpringBootApplication
@EnableScheduling
public class DfrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(DfrmApplication.class, args);
    }
} 