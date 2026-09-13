package com.jh.bigevent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jh.bigevent.mapper")
public class BigEventApplication {

	public static void main(String[] args) {
		SpringApplication.run(BigEventApplication.class, args);
	}

}
