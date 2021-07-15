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
package com.rednuo.core.binding.query.dynamic;


import com.rednuo.core.binding.JoinsBinder;
import com.rednuo.core.binding.parser.ParserCache;
import com.rednuo.core.vo.Pagination;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

/**
 * 动态查询wrapper
 * @author Mazc@dibo.ltd
 * @version v2.0
 *   2020/04/16
 */
public class DynamicJoinQueryWrapper<DTO,E> extends ExtQueryWrapper<DTO,E> {
    public DynamicJoinQueryWrapper(Class<DTO> dtoClass, Collection<String> fields){
        this.dtoClass = dtoClass;
        this.fields = fields;
    }

    /**
     * DTO类
     */
    @Getter
    private Class<DTO> dtoClass;
    /**
     * 字段
     */
    private Collection<String> fields;

    /**
     * dto字段和值
     */
    public List<AnnoJoiner> getAnnoJoiners(){
        return ParserCache.getAnnoJoiners(this.dtoClass, fields);
    }

    /**
     * 查询一条数据
     * @param entityClazz clazz
     * @return 结果 结果 e
     */
    @Override
    public E queryOne(Class<E> entityClazz){
        return JoinsBinder.queryOne(this, entityClazz);
    }

    /**
     * 查询一条数据
     * @param entityClazz clazz
     * @return 结果 结果 l
     */
    @Override
    public List<E> queryList(Class<E> entityClazz){
        return JoinsBinder.queryList(this, entityClazz);
    }

    /**
     * 查询一条数据
     * @param entityClazz clazz
     * @return 结果 结果 list
     */
    @Override
    public List<E> queryList(Class<E> entityClazz, Pagination pagination){
        return JoinsBinder.queryList(this, entityClazz, pagination);
    }

}
