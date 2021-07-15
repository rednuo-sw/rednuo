package com.rednuo.core.utils;


import java.util.List;
import java.util.stream.Collectors;

/**
 * List 操作
 * @author  nz.zou 2021/6/9
 * @since rednuo 1.0.0
 */
public class L{
    /**
     * 方法功能说明：@1.list中是否有完全相同的元素? 如果有 返回true, 如果没有返回false;
     */
    public static Boolean checkRepeat(List<?> list) {
        boolean flag = false;
        for (int i = 0; i < list.size(); i++) {
            for (int j = i; j < list.size(); j++) {
                if (i!=j && list.get(i).equals(list.get(j))) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                return flag;
            }
        }
        return false;
    }

    /**
     * 使用java8新特性stream实现List去重(有序)
     * */
    public static List removeDuplicationByStream(List<?> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }
}
