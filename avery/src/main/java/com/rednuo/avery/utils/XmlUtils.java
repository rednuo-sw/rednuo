package com.rednuo.avery.utils;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;

/**
 * @author  rednuo 2021/2/4
 */
public class XmlUtils {

    public static List<Element> readXml(String path) throws DocumentException {
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        Element root = document.getRootElement();
        List<Element> childElements = root.elements();
        return childElements;
    }
    public static String readXmlSingleValue(String path, String attribute, String attName, String childElement) throws DocumentException {
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        List<Element> elements = readXml(path);
        String result=null;
        for (Element element: elements
        ) {
            if(element.attributeValue(attribute).equals(attName)){
                result = element.elementText(childElement);
                break;
            }
        }
        return result;
    }


//    @Test
//    public void textXml() throws DocumentException, IOException {
//
//        Document doc = DocumentHelper.createDocument();
//        //增加根节点
//        Element books = doc.addElement("books");
//        //增加子元素
//        Element book1 = books.addElement("book");
//        Element title1 = book1.addElement("title");
//        Element author1 = book1.addElement("author");
//
//        Element book2 = books.addElement("book");
//        Element title2 = book2.addElement("title");
//        Element author2 = book2.addElement("author");
//
//        //为子节点添加属性
//        book1.addAttribute("id", "001");
//        //为元素添加内容
//        title1.setText("Harry Potter");
//        author1.setText("J K. Rowling");
//
//        book2.addAttribute("id", "002");
//        title2.setText("Learning XML");
//        author2.setText("Erik T. Ray");
//
//        //实例化输出格式对象
//        OutputFormat format = OutputFormat.createPrettyPrint();
//        //设置输出编码
//        format.setEncoding("UTF-8");
//        //创建需要写入的File对象
//        File file = new File( "books.xml");
//        //生成XMLWriter对象，构造函数中的参数为需要输出的文件流和格式
//        XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
//        //开始写入，write方法中包含上面创建的Document对象
//        writer.write(doc);
//        //
//        List<Element> elements = XmlUtils.readXml( "books.xml");
//        for (Element element:elements
//        ) {
//            //未知属性名情况下
//            List<Attribute> attributeList = element.attributes();
//            for (Attribute attr : attributeList) {
//                System.out.println(attr.getName() + ": " + attr.getValue());
//            }
//            //未知子元素名情况下
//            List<Element> elementList = element.elements();
//            for (Element ele : elementList) {
//                System.out.println(ele.getName() + ": " + ele.getText());
//            }
//            System.out.println();
//
//            //已知属性名情况下
//            System.out.println("id: " + element.attributeValue("id"));
//            //已知子元素名的情况下
//            System.out.println("title" + element.elementText("title"));
//            System.out.println("author" + element.elementText("author"));
//
//            System.out.println();
//        }
//    }
}
