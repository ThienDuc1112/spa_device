package com.company.pda.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.company.pda.infrastructure.persistence.mybatis.mapper")
public class MyBatisConfig {}
