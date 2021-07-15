package com.rednuo.core.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rednuo.core.config.Cons;
import com.rednuo.core.entity.BaseEntity;
import com.rednuo.core.exception.CoreCode;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.response.ResponseResult;
import com.rednuo.core.service.BaseService;
import com.rednuo.core.service.DictionaryService;
import com.rednuo.core.utils.BeanUtils;
import com.rednuo.core.utils.ContextHelper;
import com.rednuo.core.utils.S;
import com.rednuo.core.utils.V;
import com.rednuo.core.vo.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CURD增删改查 通用RestController 父类
 * @author nz.zou 2021/5/24
 * @since rednuo 1.0.0
 */
@Slf4j
public class BaseCrudRestController<E extends BaseEntity> extends BaseController {
    /**
     * Entity，VO对应的class
     */
    private Class<E> entityClass;
    /**
     * Service实现类
     */
    private BaseService baseService;
    /**
     * 字典service
     */
    @Autowired(required = false)
    protected DictionaryService dictionaryService;

    /**
     * 查询ViewObject，用于子类重写的方法
     * @param id id
     * @return 结果
     * @throws Exception e e
     */
    protected <VO> VO getViewObject(Serializable id, Class<VO> voClass) throws Exception{
        // 检查String类型id
        if(id instanceof String && !S.isNumeric((String)id)){
            String pk = ContextHelper.getPrimaryKey(getEntityClass());
            if(Cons.FieldName.id.name().equals(pk)){
                ExceptionCast.cast(CoreCode.FAIL_INVALID_PARAM);
            }
        }
        return (VO)getService().getViewObject(id, voClass);
    }

    /**
     * 查询Entity，用于子类直接调用
     * @return 结果
     * @throws Exception e e
     */
    protected <E> E getEntity(Serializable id) throws Exception{
        // 检查String类型id
        if(id instanceof String && !S.isNumeric((String)id)){
            String pk = ContextHelper.getPrimaryKey(getEntityClass());
            if(Cons.FieldName.id.name().equals(pk)){
                ExceptionCast.cast(CoreCode.FAIL_INVALID_PARAM);
            }
        }
        return (E)getService().getEntity(id);
    }

    /***
     * 获取某VO资源的集合，用于子类重写的方法
     * <p>
     * url参数示例: /${bindURL}?pageSize=20&pageIndex=1&orderBy=itemValue&type=GENDAR
     * </p>
     * @return 结果 JsonResult
     * @throws Exception e e
     */
    protected <VO> List<VO> getViewObjectList(E entity, Pagination pagination, Class<VO> voClass) throws Exception {
        QueryWrapper<E> queryWrapper = super.buildQueryWrapperByQueryParams(entity);
        // 查询当前页的数据
        List<VO> voList = getService().getViewObjectList(queryWrapper, pagination, voClass);
        // 返回结果
        return voList;
    }

    /***
     * 获取某VO资源的集合，用于子类重写的方法
     * <p>
     * url参数示例: /${bindURL}?pageSize=20&pageIndex=1&orderBy=itemValue&type=GENDAR
     * </p>
     * @return 结果 JsonResult
     * @throws Exception e e
     */
    protected <VO> List<VO> getViewObjectList(E entity, Pagination pagination, Class<VO> voClass, boolean buildQueryWrapperByDTO) throws Exception {
        //DTO全部属性参与构建时调用
        QueryWrapper<E> queryWrapper = buildQueryWrapperByDTO? super.buildQueryWrapperByDTO(entity) : super.buildQueryWrapperByQueryParams(entity);
        // 查询当前页的数据
        List<VO> voList = getService().getViewObjectList(queryWrapper, pagination, voClass);
        // 返回结果
        return voList;
    }

    /**
     * 获取符合查询条件的全部数据（不分页）
     * @param queryWrapper 查询条件
     * @return 结果
     * @throws Exception e e
     */
    protected List getEntityList(Wrapper queryWrapper) throws Exception {
        // 查询当前页的数据
        List entityList = getService().getEntityList(queryWrapper);
        // 返回结果
        return entityList;
    }

