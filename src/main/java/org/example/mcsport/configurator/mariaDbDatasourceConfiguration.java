package org.example.mcsport.configurator;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "mariaDbEntityManagerFactory",
        transactionManagerRef = "mariaDbTransactionManager",
        basePackages = {"org.example.mcsport.repository.mariadb"})
public class mariaDbDatasourceConfiguration {
    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.mariadb")
    public DataSource mariaDbDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "mariaDbEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mariaDbEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(mariaDbDataSource())
                .packages("org.example.mcsport.entity.mariadb")
                .persistenceUnit("mariadb")
                .properties(Map.of(
                        "hibernate.dialect", "org.hibernate.dialect.MariaDBDialect",
                        "hibernate.hbm2ddl.auto", "update"
                ))
                .build();
    }

    @Primary
    @Bean(name = "mariaDbTransactionManager")
    public PlatformTransactionManager mariaDbTransactionManager(
            @Qualifier("mariaDbEntityManagerFactory")
            EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
