/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("spring/dubbo-consumer.xml")
//@ComponentScan("org.apache.dubbo.demo.consumer")
public class Application {
    /**
     * In order to make sure multicast registry works, need to specify '-Djava.net.preferIPv4Stack=true' before
     * launch the application
     */
    public static void main(String[] args) throws Exception {
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/dubbo-consumer.xml");
//        context.start();
        SpringApplication.run(Application.class, args);
//        DemoService demoService = context.getBean("demoService", DemoService.class);
//        GreetingService greetingService = context.getBean("greetingService", GreetingService.class);
//
//        new Thread(() -> {
//            while (true) {
//                String greetings = greetingService.hello();
//                System.out.println(greetings + " from separated thread.");
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

//        while (true) {
//            try{
//                String hello = demoService.sayHello("world");
//                System.out.println("result: " + hello);
//
////            String greetings = greetingService.hello();
////            System.out.println("result: " + greetings);
//
//                Thread.sleep(3000);
//            }catch (Exception e){
//                Thread.sleep(3000);
//            }
//        }
    }
}
