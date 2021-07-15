package com.rednuo.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rednuo.core.binding.Binder;
import com.rednuo.core.binding.QueryBuilder;
import com.rednuo.core.config.Cons;
import com.rednuo.core.utils.S;
import com.rednuo.core.utils.V;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 基本父类控制器
 * @author  nz.zou 2021/5/7
 * @since rednuo 1.0.0
 */
@Slf4j
public class BaseController {

    @Autowired
    protected HttpServletRequest request;

    /***
     * 构建查询QueryWrapper (根据BindQuery注解构建相应的查询条件)
     * @param entityOrDto Entity对象或者DTO对象 (属性若无BindQuery注解，默认构建为为EQ相等条件)
     * @see #buildQueryWrapperByDTO #buildQueryWrapperByQueryParams
     * @return 结果 结果
     */
    @Deprecated
    protected <DTO> QueryWrapper<DTO> buildQueryWrapper(DTO entityOrDto) throws Exception{
        return buildQueryWrapperByQueryParams(entityOrDto);
    }

    /***
     * 根据DTO构建查询QueryWrapper (根据BindQuery注解构建相应的查询条件，DTO中的非空属性均参与构建)
     * @param entityOrDto Entity对象或者DTO对象 (属性若无BindQuery注解，默认构建为为EQ相等条件)
     * @return 结果 结果
     */
    protected <DTO> QueryWrapper<DTO> buildQueryWrapperByDTO(DTO entityOrDto) throws Exception{
        return QueryBuilder.toQueryWrapper(entityOrDto);
    }

    /***
     * 根据请求参数构建查询QueryWrapper (根据BindQuery注解构建相应的查询条件，url中的请求参数参与构建)
     * @param entityOrDto Entity对象或者DTO对象 (属性若无BindQuery注解，默认构建为为EQ相等条件)
     * @return 结果 结果
     */
    protected <DTO> QueryWrapper<DTO> buildQueryWrapperByQueryParams(DTO entityOrDto) throws Exception{
        return QueryBuilder.toQueryWrapper(entityOrDto, extractQueryParams());
    }

    /***
     * 构建查询LambdaQueryWrapper (根据BindQuery注解构建相应的查询条件)
     * @param entityOrDto Entity对象或者DTO对象 (属性若无BindQuery注解，默认构建为为EQ相等条件)
     * @see #buildLambdaQueryWrapperByDTO #buildLambdaQueryWrapperByQueryParams
     * @return 结果 结果
     */
    @Deprecated
    protected <DTO> LambdaQueryWrapper<DTO> buildLambdaQueryWrapper(DTO entityOrDto) throws Exception{
        return buildLambdaQueryWrapperByQueryParams(entityOrDto);
    }

    /***
     * 根据DTO构建查询LambdaQueryWrapper (根据BindQuery注解构建相应的查询条件，DTO中的非空属性均参与构建)
     * @param entityOrDto Entity对象或者DTO对象 (属性若无BindQuery注解，默认构建为为EQ相等条件)
     * @return 结果 结果
     */
    protected <DTO> LambdaQueryWrapper<DTO> buildLambdaQueryWrapperByDTO(DTO entityOrDto) throws Exception{
        return QueryBuilder.toLambdaQueryWrapper(entityOrDto);
    }

    /***
     * 根据请求参数构建查询LambdaQueryWrapper (根据BindQuery注解构建相应的查询条件，url中的请求参数参与构建)
     * @param entityOrDto Entity对象或者DTO对象 (属性若无BindQuery注解，默认构建为为EQ相等条件)
     * @return 结果 结果
     */
    protected <DTO> LambdaQueryWrapper<DTO> buildLambdaQueryWrapperByQueryParams(DTO entityOrDto) throws Exception{
        return QueryBuilder.toLambdaQueryWrapper(entityOrDto, extractQueryParams());
    }

    /***
     * 获取请求参数Map
     * @return 结果 结果
     */
    protected Map<String, Object> getParamsMap() throws Exception{
        return getParamsMap(null);
    }

    /***
     * 获取请求参数Map
     * @return 结果 结果
     */
    private Map<String, Object> getParamsMap(List<String> paramList) throws Exception{
        Map<String, Object> result = new HashMap<>(8);
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()){
            String paramName = (String) paramNames.nextElement();
            // 如果非要找的参数，则跳过
            if(V.notEmpty(paramList) && !paramList.contains(paramName)){
                continue;
            }
            String[] values = request.getParameterValues(paramName);
            if(V.notEmpty(values)){
                if(values.length == 1){
                    if(V.notEmpty(values[0])){
                        String paramValue = java.net.URLDecoder.decode(values[0], Cons.CHARSET_UTF8);
                        result.put(paramName, paramValue);
                    }
                }
                else{
                    String[] valueArray = new String[values.length];
                    for(int i=0; i<values.length; i++){
                        valueArray[i] = java.net.URLDecoder.decode(values[i], Cons.CHARSET_UTF8);
                    }
                    // 多个值需传递到后台SQL的in语句
                    result.put(paramName, valueArray);
                }
            }
        }

