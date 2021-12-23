package org.apache.dubbo.demo.consumer;

import java.util.Map;

public interface HelloWordTestService1 {
    Map<String, Object> sayHello(long sleep);
}