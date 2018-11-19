package com.alibaba.dubbo.demo.consumer.com.winxuan.services.pqs.service;

import java.util.Map;
import java.util.Set;

/**
 * @author yiji@apache.org
 */
public interface ProductMetaService {

    Map<BCode, Object> findBCodeValues(Long l, Set set);

}
