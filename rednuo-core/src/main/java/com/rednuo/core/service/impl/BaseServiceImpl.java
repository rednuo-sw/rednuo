package com.rednuo.core.service.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.rednuo.core.binding.Binder;
import com.rednuo.core.binding.binder.EntityBinder;
import com.rednuo.core.binding.binder.EntityListBinder;
import com.rednuo.core.binding.binder.FieldBinder;
import com.rednuo.core.binding.helper.ServiceAdaptor;
import com.rednuo.core.binding.query.dynamic.DynamicJoinQueryWrapper;
import com.rednuo.core.config.BaseConfig;
import com.rednuo.core.config.Cons;
import com.rednuo.core.exception.CoreCode;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.mapper.BaseCrudMapper;
import com.rednuo.core.service.BaseService;
import com.rednuo.core.utils.*;
import com.rednuo.core.vo.KeyValue;
import com.rednuo.core.vo.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;

/**
 * CRUD通用接口实现类
 * @author nz.zou 2021/5/6
 * @since rednuo 1.0.0
 */
@Slf4j
public class BaseServiceImpl<M extends BaseCrudMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {

    /***
     * 获取当前的Mapper对象
     * @return 结果 m
     */
    @Override
    public M getMapper(){
        return baseMapper;
    }

    @Override
    public QueryChainWrapper<T> query() {
        return ChainWrappers.queryChain(this.getBaseMapper());
    }

    @Override
    public LambdaQueryChainWrapper<T> lambdaQuery() {
        return ChainWrappers.lambdaQueryChain(this.getBaseMapper());
    }

    @Override
    public UpdateChainWrapper<T> update() {
        return ChainWrappers.updateChain(this.getBaseMapper());
    }

    @Override
    public LambdaUpdateChainWrapper<T> lambdaUpdate() {
        return ChainWrappers.lambdaUpdateChain(this.getBaseMapper());
    }

    @Override
    public T getEntity(Serializable id){
        return super.getById(id);
    }

    @Override
    public <FT> FT getValueOfField(SFunction<T, ?> idGetterFn, Serializable idVal, SFunction<T, FT> getterFn) {
        String fieldName = convertGetterToFieldName(getterFn);
        LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<T>()
                .select(idGetterFn, getterFn)
                .eq(idGetterFn, idVal);
        T entity = getSingleEntity(queryWrapper);
        return entity != null? (FT) BeanUtils.getProperty(entity, fieldName) : null;
    }

    @Override
    public boolean createEntity(T entity) {
        if(entity == null){
            warning("createEntity", "参数entity为null");
            return false;
        }
        return save(entity);
    }

    @Override
    public boolean save(T entity) {
        beforeCreateEntity(entity);
        return super.save(entity);
    }

