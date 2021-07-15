package com.rednuo.core.starter;

import com.rednuo.core.utils.S;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author nz.zou 2021/7/14
 * @since avery 1.0.0
 */
@ConfigurationProperties(prefix = "rednuo.core")
public class CoreProperties {

    /**
     * 统一响应包名
     */

    @NestedConfigurationProperty
    private String[] ursPackage= {"com.rednuo"};

    public void setUrsPackage(String ursPackage) {
        if(!S.contains(ursPackage,"com.rednuo")){
            ursPackage += ",com.rednuo";
        }
        ursPackage.replace(" ", "");
        this.ursPackage = S.split(ursPackage,S.SEPARATOR);
    }
    public String[] getUrsPackage(){
        return ursPackage;
    }
}
