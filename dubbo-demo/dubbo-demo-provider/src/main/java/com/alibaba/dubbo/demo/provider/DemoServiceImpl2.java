package com.alibaba.dubbo.demo.provider;

import com.alibaba.dubbo.demo.DemoService;

/**
 * @author yiji@apache.org
 */
public class DemoServiceImpl2 implements DemoService {

    @Override
    public String sayHello(String name) {
        return "Hello " + name + ", from unit hangzhou2";
    }
}
