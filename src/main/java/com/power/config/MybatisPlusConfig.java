package com.power.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * MybatisPlus配置
 */
@Configuration
//@MapperScan("com.power.mapper")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

//    @Bean
//    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
//        String typeAliasesPackage = env.getProperty("mybatis-plus.typeAliasesPackage");
//        String mapperLocations = env.getProperty("mybatis-plus.mapperLocations");
//        String configLocation = env.getProperty("mybatis-plus.configLocation");
//        typeAliasesPackage = setTypeAliasesPackage(typeAliasesPackage);
//        VFS.addImplClass(SpringBootVFS.class);
//
//        final MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
//        sessionFactory.setDataSource(dataSource);
//        sessionFactory.setTypeAliasesPackage(typeAliasesPackage);
//        sessionFactory.setMapperLocations(resolveMapperLocations(StringUtils.split(mapperLocations, ",")));
//        sessionFactory.setConfigLocation(new DefaultResourceLoader().getResource(configLocation));
//        return sessionFactory.getObject();
//    }
}
