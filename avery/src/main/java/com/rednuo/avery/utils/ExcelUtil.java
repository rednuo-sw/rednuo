package com.rednuo.avery.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.rednuo.core.exception.CoreCode;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.utils.BeanUtils;
import com.rednuo.core.utils.V;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
/**
 * @author  nz.zou 2021/7/13
 * @since avery 1.0.0
 */
@Slf4j
public class ExcelUtil {
    private ExcelUtil(){
    }

    private static String SHEET_NAME = "sheet1";
    /**
     * 读取Excel（一个sheet）
     * @param excel 文件
     * @param clazz 实体类
     * @param sheetNo sheet序号
     * @return 结果 结果 返回实体列表(需转换)
     */
    public static <T> List<T> readExcel(MultipartFile excel, Class<T> clazz,int sheetNo) {

        ExcelListener excelListener = new ExcelListener();

        ExcelReader excelReader = getReader(excel,clazz,excelListener);
        if (excelReader == null) {
            return new ArrayList<>();
        }

        ReadSheet readSheet = EasyExcel.readSheet(sheetNo).build();
        excelReader.read(readSheet);
        excelReader.finish();

        return BeanUtils.convertList(excelListener.getDataList(), clazz);
    }


    /**
     * 读取Excel（多个sheet可以用同一个实体类解析）
     * @param excel 文件
     * @param clazz 实体类
     * @return 结果 结果 返回实体列表(需转换)
     */
    public static <T> List<T> readExcel(MultipartFile excel, Class<T> clazz) {

        ExcelListener excelListener = new ExcelListener();
        ExcelReader excelReader = getReader(excel,clazz,excelListener);

        if (excelReader == null) {
            return new ArrayList<>();
        }

        List<ReadSheet> readSheetList = excelReader.excelExecutor().sheetList();

        for (ReadSheet readSheet:readSheetList){
            excelReader.read(readSheet);
        }
        excelReader.finish();

        return BeanUtils.convertList(excelListener.getDataList(), clazz);
    }


    /**
     * 导出Excel(一个sheet)
     *
     * @param response  HttpServletResponse
     * @param list      数据list
     * @param fileName  导出的文件名
     * @param sheetName 导入文件的sheet名
     * @param clazz 实体类
     */
    public static <T> void  writeExcel(HttpServletResponse response, List<T> list, String fileName, String sheetName, Class<T> clazz) {

        OutputStream outputStream = getOutputStream(response, fileName);

        ExcelWriter excelWriter = EasyExcel.write(outputStream, clazz).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

        excelWriter.write(list, writeSheet);

        excelWriter.finish();
    }


    /**
     * 导出Excel(带样式)
     *
     */
    public static  <T> void writeStyleExcel(HttpServletResponse response,List<T> list, String fileName, String sheetName, Class<T> clazz) {
        //表头策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //背景浅灰
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short)20);
        headWriteCellStyle.setWriteFont(headWriteFont);

        //内容策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //这里需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 否则无法显示背景颜色；头默认了FillPatternType
        contentWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        //背景浅绿
        contentWriteCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        WriteFont contentWriteFont = new WriteFont();
        //字体大小
        contentWriteFont.setFontHeightInPoints((short)15);
        contentWriteCellStyle.setWriteFont(contentWriteFont);

        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

        OutputStream outputStream = getOutputStream(response, fileName);
        EasyExcel.write(outputStream, clazz).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(list);

    }


    /**
     * 导出Excel(动态表头)
     * write时不传入class,table时传入并设置needHead为false
     */
    public static  <T> void writeDynamicHeadExcel(HttpServletResponse response,List<T> list, String fileName, String sheetName, Class<T> clazz,List<List<String>> headList) {

        OutputStream outputStream = getOutputStream(response, fileName);

        EasyExcel.write(outputStream)
                .head(headList)
                .sheet(sheetName)
                .table().head(clazz).needHead(Boolean.FALSE)
                .doWrite(list);
    }


    /**
     * 导出时生成OutputStream
     */
    private static OutputStream getOutputStream(HttpServletResponse response,String fileName) {
        //创建本地文件
        String filePath = fileName + ".xlsx";
        File file = new File(filePath);
        try {
            if (!file.exists() || file.isDirectory()) {
                file.createNewFile();
            }
            fileName = new String(filePath.getBytes(), "ISO-8859-1");
            response.addHeader("Content-Disposition", "filename=" + fileName);
            return response.getOutputStream();
        } catch (IOException  e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回ExcelReader
     * @param excel         文件
     * @param clazz         实体类
     * @param excelListener l
     */
    private static <T> ExcelReader getReader(MultipartFile excel, Class<T> clazz, ExcelListener excelListener) {
        String filename = excel.getOriginalFilename();

        try {
            if (filename == null || (!filename.toLowerCase().endsWith(".xls") && !filename.toLowerCase().endsWith(".xlsx"))) {
                return null;
            }

            InputStream inputStream = new BufferedInputStream(excel.getInputStream());

            ExcelReader excelReader = EasyExcel.read(inputStream, clazz, excelListener).build();

            inputStream.close();

            return excelReader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存Excel 到本地
     * @param filePath path
     * @param data data
     * @param clazz clazz
     */
    public static void writeToLocal(String filePath, List<?> data, Class<?> clazz) throws IOException {
        writeToLocal(filePath, data, clazz,null);
    }

    public static void writeToLocal(String filePath, List<?> data, Class<?> clazz, String sheet) throws IOException {
        if(V.isEmpty(data)){
            return;
        }
        WriteSheet writeSheet = null;
        if(V.isNull(sheet)){
            writeSheet = EasyExcel.writerSheet(SHEET_NAME).build();
        } else {
            writeSheet = EasyExcel.writerSheet(sheet).build();
        }
        OutputStream outputStream = null;
        ExcelWriter writer = null;
        try {
            outputStream = new FileOutputStream(createFile(filePath));
            // 覆盖了
            writer = EasyExcel.write(outputStream, clazz).build();
            writer.write(data, writeSheet);
            writer.finish();
            outputStream.close();
        } catch (FileNotFoundException e) {
            if(writer != null){
                writer.finish();
            }
            if(outputStream != null){
                outputStream.close();
            }
            ExceptionCast.cast(CoreCode.FAIL,"找不到文件或文件路径错误, 文件：" + filePath);
        }
    }

    /**
     *
     */
    private static File createFile(String filePath) throws IOException {
        return createFile(filePath,false);
    }
    /**
     * 创建文件
     * @param filePath 文件路径
     * @param append 文件存在是否删除
     */
    private static File createFile(String filePath,boolean append) throws IOException {
        File file = new File(filePath);
        if(append){
            if(file.exists()){
                file.delete();
            }
        }
        if(!file.exists()){
            File parent = file.getParentFile();
            if(parent != null && !parent.exists()){
                parent.mkdirs();
            }
            file.createNewFile();
        }
        return file;
    }
}

