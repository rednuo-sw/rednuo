package com.rednuo.core.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.github.pagehelper.PageInfo;
import com.rednuo.core.binding.binder.EntityBinder;
import com.rednuo.core.binding.binder.EntityListBinder;
import com.rednuo.core.binding.binder.FieldBinder;
import com.rednuo.core.utils.IGetter;
import com.rednuo.core.utils.ISetter;
import com.rednuo.core.vo.KeyValue;
import com.rednuo.core.vo.Pagination;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基础的Service服务
 * @author nz.zou 2021/5/6
 * @since rednuo 1.0.0
 */
public interface BaseService<T> {
    /**
     * 获取对应 entity 的 BaseMapper
     *
     * @return 结果 BaseMapper
     */
    BaseMapper<T> getMapper();

    /**
     * 构建mybatis-plus的query
     * @return 结果
     */
    QueryChainWrapper<T> query();
    /**
     * 构建mybatis-plus的lambdaQuery
     * @return 结果
     */
    LambdaQueryChainWrapper<T> lambdaQuery();
    /**
     * 构建mybatis-plus的update
     * @return 结果
     */
    UpdateChainWrapper<T> update();
    /**
     * 构建mybatis-plus的lambdaUpdate
     * @return 结果
     */
    LambdaUpdateChainWrapper<T> lambdaUpdate();

    /**
     * 获取Entity实体
     * @param id 主键
     * @return 结果 entity
     */
    T getEntity(Serializable id);

    /**
     * 获取entity某个属性值
     * @param idGetterFn id getter
     * @param idVal id值
     * @param getterFn 返回属性getter
     * @return 结果
     */
    <FT> FT getValueOfField(SFunction<T, ?> idGetterFn, Serializable idVal, SFunction<T, FT> getterFn);

    /**
     * 创建Entity实体
     * @param entity e
     * @return 结果 true:成功, false:失败
     */
    boolean createEntity(T entity);

    /***
     * 批量创建Entity
     * @param entityList 实体对象列表
     * @return 结果 true:成功, false: 失败
     */
    boolean createEntities(Collection<T> entityList);

    /**
     * 添加entity 及 其关联子项entities
     * @param entity 主表entity
     * @param relatedEntities 关联表entities
     * @param relatedEntitySetter 关联Entity类的setter
     * @return 结果
     */
    <RE, R> boolean createEntityAndRelatedEntities(T entity, List<RE> relatedEntities, ISetter<RE, R> relatedEntitySetter);

    /**
     * 创建或更新n-n关联
     * （在主对象的service中调用，不依赖中间表service实现中间表操作）
     * @param driverIdGetter 驱动对象getter
     * @param driverId 驱动对象ID
     * @param followerIdGetter 从动对象getter
     * @param followerIdList 从动对象id集合
     * @return 结果
     */
    <R> boolean createOrUpdateN2NRelations(SFunction<R, ?> driverIdGetter, Object driverId, SFunction<R, ?> followerIdGetter, List<? extends Serializable> followerIdList);

    /**
     * 更新Entity实体
     * @param entity entity
     * @return 结果
     */
    boolean updateEntity(T entity);

    /**
     * 更新Entity实体（更新符合条件的所有非空字段）
     * @param entity entity
     * @param updateCriteria u
     * @return 结果
     */
    boolean updateEntity(T entity, Wrapper updateCriteria);

    /**
     * 更新Entity实体（仅更新updateWrapper.set指定的字段）
     * @param updateWrapper
     * @return 结果
     */
    boolean updateEntity(Wrapper updateWrapper);

    /**
     * 批量更新entity
     * @param entityList
     * @return 结果
     */
    boolean updateEntities(Collection<T> entityList);

    /***
     * 创建或更新entity（entity.id存在则新建，否则更新）
     * @param entity entity
     * @return 结果
     */
    boolean createOrUpdateEntity(T entity);

    /**
     * 批量创建或更新entity（entity.id存在则新建，否则更新）
     * @param entityList el
     * @return 结果
     */
    boolean createOrUpdateEntities(Collection entityList);

    /**
     * 添加entity 及 其关联子项entities
     * @param entity 主表entity
     * @param relatedEntities 关联表entities
     * @param relatedEntitySetter 关联Entity类的setter
     * @return 结果
     */
    <RE,R> boolean updateEntityAndRelatedEntities(T entity, List<RE> relatedEntities, ISetter<RE, R> relatedEntitySetter);

