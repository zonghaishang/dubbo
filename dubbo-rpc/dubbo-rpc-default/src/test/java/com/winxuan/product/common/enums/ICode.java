package com.winxuan.product.common.enums;

/**
 * 元数据接口
 * @author Li Pengcheng
 * @version 1.0
 */
public interface ICode {

    /**
     * code 编码
     *
     * @return
     */
    String getCode();

    /**
     * 中文名
     *
     * @return
     */
    String getName();

    /**
     * 对应类型
     * @return
     */
    Class getClazz();

}


