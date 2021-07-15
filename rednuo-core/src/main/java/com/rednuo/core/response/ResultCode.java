package com.rednuo.core.response;

/**
 * 响应操作提示信息
 * 10000 -- 通用OK Code
 * 所有的服务响应文件都继承这个借口这个.
 * @author  rednuo 2021/4/28
 */
public interface ResultCode {
    /**
     * 操作是否成功,true为成功,false操作失败
     * @return 结果 true/false
     */
    boolean isSuccess();

    /**
     * 操作代码
     * @return 结果 操作编号
     */
    int code();

    /**
     * 提示信息
     * @return 结果 字符串
     */
    String message();

    /**
     *
     */
    default long timestamp(){
        return System.currentTimeMillis();
    };
}
