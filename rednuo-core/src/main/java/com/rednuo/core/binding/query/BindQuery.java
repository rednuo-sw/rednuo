/*
 * Copyright (c) 2015-2020, www.dibo.ltd (service@dibo.ltd).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.rednuo.core.binding.query;

import javax.lang.model.type.NullType;
import java.lang.annotation.*;

/**
 * 绑定管理器
 * @author mazc@dibo.ltd
 * @version v2.0
 *   2019/7/18
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BindQuery {

    /**
     * 查询条件
     * @return 结果
     */
    Comparison comparison() default Comparison.EQ;

    /**
     * 数据库字段，默认为空，自动根据驼峰转下划线
     * @return 结果
     */
    String field() default "";

    /***
     * 绑定的Entity类
     * @return 结果
     */
    Class entity() default NullType.class;

    /***
     * JOIN连接条件，支持动态的跨表JOIN查询
     * @return 结果
     */
    String condition() default "";

    /**
     * 忽略该字段
     * @return 结果
     */
    boolean ignore() default false;
}
