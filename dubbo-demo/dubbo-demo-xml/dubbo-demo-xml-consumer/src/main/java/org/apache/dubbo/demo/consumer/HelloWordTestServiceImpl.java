package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.demo.HelloWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class HelloWordTestServiceImpl extends AbstractHelloWordTestServiceImpl implements HelloWordTestService1 {

    @Autowired
    @Qualifier("helloWordService")
    protected HelloWordService service;


    public void setHelloWordService(HelloWordService helloWordService) {
        this.service = helloWordService;
    }

    @Override
    public void setHelloWordService() {
        super.service = this.service;
    }
}