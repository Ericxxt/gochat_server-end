package com.ericxxt.gochat;

import com.ericxxt.utils.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
//扫描mybatis mapper包的路径
@MapperScan(basePackages = "com.ericxxt.mapper")
//扫描 所需要的包的路径
@ComponentScan(basePackages = {"com.ericxxt","org.n3r.idworker"})
public class GochatApplication {

	//获取被spring管理的bean对象
	@Bean
	public SpringUtil springUtil(){
		return new SpringUtil();
	}
	public static void main(String[] args) {
		SpringApplication.run(GochatApplication.class, args);
	}


}
