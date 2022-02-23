package org.apache.dubbo.demo.consumer;

import com.alibaba.spring.context.OnceApplicationContextEventListener;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

public class AppStartedInvocation extends OnceApplicationContextEventListener
        implements ApplicationContextAware, Ordered {


    @Override
    public void onApplicationContextEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HelloWordTestService1 service1 =
                            (HelloWordTestService1) AppStartedInvocation.this.getApplicationContext()
                                    .getBean("consumer1");
                    while (true) {
                        try {
                            System.out.println("result: " + service1.sayHello(1));

                            Thread.sleep(5000);
                        } catch (Exception e) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}