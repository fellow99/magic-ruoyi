package org.fellow99.magic.ruoyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动程序
 *
 * @author Lion Li
 */

@SpringBootApplication
@ComponentScan(basePackages = {
    "org.fellow99",
    "org.dromara"  // ← Add this
})
public class MagicRuoyiApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MagicRuoyiApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("ssdesk-admin启动成功");
    }

}
