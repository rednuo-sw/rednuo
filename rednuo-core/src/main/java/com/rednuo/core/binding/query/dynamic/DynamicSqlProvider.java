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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rednuo.core.binding.QueryBuilder;
import com.rednuo.core.binding.parser.ParserCache;
import com.rednuo.core.config.BaseConfig;
import com.rednuo.core.config.Cons;
import com.rednuo.core.utils.S;
import com.rednuo.core.utils.V;
import org.apache.ibatis.jdbc.SQL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 动态SQL构建Provider
 * @author Mazc@dibo.ltd
 * @version v2.0  2020/04/15
 */
public class DynamicSqlProvider {

    /**
     * 构建动态SQL
     * @param ew f
     * @return 结果 结果 s
     */
    public String buildSqlForList(QueryWrapper ew){
        return buildDynamicSql(null, ew);
    }

    /**
     * 构建动态SQL
     * @param page 分页参数，用于MP分页插件AOP，不可删除
     * @param ew we
     * @return 结果 结果 s
     */
    public <DTO> String buildSqlForListWithPage(Page<?> page, QueryWrapper<DTO> ew){
        return buildDynamicSql(page, ew);
    }

    /**
     * 构建动态SQL
     * @param page 分页参数，用于MP分页插件AOP，不可删除
     * @param ew ew
     * @return 结果 结果 s
     */
    private <DTO> String buildDynamicSql(Page<?> page, QueryWrapper<DTO> ew){
        DynamicJoinQueryWrapper wrapper = (DynamicJoinQueryWrapper)ew;
        return new SQL() {{
            if(V.isEmpty(ew.getSqlSelect())){
                SELECT("self.*");
            }
            else{
                SELECT(formatSqlSelect(ew.getSqlSelect()));
            }
            FROM(wrapper.getEntityTable()+" self");
            //提取字段，根据查询条件中涉及的表，动态join
            List<AnnoJoiner> annoJoinerList = wrapper.getAnnoJoiners();
            if(V.notEmpty(annoJoinerList)){
                Set<String> tempSet = new HashSet<>();
                StringBuilder sb = new StringBuilder();
                for(AnnoJoiner joiner : annoJoinerList){
                    if(V.notEmpty(joiner.getJoin()) && V.notEmpty(joiner.getOnSegment())){
                        if(joiner.getMiddleTable() != null){
                            sb.setLength(0);
                            sb.append(joiner.getMiddleTable()).append(" ").append(joiner.getMiddleTableAlias()).append(" ON ").append(joiner.getMiddleTableOnSegment());
                            if(S.containsIgnoreCase(joiner.getMiddleTable(), " "+ Cons.COLUMN_IS_DELETED) == false && ParserCache.hasDeletedColumn(joiner.getMiddleTable())){
                                sb.append(" AND ").append(joiner.getMiddleTableAlias()).append(".").append(Cons.COLUMN_IS_DELETED).append(" = ").append(BaseConfig.getActiveFlagValue());
                            }
                            String joinSegment = sb.toString();
                            if(!tempSet.contains(joinSegment)){
                                LEFT_OUTER_JOIN(joinSegment);
                                tempSet.add(joinSegment);
                            }
                        }
                        sb.setLength(0);
                        sb.append(joiner.getJoin()).append(" ").append(joiner.getAlias()).append(" ON ").append(joiner.getOnSegment());
                        if(S.containsIgnoreCase(joiner.getOnSegment(), " "+Cons.COLUMN_IS_DELETED) == false && ParserCache.hasDeletedColumn(joiner.getJoin())){
                            sb.append(" AND ").append(joiner.getAlias()).append(".").append(Cons.COLUMN_IS_DELETED).append(" = ").append(BaseConfig.getActiveFlagValue());
                        }
                        String joinSegment = sb.toString();
                        if(!tempSet.contains(joinSegment)){
                            LEFT_OUTER_JOIN(joinSegment);
                            tempSet.add(joinSegment);
                        }
                    }
                }
                tempSet = null;
            }
            MergeSegments segments = ew.getExpression();
            if(segments != null){
                String normalSql = segments.getNormal().getSqlSegment();
                if(V.notEmpty(normalSql)){
                    WHERE(formatNormalSql(normalSql));
                    // 动态为主表添加is_deleted=0
                    String isDeletedSection = "self."+ Cons.COLUMN_IS_DELETED;
                    if(QueryBuilder.checkHasColumn(segments.getNormal(), isDeletedSection) == false && ParserCache.hasDeletedColumn(wrapper.getEntityTable())){
                        WHERE(isDeletedSection+ " = " +BaseConfig.getActiveFlagValue());
                    }
                    if(segments.getOrderBy() != null){
                        String orderBySql = segments.getOrderBy().getSqlSegment();
                        int beginIndex = S.indexOfIgnoreCase(orderBySql,"ORDER BY ");
                        if(beginIndex >= 0){
                            orderBySql = S.substring(orderBySql, beginIndex+"ORDER BY ".length());
                            ORDER_BY(orderBySql);
                        }
                    }
                }
            }
        }}.toString();
    }

    /**
     * 格式化sql select列语句
     * @param sqlSelect s
     * @return 结果 结果 s
     */
    private String formatSqlSelect(String sqlSelect){
        String[] columns = S.split(sqlSelect);
        List<String> selects = new ArrayList<>(columns.length);
        for(String column : columns){
            column = S.removeDuplicateBlank(column).trim();
            selects.add("self."+S.toSnakeCase(column));
        }
        return S.join(selects);
    }

    /**
     * 格式化where条件的sql
     * @param normalSql s
     * @return 结果 结果 s
     */
    private String formatNormalSql(String normalSql){
        if(normalSql.startsWith("(") && normalSql.endsWith(")")){
            return S.substring(normalSql,1,normalSql.length()-1);
        }
        return normalSql;
    }

}
