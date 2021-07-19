package com.rednuo.core.starter;

import com.rednuo.core.utils.S;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author nz.zou 2021/7/14
 * @since avery 1.0.0
 */
@ConfigurationProperties(prefix = CoreProperties.REDNUO_PREFIX)
public class CoreProperties {

    public static final String REDNUO_PREFIX = "rednuo.core";
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
