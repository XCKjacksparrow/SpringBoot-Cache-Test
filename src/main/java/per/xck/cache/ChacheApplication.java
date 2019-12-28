package per.xck.cache;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@MapperScan("per.xck.cache.mapper")
@EnableCaching
public class ChacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChacheApplication.class, args);
    }

}
