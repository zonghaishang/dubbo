package org.apache.dubbo.demo.consumer;

import java.util.Map;
import org.apache.dubbo.demo.HelloWordInterface;

public abstract class AbstractHelloWordTestServiceImpl {

    protected HelloWordInterface service;

    public abstract void setHelloWordService();

    public Map<String, Object> sayHello(long sleep) {
        setHelloWordService();
        return service.sayHello(sleep);
    }

}