    /**
     * 用于创建之前的自动填充等场景调用
     */
    protected void beforeCreateEntity(T entity){
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <RE, R> boolean createEntityAndRelatedEntities(T entity, List<RE> relatedEntities, ISetter<RE, R> relatedEntitySetter) {
        boolean success = createEntity(entity);
        if(!success){
            log.warn("新建Entity失败: {}", entity.toString());
            return false;
        }
        if(V.isEmpty(relatedEntities)){
            return success;
        }
        Class relatedEntityClass = relatedEntities.get(0).getClass();
        // 获取主键
        Object pkValue = getPrimaryKeyValue(entity);
        String attributeName = BeanUtils.convertToFieldName(relatedEntitySetter);
        // 填充关联关系
        relatedEntities.stream().forEach(relatedEntity->{
            BeanUtils.setProperty(relatedEntity, attributeName, pkValue);
        });
        // 获取关联对象对应的Service
        BaseService relatedEntityService = ContextHelper.getBaseServiceByEntity(relatedEntityClass);
        if(relatedEntityService != null){
            return relatedEntityService.createEntities(relatedEntities);
        }
        else{
            // 查找mapper
            BaseMapper mapper = ContextHelper.getBaseMapperByEntity(entity.getClass());
            // 新增关联，无service只能循环插入
            for(RE relation : relatedEntities){
                mapper.insert(relation);
            }
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createEntities(Collection<T> entityList){
        if(V.isEmpty(entityList)){
            return false;
        }
        if(DbType.SQL_SERVER.getDb().equalsIgnoreCase(ContextHelper.getDatabaseType())){
            for(T entity : entityList){
                createEntity(entity);
            }
            return true;
        }
        else{
            // 批量插入
            return saveBatch(entityList, BaseConfig.getBatchSize());
        }
    }

    @Override
    public boolean saveBatch(Collection<T> entityList, int batchSize){
        // 批量插入
        beforeCreateEntities(entityList);
        return super.saveBatch(entityList, batchSize);
    }

    /**
     * 用于创建之前的自动填充等场景调用
     */
    protected void beforeCreateEntities(Collection<T> entityList){
        if(V.isEmpty(entityList)){
            return;
        }
        for(T entity : entityList){
            beforeCreateEntity(entity);
        }
    }

    @Override
    public boolean updateEntity(T entity) {
        boolean success = super.updateById(entity);
        return success;
    }

    @Override
    public boolean updateEntity(T entity, Wrapper updateWrapper) {
        boolean success = super.update(entity, updateWrapper);
        return success;
    }

    @Override
    public boolean updateEntity(Wrapper updateWrapper) {
        boolean success = super.update(null, updateWrapper);
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEntities(Collection<T> entityList) {
        if(V.isEmpty(entityList)){
            return false;
        }
        boolean success = super.updateBatchById(entityList);
        return success;
    }

    @Override
    public boolean createOrUpdateEntity(T entity) {
        boolean success = super.saveOrUpdate(entity);
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrUpdateEntities(Collection entityList) {
        if(V.isEmpty(entityList)){
            warning("createOrUpdateEntities", "参数entityList为空!");
            return false;
        }
        // 批量插入
        return super.saveOrUpdateBatch(entityList, BaseConfig.getBatchSize());
    }

    /**
     * 更新n-n关联
     * @return 结果 b
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public     <R> boolean createOrUpdateN2NRelations(SFunction<R, ?> driverIdGetter, Object driverId,
                                                      SFunction<R, ?> followerIdGetter, List<? extends Serializable> followerIdList)
    {
        if(driverId == null){
            ExceptionCast.cast(CoreCode.FAIL,"主动ID值不能为空！");
        }
        // 从getter中获取class和fieldName
        com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda lambda = LambdaUtils.resolve(driverIdGetter);
        Class<R> middleTableClass = (Class<R>) lambda.getImplClass();
        // 获取主动从动字段名
        String driverFieldName = PropertyNamer.methodToProperty(lambda.getImplMethodName());
        String followerFieldName = convertGetterToFieldName(followerIdGetter);
        List<R> n2nRelations = null;
        if(V.notEmpty(followerIdList)){
            n2nRelations = new ArrayList<>(followerIdList.size());
            try{
                for(Serializable followerId : followerIdList){
                    R relation = middleTableClass.newInstance();
                    BeanUtils.setProperty(relation, driverFieldName, driverId);
                    BeanUtils.setProperty(relation, followerFieldName, followerId);
                    n2nRelations.add(relation);
                }
            }
            catch (Exception e){
                ExceptionCast.cast(CoreCode.FAIL, e.getLocalizedMessage());
            }
        }
        // 删除已有关联
        LambdaQueryWrapper<R> queryWrapper = new QueryWrapper<R>().lambda()
                .eq(driverIdGetter, driverId);
        // 查找service
        BaseService baseService = ContextHelper.getBaseServiceByEntity(middleTableClass);
        if(baseService != null){
            // 条件为空，不删除
            baseService.deleteEntities(queryWrapper);
            // 添加
            if(V.notEmpty(n2nRelations)){
                baseService.createEntities(n2nRelations);
            }
        }
        else{
            // 查找mapper
            BaseMapper mapper = ContextHelper.getBaseMapperByEntity(middleTableClass);
            // 条件为空，不删除
            mapper.delete(queryWrapper);
            // 新增关联，无service只能循环插入
            if(V.notEmpty(n2nRelations)){
                for(R relation : n2nRelations){
                    mapper.insert(relation);
                }
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <RE,R> boolean updateEntityAndRelatedEntities(T entity, List<RE> relatedEntities, ISetter<RE,R> relatedEntitySetter) {
        boolean success = updateEntity(entity);
        if(!success){
            log.warn("更新Entity失败: {}", entity.toString());
            return false;
        }
        // 获取关联entity的类
        Class relatedEntityClass = null;
        if(V.notEmpty(relatedEntities)){
            relatedEntityClass = BeanUtils.getTargetClass(relatedEntities.get(0));
        }
        else{
            try{
                relatedEntityClass = Class.forName(BeanUtils.getSerializedLambda(relatedEntitySetter).getImplClass().replaceAll("/", "."));
            }
            catch (Exception e){
                log.warn("无法识别关联Entity的Class", e.getMessage());
                return false;
            }
        }
        // 获取关联对象对应的Service
        BaseService relatedEntityService = ContextHelper.getBaseServiceByEntity(relatedEntityClass);
        if(relatedEntityService == null){
            log.error("未能识别到Entity: {} 的Service实现，请检查！", relatedEntityClass.getName());
            return false;
        }
        // 获取主键
        Object pkValue = getPrimaryKeyValue(entity);
        String attributeName = BeanUtils.convertToFieldName(relatedEntitySetter);
        //获取原 关联entity list
        QueryWrapper<RE> queryWrapper = new QueryWrapper();
        queryWrapper.eq(S.toSnakeCase(attributeName), pkValue);
        List<RE> oldRelatedEntities = relatedEntityService.getEntityList(queryWrapper);

        // 遍历更新关联对象
        Set relatedEntityIds = new HashSet();
        if(V.notEmpty(relatedEntities)){
            // 新建 修改 删除
            List<RE> newRelatedEntities = new ArrayList<>();
            for(RE relatedEntity : relatedEntities){
                BeanUtils.setProperty(relatedEntity, attributeName, pkValue);
                Object relPkValue = getPrimaryKeyValue(relatedEntity);
                if(V.notEmpty(relPkValue)){
                    relatedEntityService.updateEntity(relatedEntity);
                }
                else{
                    newRelatedEntities.add(relatedEntity);
                }
                relatedEntityIds.add(relPkValue);
            }
            relatedEntityService.createEntities(newRelatedEntities);
        }
        // 遍历已有关联对象
        if(V.notEmpty(oldRelatedEntities)){
            List deleteRelatedEntityIds = new ArrayList();
            for(RE relatedEntity : oldRelatedEntities){
                Object relPkValue = getPrimaryKeyValue(relatedEntity);
                if(!relatedEntityIds.contains(relPkValue)){
                    deleteRelatedEntityIds.add(relPkValue);
                }
            }
            relatedEntityService.deleteEntities(deleteRelatedEntityIds);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <RE,R> boolean deleteEntityAndRelatedEntities(Serializable id, Class<RE> relatedEntityClass, ISetter<RE,R> relatedEntitySetter) {
        boolean success = deleteEntity(id);
        if(!success){
            log.warn("删除Entity失败: {}",id);
            return false;
        }
        // 获取关联对象对应的Service
        BaseService relatedEntityService = ContextHelper.getBaseServiceByEntity(relatedEntityClass);
        if(relatedEntityService == null){
            log.error("未能识别到Entity: {} 的Service实现，请检查！", relatedEntityClass.getClass().getName());
            return false;
        }
        // 获取主键的关联属性
        String attributeName = BeanUtils.convertToFieldName(relatedEntitySetter);
        QueryWrapper<RE> queryWrapper = new QueryWrapper<RE>().eq(S.toSnakeCase(attributeName), id);
        // 删除关联子表数据
        return relatedEntityService.deleteEntities(queryWrapper);
    }

    @Override
    public boolean deleteEntity(Serializable id) {
        return super.removeById(id);
    }

    @Override
    public boolean deleteEntities(Wrapper queryWrapper){
        // 执行
        return super.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEntities(Collection<? extends Serializable> entityIds) {
        if(V.isEmpty(entityIds)){
            return false;
        }
        return super.removeByIds(entityIds);
    }

    @Override
    public int getEntityListCount(Wrapper queryWrapper) {
        return super.count(queryWrapper);
    }

    @Override
    public List<T> getEntityList(Wrapper queryWrapper) {
        return getEntityList(queryWrapper, null);
    }

    @Override
    public List<T> getEntityList(Wrapper queryWrapper, Pagination pagination) {
        // 如果是动态join，则调用JoinsBinder
        if(queryWrapper instanceof DynamicJoinQueryWrapper){
            return Binder.joinQueryList((DynamicJoinQueryWrapper)queryWrapper, entityClass, pagination);
        }
        // 否则，调用MP默认实现
        // TODO:这里是全部查出来了.
        if(pagination != null){
            IPage<T> page = convertToIPage(queryWrapper, pagination);
            page = super.page(page, queryWrapper);
            // 如果重新执行了count进行查询，则更新pagination中的总数
            if(page.isSearchCount()){
                pagination.setTotalCount(page.getTotal());
            }
            return page.getRecords();
        }
        else{
            List<T> list = super.list(queryWrapper);
            if(list == null){
                list = Collections.emptyList();
            }
            else if(list.size() > BaseConfig.getBatchSize()){
                log.warn("单次查询记录数量过大，请及时检查优化。返回结果数={}", list.size());
            }
            return list;
        }
    }

    /**
     * 获取指定条件的Entity ID集合
     * @param queryWrapper ew
     * @param getterFn f
     * @return 结果 list
     */
    @Override
    public <FT> List<FT> getValuesOfField(Wrapper queryWrapper, SFunction<T, ?> getterFn){
        String fieldName = convertGetterToFieldName(getterFn);
        String columnName = S.toSnakeCase(fieldName);
        // 优化SQL，只查询当前字段
        if(queryWrapper instanceof QueryWrapper){
            if(V.isEmpty(queryWrapper.getSqlSelect())){
                ((QueryWrapper)queryWrapper).select(columnName);
            }
        }
        else if(queryWrapper instanceof LambdaQueryWrapper){
            if(V.isEmpty(queryWrapper.getSqlSelect())){
                ((LambdaQueryWrapper) queryWrapper).select(getterFn);
            }
        }
        List<T> entityList = getEntityList(queryWrapper);
        if(V.isEmpty(entityList)){
            return Collections.emptyList();
        }
        List<FT> fldValues = new ArrayList<>(entityList.size());
        for(T entity : entityList){
            FT value = (FT)BeanUtils.getProperty(entity, fieldName);
            if(value != null && !fldValues.contains(value)){
                fldValues.add(value);
            }
        }
        return fldValues;
    }

    @Override
    public List<T> getEntityListLimit(Wrapper queryWrapper, int limitCount) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, limitCount);
        page.setSearchCount(false);
        page = super.page(page, queryWrapper);
        return page.getRecords();
    }

    @Override
    public T getSingleEntity(Wrapper queryWrapper) {
        List<T> entityList = getEntityListLimit(queryWrapper, 1);
        if(V.notEmpty(entityList)){
            return entityList.get(0);
        }
        return null;
    }

    @Override
    public boolean exists(IGetter<T> getterFn, Object value) {
        QueryWrapper<T> queryWrapper = new QueryWrapper();
        String field = BeanUtils.convertToFieldName(getterFn);
        String column = S.toSnakeCase(field);
        queryWrapper.select(column).eq(column, value);
        return exists(queryWrapper);
    }

    @Override
    public boolean exists(Wrapper queryWrapper) {
        List<T> entityList = getEntityListLimit(queryWrapper, 1);
        boolean isExists = V.notEmpty(entityList) && entityList.size() > 0;
        entityList = null;
        return isExists;
    }

    @Override
    public List<T> getEntityListByIds(List ids) {
        QueryWrapper<T> queryWrapper = new QueryWrapper();
        queryWrapper.in(Cons.FieldName.id.name(), ids);
        return getEntityList(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getMapList(Wrapper queryWrapper) {
        return getMapList(queryWrapper, null);
    }

    @Override
    public List<Map<String, Object>> getMapList(Wrapper queryWrapper, Pagination pagination) {
        if(pagination != null){
            IPage page = convertToIPage(queryWrapper, pagination);
            IPage<Map<String, Object>> resultPage = super.pageMaps(page, queryWrapper);
            // 如果重新执行了count进行查询，则更新pagination中的总数
            if(page.isSearchCount()){
                pagination.setTotalCount(page.getTotal());
            }
            return resultPage.getRecords();
        }
        else{
            List<Map<String, Object>> list = super.listMaps(queryWrapper);
            if(list == null){
                list = Collections.emptyList();
            }
            else if(list.size() > BaseConfig.getBatchSize()){
                log.warn("单次查询记录数量过大，请及时检查优化。返回结果数={}", list.size());
            }
            return list;
        }
    }

    @Override
    public List<KeyValue> getKeyValueList(Wrapper queryWrapper) {
        String sqlSelect = queryWrapper.getSqlSelect();
        // 最多支持3个属性：k, v, ext
        if(V.isEmpty(sqlSelect) || S.countMatches(sqlSelect, Cons.SEPARATOR_COMMA) > 2){
            log.error("调用错误: getKeyValueList必须用select依次指定返回的Key,Value, ext键值字段，如: new QueryWrapper<Dictionary>().lambda().select(Dictionary::getItemName, Dictionary::getItemValue)");
            return Collections.emptyList();
        }
        // 获取mapList
        List<Map<String, Object>> mapList = super.listMaps(queryWrapper);
        if(mapList == null){
            return Collections.emptyList();
        }
        else if(mapList.size() > BaseConfig.getBatchSize()){
            log.warn("单次查询记录数量过大，建议您及时检查优化。返回结果数={}", mapList.size());
        }
        // 转换为Key-Value键值对
        String[] keyValueArray = sqlSelect.split(Cons.SEPARATOR_COMMA);
        List<KeyValue> keyValueList = new ArrayList<>(mapList.size());
        for(Map<String, Object> map : mapList){
            // 如果key和value的的值都为null的时候map也为空，则不处理此项
            if (V.isEmpty(map)) {
                continue;
            }
            String key = keyValueArray[0], value = keyValueArray[1], ext = null;
            // 兼容oracle大写
            if(map.containsKey(key) == false && map.containsKey(key.toUpperCase())){
                key = key.toUpperCase();
            }
            if(map.containsKey(value) == false && map.containsKey(value.toUpperCase())){
                value = value.toUpperCase();
            }
            if(map.containsKey(key)){
                KeyValue kv = new KeyValue(S.valueOf(map.get(key)), map.get(value));
                if(keyValueArray.length > 2){
                    ext = keyValueArray[2];
                    if(map.containsKey(ext) == false && map.containsKey(ext.toUpperCase())){
                        ext = ext.toUpperCase();
                    }
                    kv.setExt(map.get(ext));
                }
                keyValueList.add(kv);
            }
        }
        return keyValueList;
    }

    @Override
    public Map<String, Object> getKeyValueMap(Wrapper queryWrapper) {
        List<KeyValue> keyValueList = getKeyValueList(queryWrapper);
        return BeanUtils.convertKeyValueList2Map(keyValueList);
    }

    @Override
    public FieldBinder<T> bindingFieldTo(List voList){
        return new FieldBinder<>(this, voList);
    }

    @Override
    public EntityBinder<T> bindingEntityTo(List voList){
        return new EntityBinder<>(this, voList);
    }

    @Override
    public EntityListBinder<T> bindingEntityListTo(List voList){
        return new EntityListBinder<>(this, voList);
    }

    /**
     * 获取View Object对象
     * @param id 主键
     * @return 结果 entity
     */
    @Override
    public <VO> VO getViewObject(Serializable id, Class<VO> voClass){
        T entity = getEntity(id);
        if(entity == null){
            return null;
        }
        // 绑定
        return Binder.convertAndBindRelations(entity, voClass);
    }

    @Override
    public <VO> List<VO> getViewObjectList(Wrapper queryWrapper, Pagination pagination, Class<VO> voClass) {
        List<T> entityList = getEntityList(queryWrapper, pagination);
        // 自动转换为VO并绑定关联对象
        List<VO> voList = Binder.convertAndBindRelations(entityList, voClass);
        return voList;
    }

    /***
     * 转换为IPage
     * @param queryWrapper 查询条件
     * @param pagination 分页
     * @return 结果 page
     */
    protected Page<T> convertToIPage(Wrapper queryWrapper, Pagination pagination){
        return ServiceAdaptor.convertToIPage(pagination, entityClass);
    }

    /**
     * 获取主键值
     * @param entity entity
     * @return 结果 o
     */
    private Object getPrimaryKeyValue(Object entity){
        String pk = ContextHelper.getPrimaryKey(entity.getClass());
        return BeanUtils.getProperty(entity, pk);
    }

    /**
     * 转换SFunction为属性名
     * @param getterFn f
     * @param <T> t
     * @return 结果
     */
    private <T> String convertGetterToFieldName(SFunction<T, ?> getterFn) {
        com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda lambda = LambdaUtils.resolve(getterFn);
        String fieldName = PropertyNamer.methodToProperty(lambda.getImplMethodName());
        return fieldName;
    }

    /***
     * 打印警告信息
     * @param method s
     * @param message s
     */
    private void warning(String method, String message){
        log.warn(this.getClass().getSimpleName() + ".{} 调用错误: {}, 请检查！", method, message);
    }
}
