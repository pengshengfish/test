package com.data.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class PublicUtil {

    private static final Pattern P = Pattern.compile(">(\\s*|\n|\t|\r)<");


    /**
     * @Description: 获取目录下的所有文件
     * @Date: 2022/9/8  14:54
     */
    public static List<List<File>> getFileList(String dir) {
        File f = new File(dir);

        // 获取目录下的所有文件
        File[] filePaths = f.listFiles();
        if (filePaths == null) {
            return null;
        }

        // 最多处理1200个文件
        final List<File> filePathsList = new ArrayList<File>();
        int maxFileLen = filePaths.length >= 3600 ? 3600 : filePaths.length;
        for (int i = 0; i < maxFileLen; i++) {
            filePathsList.add(filePaths[i]);
        }

        List<List<File>> splitList = splitList(filePathsList, 1200);
        return splitList;
    }

    /**
     * Description: Java8 Stream分割list集合
     *
     * @param list      集合数据
     * @param splitSize 几个分割一组
     * @return 集合分割后的集合
     */
    private static <T> List<List<T>> splitList(List<T> list, int splitSize) {
        //计算分割后的大小
        int maxSize = (list.size() + splitSize - 1) / splitSize;
        //开始分割
        return Stream.iterate(0, n -> n + 1)
                .limit(maxSize)
                .parallel()
                .map(a -> list.parallelStream().skip(a * splitSize).limit(splitSize).collect(Collectors.toList()))
                .filter(b -> !b.isEmpty())
                .collect(Collectors.toList());
    }


    /**
     * 　* @Description: 读取文件内容
     * 　* @Author: ps
     * 　* @Date: 2022/6/4 0004 21:44
     */
    public static String readFile(File file) {
        String str = null;
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel();) {
            ByteBuffer bb = ByteBuffer.allocate(new Long(file.length()).intValue());
            //fc向buffer中读入数据
            fc.read(bb);
            bb.flip();
            str = new String(bb.array(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{}读取文件内容失败{}", file.getName(), e.getMessage());
        }
        return str;
    }


    /**
     * 删除文件或文件夹
     *
     * @param fileName 文件名
     * @return 删除成功返回true, 失败返回false
     */
    public static boolean deleteFileOrDirectory(String fileName) {
        File file = new File(fileName);  // fileName是路径或者file.getPath()获取的文件路径
        if (file.exists()) {
            if (file.isFile()) {
                return deleteFile(fileName);  // 是文件，调用删除文件的方法
            } else {
                return deleteDirectory(fileName);  // 是文件夹，调用删除文件夹的方法
            }
        } else {
            System.out.println("文件或文件夹删除失败：" + fileName);
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     * @return 删除成功返回true, 失败返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            file.delete();
            System.out.println("删除文件成功：" + fileName);
            return true;
        } else {
            System.out.println("删除文件失败：" + fileName);
            return false;
        }
    }

    /**
     * 删除文件夹
     * 删除文件夹需要把包含的文件及文件夹先删除，才能成功
     *
     * @param directory 文件夹名
     * @return 删除成功返回true, 失败返回false
     */
    public static boolean deleteDirectory(String directory) {
        // directory不以文件分隔符（/或\）结尾时，自动添加文件分隔符，不同系统下File.separator方法会自动添加相应的分隔符
        if (!directory.endsWith(File.separator)) {
            directory = directory + File.separator;
        }
        File directoryFile = new File(directory);
        // 判断directory对应的文件是否存在，或者是否是一个文件夹
        if (!directoryFile.exists() || !directoryFile.isDirectory()) {
            System.out.println("文件夹删除失败，文件夹不存在" + directory);
            return false;
        }
        boolean flag = true;
        // 删除文件夹下的所有文件和文件夹
        File[] files = directoryFile.listFiles();
        for (int i = 0; i < files.length; i++) {  // 循环删除所有的子文件及子文件夹
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {  // 删除子文件夹
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            System.out.println("删除失败");
            return false;
        }
        // 最后删除当前文件夹
        if (directoryFile.delete()) {
            System.out.println("删除成功：" + directory);
            return true;
        } else {
            System.out.println("删除失败：" + directory);
            return false;
        }
    }


    /**
     * @Author: ps
     * @Description: 迁移文件
     * @Date: Created in 2021/9/23 14:13
     * @params: [oldFile, bakPath, flag]
     * @return: void
     */
    public static String moveFile(Object t, String bakPath, String flag) {
        String newPath = bakPath + File.separator + flag + File.separator + LocalDateTime.now().format(DateTimeFormatter
                .ofPattern("yyyyMMdd"));
        String newFilePath = null;
        File directory = new File(newPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (t instanceof File) {
            File oldFile = (File) t;
            try {
                newFilePath = newPath + File.separator + oldFile.getName();
                File file = FileUtils.getFile(newFilePath);
                if (file.exists()) {
                    file.delete();
                }
                FileUtils.moveFile(oldFile, new File(newFilePath));
            } catch (Exception e) {
                log.error("迁移文件失败：", oldFile.getName(), e);
                return null;
            }
        }
        return newFilePath;
    }


    /**
     * @Description: 创建文件并写入
     * @Author: xuyang
     * @Date: 2022/9/8  13:09
     */
    public static void createFile(String fileName, String fileContent) throws IOException {
        File file = new File(fileName);
        String pfile = file.getParent();
        File pdir = new File(pfile);
        if (!pdir.exists()) {
            pdir.mkdirs();
            log.info("{}", "创建：" + pfile);
        }
        Path path = Paths.get(fileName);
        //使用newBufferedWriter创建文件并写入文件
        //使用try无需手动关闭
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(fileContent);
        }
    }


    /**
     * @Description: xml转Map（适用于节点中没有属性的情况）
     * @Author: xuyang
     * @Date: 2022/9/8  15:08
     */
    public static Map<String, Object> xmlToMap(String xmlStr) throws DocumentException {
        Map map = new ListOrderedMap();
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new ByteArrayInputStream(xmlStr.getBytes("UTF-8")));//xml串第一行不能有空格，否则报错
            Element root = document.getRootElement();//得到xml文档根节点元素，即最上层的"<xml>"
            elementTomap(root, map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return map;
    }


    /**
     * @Description: 根据节点转map
     * @Author: xuyang
     * @Date: 2022/9/9  9:35
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> elementTomap(Element outele, Map<String, Object> outmap) {
        List<Element> list = outele.elements();
        int size = list.size();
        if (size == 0) {
            outmap.put(outele.getName(), outele.getTextTrim());
        } else {
            Map<String, Object> innermap = new ListOrderedMap();
            int i = 1;

            for (Element ele1 : list) {
                String eleName = ele1.getName();

                String value = ele1.getText();
                Object obj = innermap.get(eleName);
                if (obj == null) {
                    elementTomap(ele1, innermap);
                } else {
                    if (obj instanceof java.util.Map) {
                        List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
                        list1.add((Map<String, Object>) innermap.remove(eleName));
                        elementTomap(ele1, innermap);
                        list1.add((Map<String, Object>) innermap.remove(eleName));
                        innermap.put(eleName, list1);
                    } else if (obj instanceof String) {

                        innermap.put(eleName + i, value);
                        i++;
                    } else {
                        elementTomap(ele1, innermap);
                        Map<String, Object> listValue = (Map<String, Object>) innermap.get(eleName);
                        ((List<Map<String, Object>>) obj).add(listValue);
                        innermap.put(eleName, obj);
                    }

                }
            }
            outmap.put(outele.getName(), innermap);
        }
        return outmap;
    }


    /**
     * @return
     * @Description: 格式化XML
     * @Date: 2022/9/7  16:27
     */
    public static String formatXML(String xml) {
        String requestXML = null;
        try {
            // 拿取解析器
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(xml));
            if (null != document) {
                StringWriter stringWriter = new StringWriter();
                // 格式化,每一级前的空格
                OutputFormat format = new OutputFormat("    ", true);
                // xml声明与内容是否添加空行
                format.setNewLineAfterDeclaration(false);
                // 是否设置xml声明头部
                format.setSuppressDeclaration(false);
                // 是否分行
                format.setNewlines(true);
                format.setEncoding("utf-8");
                StandaloneWriter writer = new StandaloneWriter(stringWriter, format);
                writer.write(document);
                writer.flush();
                writer.close();
                requestXML = stringWriter.getBuffer().toString();
            }
            return requestXML;
        } catch (Exception e) {
            System.out.println("格式化xml，失败 --> {}" + e);
            return null;
        }
    }


    /**
     * @Description: map转xml（适用于节点中没有属性的情况）
     * @Author: xuyang
     * @Date: 2022/9/9  9:36
     */
    public static String parseMap(Map<?, ?> map, StringBuffer sb) {
        Set<?> set = map.keySet();
        for (Iterator<?> it = set.iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            Object value = map.get(key);
            if (value instanceof ListOrderedMap) {
                sb.append("<" + key + ">");
                parseMap((ListOrderedMap) value, sb);
                sb.append("</" + key + ">");
            } else if (value instanceof ArrayList) {
                List<?> list = (ArrayList<?>) map.get(key);
                for (int i = 0; i < list.size(); i++) {
                    sb.append("<" + key + ">");
                    Map<?, ?> hm = (ListOrderedMap) list.get(i);
                    parseMap(hm, sb);
                    sb.append("</" + key + ">");
                }
            } else {
                sb.append("<" + key + ">" + value + "</" + key + ">");
            }
        }
        return sb.toString();
    }


    /**
     * 获取XML指定节点内容
     *
     * @param xml      xml内容
     * @param attrName 节点名称（例如：TranData.OutputData.Result）
     * @return
     * @throws Exception
     */
    public static String getXmlAttrValue(String xml, String attrName) throws Exception {
        if (null == xml || "".equals(xml) || null == attrName || "".equals(attrName)) {
            return null;
        }
        String[] attrs = attrName.split("\\.");
        int length = attrs.length;
        String result = null;
        // 将xml格式字符串转化为DOM对象
        Document document = DocumentHelper.parseText(xml);
        // 获取根结点对象
        Element element = document.getRootElement();
        List<Element> elements = Arrays.asList(element);
        for (int i = 0; i < length; i++) {
            Map<String, Element> elementMap = elements.stream().collect(Collectors.toMap(e -> e.getName(), e -> e));
            if (elementMap.containsKey(attrs[i])) {
                if (i == length - 1) {
                    result = elementMap.get(attrs[i]).asXML();
                    break;
                } else {
                    elements = elementMap.get(attrs[i]).elements();
                }
            } else {
                throw new Exception("Node does not exist:" + attrName);
            }
        }
        return result;
    }


    /**
     * @Description: 格式化xml、美化xml、加xml头信息
     * @Author: xuyang
     * @Date: 2022/9/14  11:02
     */
    public static String beautifyXml(String xml) {
        xml = xml.replaceAll("\r", "").replaceAll("\n", "");
        //去掉所有的换行符 空格 制表符  用><代替>    <,去除中间的空格
        Matcher m = P.matcher(xml);
        xml = m.replaceAll("><");
        xml = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>" + xml;
        xml = formatXML(xml);
        return xml;
    }


    /**
     * 截取两字符之间的字符串，str 和 start不能为null或""
     */
    public static String getCutOutString(String str, String start, String endwith) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        String result = "";
        if (str.contains(start)) {
            String s1 = str.split(start)[1];
            if (endwith == null || "".equals(endwith)) {
                result = s1;
            } else {
                String s2[] = s1.split(endwith);
                if (s2 != null) {
                    result = s2[0];
                }
            }
        }
        return result;

    }


}