    /**
     * 删除entity 及 其关联子项entities
     * @param id 待删除entity的主键
     * @param relatedEntityClass 待删除关联Entity类
     * @param  relatedEntitySetter 待删除类的setter方法
     * @return 结果
     */
    <RE,R> boolean deleteEntityAndRelatedEntities(Serializable id, Class<RE> relatedEntityClass, ISetter<RE, R> relatedEntitySetter);

    /**
     * 根据主键删除实体
     * @param id 主键
     * @return 结果 true:成功, false:失败
     */
    boolean deleteEntity(Serializable id);

    /**
     * 按条件删除实体
     * @param queryWrapper 查询条件
     * @return 结果
     */
    boolean deleteEntities(Wrapper queryWrapper);

    /**
     * 批量删除指定id的实体
     * @param entityIds
     * @return 结果
     */
    boolean deleteEntities(Collection<? extends Serializable> entityIds);

    /**
     * 获取符合条件的entity记录总数
     * @return 结果
     */
    int getEntityListCount(Wrapper queryWrapper);

    /**
     * 获取指定条件的Entity集合
     * @param queryWrapper 查询条件
     * @return 结果
     */
    List<T> getEntityList(Wrapper queryWrapper);

    /**
     * 获取指定条件的Entity集合
     * @param queryWrapper 查询条件
     * @param pagination p
     * @return 结果
     */
    List<T> getEntityList(Wrapper queryWrapper, Pagination pagination);

    /**
     * 获取指定条件的Entity ID集合
     * @param queryWrapper 查询条件
     * @param getterFn g
     * @return 结果
     */
    <FT> List<FT> getValuesOfField(Wrapper queryWrapper, SFunction<T, ?> getterFn);

    /**
     * 获取指定条件的Entity集合
     * @param ids ids
     * @return 结果
     */
    List<T> getEntityListByIds(List ids);

    /**
     * 获取指定数量的entity记录
     * @param queryWrapper 查询条件
     * @param limitCount l
     * @return 结果
     */
    List<T> getEntityListLimit(Wrapper queryWrapper, int limitCount);

    /**
     * 获取符合条件的一个Entity实体
     * @param queryWrapper 查询条件
     * @return 结果 entity
     */
    T getSingleEntity(Wrapper queryWrapper);

    /**
     * 是否存在符合条件的记录
     * @param getterFn entity的getter方法
     * @param value 需要检查的值
     * @return 结果
     */
    boolean exists(IGetter<T> getterFn, Object value);

    /**
     * 是否存在符合条件的记录
     * @param queryWrapper 查询条件
     * @return 结果
     */
    boolean exists(Wrapper queryWrapper);

    /**
     * 获取指定属性的Map列表
     * @param queryWrapper 查询条件
     * @return 结果
     */
    List<Map<String, Object>> getMapList(Wrapper queryWrapper);

    /**
     * 获取指定属性的Map列表
     * @param queryWrapper 查询条件
     * @param pagination p
     * @return 结果
     */
    List<Map<String, Object>> getMapList(Wrapper queryWrapper, Pagination pagination);

    /***
     * 获取键值对的列表，用于构建select下拉选项等
     *
     * @param queryWrapper 查询条件
     * @return 结果
     */
    List<KeyValue> getKeyValueList(Wrapper queryWrapper);

    /***
     * 获取键值对的Map
     *
     * @param queryWrapper 查询条件
     * @return 结果
     */
    Map<String, Object> getKeyValueMap(Wrapper queryWrapper);

    /**
     * 获取View Object对象
     * @param id 主键
     * @param voClass vo类
     * @return 结果 entity
     */
    <VO> VO getViewObject(Serializable id, Class<VO> voClass);

    /**
     * 根据查询条件获取vo列表
     * @param queryWrapper 查询条件
     * @param pagination p
     * @return 结果
     */
    <VO> List<VO> getViewObjectList(Wrapper queryWrapper, Pagination pagination, Class<VO> voClass);

    /***
     * 绑定字段值到VO列表的元素中
     * @param voList l
     * @return 结果
     */
    FieldBinder<T> bindingFieldTo(List voList);

    /***
     * 绑定entity对象到VO列表元素中
     * @param voList l
     * @return 结果
     */
    EntityBinder<T> bindingEntityTo(List voList);

    /***
     * 绑定entity对象列表到VO列表元素中(适用于VO-Entity一对多的关联)
     * @param voList vo列表
     * @return 结果
     */
    EntityListBinder<T> bindingEntityListTo(List voList);
}
