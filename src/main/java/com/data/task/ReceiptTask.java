package com.data.task;

import com.data.service.ReceiptService;
import com.data.util.PublicUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Description: 回执分发定时任务
 * @Author: ps
 * @Date: 2022/10/10 001015:52
 */
@Component
@Slf4j
public class ReceiptTask {

    @Value("${singleWindowReceiptDir}")
    private String singleWindowReceiptDir;

    @Autowired
    private ReceiptService receiptService;

    /**
     * 　* @Description: 原始预配和运抵理货回执分发到不同目录
     * 　* @Author: ps
     * 　* @Date: 2022/10/10 0010 15:53
     *
     */
//    @Scheduled(fixedDelay = 30000)
    public void receiptSend() {
        // 获取文件集
        List<List<File>> splitList = PublicUtil.getFileList(singleWindowReceiptDir);
        if (null == splitList || splitList.size() == 0) {
            return;
        }

        try {
            CountDownLatch latch = new CountDownLatch(splitList.size());
            for (List<File> fileList : splitList) {
                log.info("获取回执数据{}条", fileList.size());
                // 分发回执报文
                receiptService.issue(fileList, latch);
            }
            latch.await();
        } catch (Exception e) {
            log.error("回执分发出错" + e);
        }
    }
}
