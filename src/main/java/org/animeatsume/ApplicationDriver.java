package org.animeatsume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * SpringBootApplication is a combo of the following:
 *  Configuration - Java component configuration with beans using vanilla Spring framework, e.g. `@Bean` or `@Controller`.
 *  ComponentScan - Specifies the packages to scan for @Configuration to recognize; searches current package
 *      and all subpackages, so best to put this main driver file in module root classpath.
 *      This handles the bulk of the work required for @Autowired annotations to function properly.
 *  EnableAutoConfiguration - Allows SpringBoot to create many beans and configure the application automatically
 *      based on the resolved classpath; also allows for selective include/exclude/etc. fields.
 *      This handles the bulk of the work required for functionality required by specific classes (e.g. @Repository,
 *      JpaRepository interfaces, @DataSource, factories, etc.) so you don't have to manually create a beans.xml file
 *      for each of these specific classes.
 */
// Remove 'exclude' once database is added
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class ApplicationDriver {
    private static final Logger log = LoggerFactory.getLogger(ApplicationDriver.class);

    public static void main(String[] args) {
        SpringApplication.run(ApplicationDriver.class, args);
    }
}
