//package org.apache.dubbo.demo.consumer;
//
//import java.util.Map;
//import org.apache.dubbo.config.annotation.Reference;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class HelloWorldController2 {
//
////    @Reference()
//    @Autowired
//    private HelloWordTestService1 consumer1;
//
//
//    @RequestMapping("")
//    public void invokeLoop() throws InterruptedException {
//
//        while (true) {
//            try {
//                Map<String, Object> hello = consumer1.sayHello(1);
//                System.out.println("result: " + hello);
//
//                Thread.sleep(3000);
//            } catch (Exception e) {
//                Thread.sleep(3000);
//            }
//        }
//
//    }
//
//}