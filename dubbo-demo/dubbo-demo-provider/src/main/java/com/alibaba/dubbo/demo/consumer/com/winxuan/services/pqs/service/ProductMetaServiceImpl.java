package com.alibaba.dubbo.demo.consumer.com.winxuan.services.pqs.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author yiji@apache.org
 */
public class ProductMetaServiceImpl implements ProductMetaService {
    public Map<BCode, Object> findBCodeValues(Long l, Set set) {
        System.out.println("do nothing.");

        Map<BCode, Object> result = new HashMap<BCode, Object>();
        result.put(BCode.B_BOOKING_DC, null);

        return result;
    }
}
