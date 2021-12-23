package org.apache.dubbo.demo;

import java.util.HashMap;
import java.util.Map;

public class HelloWordServerServiceImpl implements HelloWordService {

    public Map<String, Object> sayHello(long sleep) {
        Map<String, Object> aa = new HashMap<>();
        aa.put("77", "88");
        return aa;
    }

}