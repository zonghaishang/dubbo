/**
 * Alipay.com Inc. Copyright (c) 2004-2019 All Rights Reserved.
 */
package org.apache.dubbo.common.serialize.hessian2;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author yiji
 * @version : TestMain.java, v 0.1 2019年07月24日 11:28 yiji Exp $
 */
public class TestMain {

    public static void main(String[] args){
        Map<Boolean, List<String>> map = new HashMap<Boolean, List<String>>();

        List<String> list = new ArrayList<String>();
        map.put(true, list);

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArray);
        try {
            output.writeObject(map);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream input = new ByteArrayInputStream(byteArray.toByteArray());
        Hessian2Input hessianInput = new Hessian2Input(input);
        Object decObject = null;
        try {
            decObject = hessianInput.readObject();
            hessianInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(decObject);
    }
}