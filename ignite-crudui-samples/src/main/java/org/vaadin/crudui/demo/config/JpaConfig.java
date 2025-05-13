package org.vaadin.crudui.demo.config;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextRefreshedEvent;

import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.vaadin.crudui.demo.entity.Group;
import org.vaadin.crudui.demo.entity.MaritalStatus;
import org.vaadin.crudui.demo.entity.Technology;
import org.vaadin.crudui.demo.entity.User;
import org.vaadin.crudui.demo.service.GroupService;
import org.vaadin.crudui.demo.service.TechnologyService;
import org.vaadin.crudui.demo.service.UserService;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Configuration
@PropertySource("classpath:application.properties")
@EnableJpaRepositories(basePackages = "org.vaadin.crudui.demo.repository")
@EnableTransactionManagement
public class JpaConfig {
    private static Logger log = LoggerFactory.getLogger(JpaConfig.class);

    public static final int DEMO_USERS_COUNT = UserService.USERS_COUNT_LIMIT / 2;


    // 配置数据源
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:/tmp/yourdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    // 配置JPA实体管理器工厂
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("org.vaadin.crudui.demo.entity");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    // 配置事务管理器
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    // Hibernate特定属性
    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        return properties;
    }

    @Bean
    public ApplicationListener<ContextRefreshedEvent> initDatabase(GroupService groupService, UserService userService,
                                                                   TechnologyService technologyService) {
        return event -> {
            if (groupService.count() == 0) {
                createDemoData(groupService, userService, technologyService);
            }
        };
    }

    private void createDemoData(GroupService groupService, UserService userService,
                                TechnologyService technologyService) {
        log.info("Creating demo data...");

        Stream.of("Services,IT,HR,Management,Marketing,Sales,Operations,Finance".split(","))
                .map(Group::new)
                .forEach(groupService::save);

        List<Group> allGroups = groupService.findAll();

        groupService.findAll();

        String[] firstNames = "Maria,Edgar,Juan,Angelica,Nicole,Brenda,Clare,Cathy,Elizabeth,Tom,John,Daniel,Edward,Hank,Arthur,Bill,Alejandro"
                .split(",");
        String[] lastNames = "Smith,Duarte,Avendano,Vento,Johnson,Williams,Jones,Brown,Miller,Wilson,Wright,Thompson,Lee"
                .split(",");

        Random rand = new Random();

        IntStream.rangeClosed(1, DEMO_USERS_COUNT)
                .mapToObj(i -> {
                    String name = firstNames[rand.nextInt(firstNames.length)] + " "
                            + lastNames[rand.nextInt(lastNames.length)];
                    ArrayList<Group> groups = IntStream.rangeClosed(1, 1 + rand.nextInt(2))
                            .mapToObj(j -> allGroups.get(rand.nextInt(allGroups.size())))
                            .collect(Collectors.toCollection(ArrayList::new));

                    return new User(
                            name,
                            new java.sql.Date(rand.nextInt(365 * 99)),
                            rand.nextInt(9000000) + 1000000,
                            name.replace(" ", "").toLowerCase() + i + "@test.com",
                            BigDecimal.valueOf(5000),
                            UUID.randomUUID().toString(),
                            rand.nextInt(10) > 0,
                            groups.get(rand.nextInt(groups.size())),
                            new HashSet<>(groups),
                            MaritalStatus.values()[rand.nextInt(MaritalStatus.values().length)]);
                })
                .forEach(userService::save);

        String[] parentTechs = new String[] { "Java", "Javascript", "Databases" };
        String[][] childrenTechs = new String[][] {
                { "Vaadin", "Spring", "Quarkus" },
                { "Hilla", "React", "Svelte" },
                { "MariaDB", "MySQL", "Postgres" }
        };

        for (int i = 0; i < parentTechs.length; i++) {
            Technology parentTech = technologyService
                    .save(new Technology(parentTechs[i], null, parentTechs[i], null, null));
            for (int j = 0; j < childrenTechs[i].length; j++) {
                var technology = new Technology(childrenTechs[i][j], rand.nextDouble() * 10, childrenTechs[i][j],
                        LocalDateTime.now().minusHours(rand.nextInt(24 * 60)), parentTech);
                technologyService.save(technology);
            }
        }

        log.info("Demo data created.");
    }

}