    /***
     * 获取符合查询条件的某页数据（有分页）
     * <p>
     * url参数示例: /${bindURL}?pageSize=20&pageIndex=1
     * </p>
     * @return 结果 JsonResult
     * @throws Exception e e
     */
    protected List getEntityListWithPaging(Wrapper queryWrapper, Pagination pagination) throws Exception {
        // 查询当前页的数据
        List entityList = getService().getEntityList(queryWrapper, pagination);
        // 返回结果
        // TODO: 分页信息
        return entityList;//).bindPagination(pagination);
    }

    /***
     * 创建资源对象，用于子类重写的方法
     * @param entity entity
     * @return 结果 JsonResult
     * @throws Exception e e
     */
    protected Map<String, Object> createEntity(E entity) throws Exception {
        // 执行创建资源前的操作
        String validateResult = this.beforeCreate(entity);
        if (validateResult != null) {
            ExceptionCast.cast(CoreCode.FAIL,validateResult);
        }
        // 执行保存操作
        boolean success = getService().createEntity(entity);
        if (success) {
            // 执行创建成功后的操作
            this.afterCreated(entity);
            // 组装返回结果
            return buildPKDataMap(entity);
        } else {
            log.warn("创建操作未成功，entity=" + entity.getClass().getSimpleName());
            // 组装返回结果
            ExceptionCast.cast(CoreCode.FAIL, "创建操作未成功，entity=" + entity.getClass().getSimpleName());
        }
        return null;
    }

    /***
     * 根据ID更新资源对象，用于子类重写的方法
     * @param entity entity
     * @return 结果 JsonResult
     * @throws Exception e e
     */
    protected boolean updateEntity(Serializable id, E entity) throws Exception {
        // 如果前端没有指定entity.id，在此设置，以兼容前端不传的情况
        if(entity.getId() == null){
            String pk = ContextHelper.getPrimaryKey(getEntityClass());
            if(Cons.FieldName.id.name().equals(pk)){
                String stringId = (id instanceof String)? (String)id : id.toString();
                entity.setId(stringId);
            }
            else if(BeanUtils.getProperty(entity, pk) == null){
                BeanUtils.setProperty(entity, pk, id);
            }
        }
        // 执行更新资源前的操作
        String validateResult = this.beforeUpdate(entity);
        if (validateResult != null) {
            ExceptionCast.cast(CoreCode.FAIL, validateResult);
        }
        // 执行保存操作
        boolean success = getService().updateEntity(entity);
        if (success) {
            // 执行更新成功后的操作
            this.afterUpdated(entity);
            return true;
        } else {
            log.warn("更新操作失败，{}:{}", entity.getClass().getSimpleName(), entity.getId());
            // 返回操作结果
            ExceptionCast.cast(CoreCode.FAIL);
        }
        return false;
    }

    /***
     * 根据id删除资源对象，用于子类重写的方法
     * @param id id
     * @return 结果
     * @throws Exception e e
     */
    protected boolean deleteEntity(Serializable id) throws Exception {
        if (id == null) {
            ExceptionCast.cast(CoreCode.FAIL_INVALID_PARAM, "请选择需要删除的条目！");
        }
        // 是否有权限删除
        E entity = (E) getService().getEntity(id);
        String validateResult = beforeDelete(entity);
        if (validateResult != null) {
            // 返回json
           ExceptionCast.cast(CoreCode.FAIL, validateResult);
        }
        // 执行删除操作
        boolean success = getService().deleteEntity(id);
        if (success) {
            // 执行更新成功后的操作
            this.afterDeleted(entity);
            log.info("删除操作成功，{}:{}", entity.getClass().getSimpleName(), id);
            return true;
        } else {
            log.warn("删除操作未成功，{}:{}", entity.getClass().getSimpleName(), id);
            ExceptionCast.cast(CoreCode.FAIL);
        }
        return false;
    }

