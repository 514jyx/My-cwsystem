package com.example.demo.common;


import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {

    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir")+"/demo";

        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/cwsystem?useSSL=false&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai",
                        "root",
                        "12345")
                // 全局配置
                .globalConfig(builder -> {
                    builder.author("jyxmn")
                            .outputDir(projectPath + "/src/main/java")
                            .disableOpenDir()
                            .dateType(DateType.ONLY_DATE)
                            .commentDate("yyyy-MM-dd")
                            .enableSwagger();
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent("com.example.demo")
                            .moduleName("system")
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .mapper("mapper")
                            .controller("controller")
                            .xml("mapper")
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    projectPath + "/src/main/resources/mapper"
                            ));
                })
                // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude("user", "accounts","suppliers","customers","products","orders","order_items","transactions","entries")
                            .addTablePrefix("tbl_", "t_")
                            .entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation()
                            .enableChainModel()
                            .versionColumnName("version")
                            .logicDeleteColumnName("deleted")
                            .controllerBuilder()
                            .enableRestStyle()
                            .enableHyphenStyle();
                })
                // 模板引擎
                .templateEngine(new FreemarkerTemplateEngine())
                // 执行
                .execute();
    }
}