        return result;
    }

    /***
     * 获取请求URI (去除contextPath)
     * @return 结果 结果
     */
    protected String getRequestMappingURI(){
        String contextPath = request.getContextPath();
        if(V.notEmpty(contextPath)){
            return S.replace(request.getRequestURI(), contextPath, "");
        }
        return request.getRequestURI();
    }

    /**
     * 提取请求参数名集合
     * @return 结果 结果
     */
    protected Set<String> extractQueryParams(){
        Map<String, Object> paramValueMap = convertParams2Map();
        if(V.notEmpty(paramValueMap)){
            return paramValueMap.keySet();
        }
        return Collections.EMPTY_SET;
    }

    /***
     * 将请求参数值转换为Map
     * @return 结果 结果
     */
    protected Map<String, Object> convertParams2Map(){
        Map<String, Object> result = new HashMap<>(8);
        if(request == null){
            return result;
        }
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()){
            String paramName = (String) paramNames.nextElement();
            String[] values = request.getParameterValues(paramName);
            if(V.notEmpty(values)){
                if(values.length == 1){
                    if(V.notEmpty(values[0])){
                        result.put(paramName, values[0]);
                    }
                }
                else{
                    // 多个值需传递到后台SQL的in语句
                    result.put(paramName, values);
                }
            }
        }
        return result;
    }

    /**
     * 自动转换为VO并绑定关联关系
     *
     * @param entityList l
     * @param voClass vo
     * @param <VO> vo
     * @return 结果
     */
    @Deprecated
    protected <VO> List<VO> convertToVoAndBindRelations(List entityList, Class<VO> voClass) {
        // 转换为VO
        List<VO> voList = Binder.convertAndBindRelations(entityList, voClass);
        return voList;
    }

    /***
     * 打印所有参数信息
     */
    protected void dumpParams(){
        Map<String, String[]> params = request.getParameterMap();
        if(params != null && !params.isEmpty()){
            StringBuilder sb = new StringBuilder();
            for(Map.Entry<String, String[]> entry : params.entrySet()){
                String[] values = entry.getValue();
                if(values != null && values.length > 0){
                    sb.append(entry.getKey() + "=" + S.join(values)+"; ");
                }
            }
            log.debug(sb.toString());
        }
    }

    /**
     * 从request获取Long参数
     * @param param p
     * @return 结果
     */
    protected Long getLong(String param){
        return S.toLong(request.getParameter(param));
    }

    /**
     * 从request获取Long参数
     * @param param p
     * @param defaultValue v
     * @return 结果
     */
    protected long getLong(String param, Long defaultValue){
        return S.toLong(request.getParameter(param), defaultValue);
    }

    /**
     * 从request获取Int参数
     * @param param
     * @return 结果
     */
    protected Integer getInteger(String param){
        return S.toInt(request.getParameter(param));
    }

    /**
     * 从request获取Int参数
     * @param param
     * @param defaultValue
     * @return 结果
     */
    protected int getInt(String param, Integer defaultValue){
        return S.toInt(request.getParameter(param), defaultValue);
    }

    /***
     * 从request中获取boolean值
     * @param param
     * @return 结果
     */
    protected boolean getBoolean(String param){
        return S.toBoolean(request.getParameter(param));
    }

    /***
     * 从request中获取boolean值
     * @param param
     * @param defaultBoolean
     * @return 结果
     */
    protected boolean getBoolean(String param, boolean defaultBoolean){
        return S.toBoolean(request.getParameter(param), defaultBoolean);
    }

    /**
     * 从request获取Double参数
     * @param param
     * @return 结果
     */
    protected Double getDouble(String param){
        if(V.notEmpty(request.getParameter(param))){
            return Double.parseDouble(request.getParameter(param));
        }
        return null;
    }

    /**
     * 从request获取Double参数
     * @param param
     * @param defaultValue
     * @return 结果
     */
    protected Double getDouble(String param, Double defaultValue){
        if(V.notEmpty(request.getParameter(param))){
            return Double.parseDouble(request.getParameter(param));
        }
        return defaultValue;
    }

    /**
     * 从request获取String参数
     * @param param
     * @return 结果
     */
    protected String getString(String param){
        if(V.notEmpty(request.getParameter(param))){
            return request.getParameter(param);
        }
        return null;
    }

    /**
     * 从request获取String参数
     * @param param
     * @param defaultValue
     * @return 结果
     */
    protected String getString(String param, String defaultValue){
        if(V.notEmpty(request.getParameter(param))){
            return request.getParameter(param);
        }
        return defaultValue;
    }

    /**
     * 从request获取String[]参数
     * @param param
     * @return 结果
     */
    protected String[] getStringArray(String param){
        if(request.getParameterValues(param) != null){
            return request.getParameterValues(param);
        }
        return null;
    }

    /***
     * 从request里获取String列表
     * @param param
     * @return 结果
     */
    protected List<String> getStringList(String param){
        String[] strArray = getStringArray(param);
        if(V.isEmpty(strArray)){
            return null;
        }
        return Arrays.asList(strArray);
    }

    /***
     * 从request里获取Long列表
     * @param param
     * @return 结果
     */
    protected List<Long> getLongList(String param){
        String[] strArray = getStringArray(param);
        if(V.isEmpty(strArray)){
            return null;
        }
        List<Long> longList = new ArrayList<>();
        for(String str : strArray){
            if(V.notEmpty(str)){
                longList.add(Long.parseLong(str));
            }
        }
        return longList;
    }
}