    /***
     * 根据id批量删除资源对象，用于子类重写的方法
     * @param ids ids
     * @return 结果
     * @throws Exception e e
     */
    protected boolean batchDeleteEntities(Collection<? extends Serializable> ids) throws Exception {
        if (V.isEmpty(ids)) {
            ExceptionCast.cast(CoreCode.FAIL_INVALID_PARAM, "请选择需要删除的条目！");
        }
        // 是否有权限删除
        String validateResult = beforeBatchDelete(ids);
        if (validateResult != null) {
            // 返回json
            ExceptionCast.cast(CoreCode.FAIL, validateResult);
        }
        // 执行删除操作
        boolean success = getService().deleteEntities(ids);
        if (success) {
            // 执行更新成功后的操作
            this.afterBatchDeleted(ids);
            log.info("删除操作成功，{}:{}", getEntityClass().getSimpleName(), S.join(ids));
            return true;
        } else {
            log.warn("删除操作未成功，{}:{}",getEntityClass().getSimpleName(), S.join(ids));
            ExceptionCast.cast(CoreCode.FAIL);
        }
        return false;
    }

    /**
     * 构建主键的返回值Data
     * @param entity entity
     * @return 结果
     */
    private Map<String, Object> buildPKDataMap(E entity){
        // 组装返回结果
        Map<String, Object> data = new HashMap<>(2);
        String pk = ContextHelper.getPrimaryKey(getEntityClass());
        Object pkValue = (Cons.FieldName.id.name().equals(pk))? entity.getId() : BeanUtils.getProperty(entity, pk);
        data.put(pk, pkValue);
        return data;
    }

    //============= 供子类继承重写的方法 =================
    /***
     * 创建前的相关处理
     * @param entityOrDto dto
     * @return 结果
     */
    protected String beforeCreate(Object entityOrDto) throws Exception {
        return null;
    }

    /***
     * 创建成功后的相关处理
     * @param entityOrDto dto
     */
    protected void afterCreated(Object entityOrDto) throws Exception {
    }

    /***
     * 更新前的相关处理
     * @param entityOrDto dto
     * @return 结果
     */
    protected String beforeUpdate(Object entityOrDto) throws Exception {
        return null;
    }

    /***
     * 更新成功后的相关处理
     * @param entityOrDto dto
     */
    protected void afterUpdated(Object entityOrDto) throws Exception {
    }

    /***
     * 是否有删除权限，如不可删除返回错误提示信息，如 Status.FAIL_NO_PERMISSION.label()
     * @param entityOrDto dto
     * @return 结果
     */
    protected String beforeDelete(Object entityOrDto) throws Exception{
        return null;
    }

    /***
     * 删除成功后的相关处理
     * @param entityOrDto dto
     */
    protected void afterDeleted(Object entityOrDto) throws Exception {
    }

    /***
     * 是否有批量删除权限，如不可删除返回错误提示信息，如 Status.FAIL_NO_PERMISSION.label()
     * @param ids ids
     */
    protected String beforeBatchDelete(Collection<? extends Serializable> ids) throws Exception{
        return null;
    }

    /***
     * 批量删除成功后的相关处理
     * @param ids ids
     */
    protected void afterBatchDeleted(Collection<? extends Serializable> ids) throws Exception {
    }

    /**
     * 得到service
     * @return 结果
     */
    protected BaseService getService() {
        if(this.baseService == null){
            Class<E> clazz = getEntityClass();
            if(clazz != null){
                this.baseService = ContextHelper.getBaseServiceByEntity(clazz);
            }
            if(this.baseService == null){
                log.warn("Entity: {} 无对应的Service定义，请检查！", clazz.getName());
            }
        }
        return this.baseService;
    }

    /**
     * 获取Entity的class
     * @return 结果
     */
    protected Class<E> getEntityClass(){
        if(this.entityClass == null){
            this.entityClass = BeanUtils.getGenericityClass(this, 0);
            if(this.entityClass == null) {
                log.warn("无法从 {} 类定义中获取泛型类entityClass", this.getClass().getName());
            }
        }
        return this.entityClass;
    }
}
