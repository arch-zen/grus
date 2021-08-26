/*
 * Copyright 2007-2020, CIIC Guanaitong, Co., Ltd.
 * All rights reserved.
 */

package com.ciicgat.sdk.gconf.support;

import com.ciicgat.grus.service.naming.NamingService;
import com.ciicgat.sdk.gconf.GlobalGconfConfig;

import java.util.Properties;

/**
 * Created by August.Zhou on 2018-11-21 14:52.
 */
public class GconfEndPointNamingService implements NamingService {


    @Override
    public String resolve(String serviceName) {
        Properties properties = GlobalGconfConfig.getConfig().getProperties("service_address.properties");
        if (properties == null) {
            return null;
        }
        return properties.getProperty(serviceName);
    }
}
