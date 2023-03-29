package com.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.data.entity.BaseXml;
import com.data.mapper.BaseXmlMapper;
import com.data.service.ReceiptService;
import com.data.util.PublicUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service("receiptService")
@Slf4j
@Transactional(rollbackFor = {Exception.class})
public class ReceiptServiceImpl implements ReceiptService {

    @Value("${msgResultDir}")
    private String msgResultDir;

    @Value("${msgResultDir2}")
    private String msgResultDir2;

    @Autowired(required = false)
    private BaseXmlMapper baseXmlMapper;

    private String[] receipt1 = new String[]{"MT1101", "MT2101", "MT4101", "MT8104", "MT8105", "MTECIM", "MTECEX", "MT9999"};

    private String[] receipt2 = new String[]{"MT3101", "MT3201", "MT5101", "MT5102", "MT5201", "MT5202"};


    @Override
    @Async("taskExecutor")
    public void issue(List<File> fileList, CountDownLatch latch) throws IOException, DocumentException {
        for (File file : fileList) {
            try {
                String fileName = file.getName();
                if (fileName.contains(".temp")) {
                    continue;
                }
                if (Arrays.stream(receipt1).anyMatch(fileName::contains)) {
                    FileUtils.moveFile(file, new File(msgResultDir + fileName));
                    continue;
                }

                if (Arrays.stream(receipt2).anyMatch(fileName::contains)) {
                    FileUtils.moveFile(file, new File(msgResultDir2 + fileName));
                    continue;
                }
                // 读取xml文件内容
                String xmlStr = PublicUtil.readFile(file);
                if (Arrays.stream(receipt1).anyMatch(xmlStr::contains)) {
                    FileUtils.moveFile(file, new File(msgResultDir + fileName));
                    continue;
                }

                if (Arrays.stream(receipt2).anyMatch(xmlStr::contains)) {
                    FileUtils.moveFile(file, new File(msgResultDir2 + fileName));
                    continue;
                }

                Document xmlDocument = DocumentHelper.parseText(xmlStr);
                Element element = xmlDocument.getRootElement();
                String messageId = null;
                if (element.elements("Head").size() == 1) {
                    messageId = element.element("Head").elementTextTrim("MessageID");
                } else {
                    messageId = element.element("RetBizData").element("Manifest").element("Head").elementTextTrim("MessageID");
                }

                QueryWrapper<BaseXml> baseWrapper = new QueryWrapper<>();
                baseWrapper.eq("MESSAGE_ID", messageId);
                List<BaseXml> list = baseXmlMapper.selectList(baseWrapper);
                if (list != null && list.size() > 0) {
                    FileUtils.moveFile(file, new File(msgResultDir + fileName));
                } else {
                    FileUtils.moveFile(file, new File(msgResultDir2 + fileName));
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("{}文件处理错误", file.getName());
            }

        }
        latch.countDown();
    }
}
