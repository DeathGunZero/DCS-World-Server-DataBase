package cn.deathgun.dcsluasocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableAsync
@EnableSwagger2
//@MapperScan(basePackages = {"cn.deathgun.dcsluasocket.dao"}, annotationClass = Mapper.class)
@SpringBootApplication
public class DcsluasocketApplication{ // implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DcsluasocketApplication.class, args);
    }
}
