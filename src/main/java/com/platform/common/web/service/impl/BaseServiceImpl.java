/**
 * Licensed to the Apache Software Foundation （ASF） under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * （the "License"）； you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.platform.common.web.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import com.platform.common.exception.BaseException;
import com.platform.common.utils.bean.BeanCopyUtils;
import com.platform.common.web.dao.BaseDao;
import com.platform.common.web.service.BaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 基础service实现层基类
 */
public class BaseServiceImpl<T> implements BaseService<T> {

    protected final static String EXIST_MSG = "数据不存在";

    private BaseDao<T> baseDao;

    public void setBaseDao(BaseDao<T> baseDao) {
        this.baseDao = baseDao;
    }

    @Override
    public Integer add(T entity) {
        return baseDao.insert(entity);
    }

    @Override
    public Integer deleteById(Long id) {
        return baseDao.deleteById(id);
    }

    @Override
    public Integer deleteByIds(Long[] ids) {
        return baseDao.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public Integer deleteByIds(Collection<Long> ids) {
        return baseDao.deleteBatchIds(ids);
    }

    @Override
    public int delete(Wrapper<T> wrapper) {
        return baseDao.delete(wrapper);
    }

    @Override
    public Integer updateById(T entity) {
        return baseDao.updateById(entity);
    }

    @Override
    public void update(T t, String... param) {
        if (param.length == 0) {
            this.updateById(BeanCopyUtils.include(t, "id", "status"));
        } else {
            this.updateById(BeanCopyUtils.include(t, param));
        }
    }

    /**
     * eq: Wrapper wrapper = Wrappers.<ChatFriend>lambdaUpdate().set(ChatFriend::getRemark, null).eq(ChatFriend::getId, friend.getId());
     */
    @Override
    public Integer update(Wrapper wrapper) {
        return baseDao.update(null, wrapper);
    }

    @Override
    public T getById(Long id) {
        return baseDao.selectById(id);
    }

    @Override
    public T findById(Long id) {
        return findById(id, EXIST_MSG);
    }

    @Override
    public T findById(Long id, String msg) {
        T t = getById(id);
        if (t == null) {
            throw new BaseException(msg);
        }
        return t;
    }

    @Override
    public List<T> getByIds(Collection<? extends Serializable> idList) {
        return baseDao.selectBatchIds(idList);
    }

    @Override
    public Long queryCount(T t) {
        Wrapper<T> wrapper = new QueryWrapper<>(t);
        return queryCount(wrapper);
    }

    @Override
    public Long queryCount(Wrapper<T> wrapper) {
        return baseDao.selectCount(wrapper).longValue();
    }

    @Override
    public List<T> queryList(T t) {
        Wrapper<T> wrapper = new QueryWrapper<>(t);
        return queryList(wrapper);
    }

    @Override
    public List<T> queryList(Wrapper<T> wrapper) {
        return baseDao.selectList(wrapper);
    }

    @Override
    public T queryOne(T t) {
        Wrapper<T> wrapper = new QueryWrapper<>(t);
        return queryOne(wrapper);
    }

    @Override
    public T queryOne(Wrapper<T> wrapper) {
        List<T> dataList = queryList(wrapper);
        if (CollectionUtils.isEmpty(dataList)) {
            return null;
        }
        return dataList.get(0);
    }

    @Transactional
    @Override
    public Integer batchAdd(List<T> dataList, Integer... batchCount) {
        if (CollectionUtils.isEmpty(dataList)) {
            return 0;
        }
        Integer batch = 1000;
        if (batchCount.length != 0) {
            batch = batchCount[0];
        }
        List<List<T>> resultList = this.batch(dataList, batch);
        AtomicReference<Integer> result = new AtomicReference<>(0);
        resultList.forEach(batchList -> {
            result.updateAndGet(v -> v + baseDao.insertBatchSomeColumn(batchList));
        });
        return result.get();
    }

    /**
     * 循环工具类
     */
    public static <T> Consumer<T> forEach(BiConsumer<T, Integer> consumer) {
        class Obj {
            int i;
        }
        Obj obj = new Obj();
        return t -> {
            int index = obj.i++;
            consumer.accept(t, index);
        };
    }

    /**
     * 批量
     */
    @Override
    public List<List<T>> batch(List<T> dataList, Integer batchCount) {
        if (CollectionUtils.isEmpty(dataList)) {
            return new ArrayList<>();
        }
        List<List<T>> resultList = new ArrayList<>();
        List<T> batchList = new ArrayList<>();
        dataList.forEach((data) -> {
            batchList.add(data);
            if (batchCount == batchList.size()) {
                resultList.add(ListUtil.toList(batchList));
                batchList.clear();
            }
        });
        if (!CollectionUtils.isEmpty(batchList)) {
            resultList.add(ListUtil.toList(batchList));
            batchList.clear();
        }
        return resultList;
    }

    /**
     * 响应请求分页数据
     */
    protected PageInfo getPageInfo(List<?> list, List<?> oldList) {
        Long total = new PageInfo(oldList).getTotal();
        return getPageInfo(list, total);
    }

    /**
     * 格式化分页
     */
    public PageInfo getPageInfo(List<?> list, long total) {
        PageInfo pageInfo = new PageInfo(list);
        pageInfo.setTotal(total);
        return pageInfo;
    }

}
