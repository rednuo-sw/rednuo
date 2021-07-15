//package com.rednuo.core.interceptor;
//
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Fegin 拦截器,  获得请求头的参数,处理
// * 调用方法
// *
// * @FeignClient(value = "${feign.client.config.house-asset-api.name}",
// *     url = "${feign.client.config.house-asset-api.url}",
// *     configuration = {FeignConfiguration.class})
// * public interface AssetEcodeApi {
// * }
// * @author rednuo 2021/4/28
// */
//public class FeignClientInterceptor implements RequestInterceptor {
//    @Override
//    public void apply(RequestTemplate requestTemplate) {
//        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        if(requestAttributes != null){
//            HttpServletRequest request = requestAttributes.getRequest();
//            //取出当前请求的header,找到jwt令牌
//            Enumeration<String> headerNames = request.getHeaderNames();
//            if(headerNames != null){
//                while (headerNames.hasMoreElements()){
//                    String headerName = headerNames.nextElement();
//                    String headerValue = request.getHeader(headerName);
//                    //将header向下传递
//                    requestTemplate.header(headerName,headerValue);
//                    //TODO: 处理拦截下来的参数吧
//                }
//            }
//            // 设置param属性：requestTemplate.query(name, values);
//            Enumeration<String> paramNames = request.getParameterNames();
//            if (paramNames != null) {
//                Map map=new HashMap();
//                while (paramNames.hasMoreElements()) {
//                    String name = paramNames.nextElement();
//                    String values = request.getParameter(name);
//                    requestTemplate.query(name, values);
//                }
//            }
//        }
//    }
//}
