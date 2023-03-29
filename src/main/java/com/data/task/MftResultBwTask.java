package com.data.task;


import com.data.service.impl.MftResultBwServiceImpl;
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
 * @Description: 舱单回执报文定时任务
 * @Author: xuyang
 * @Date: 2022/9/13  13:44
 */
@Component
@Slf4j
public class MftResultBwTask {

    @Value("${msgResultDir}")
    private String msgResultDir;

    @Value("${msgResultDir2}")
    private String msgResultDir2;

    @Autowired
    private MftResultBwServiceImpl mftResultBwService;

    /**
     * @Description: 处理原始预配回执报文
     * @Author: xuyang
     * @Date: 2022/9/13  13:55
     */
    @Scheduled(fixedDelay = 60000)
    public void analyzing() {
        // 获取文件集
        List<List<File>> splitList = PublicUtil.getFileList(msgResultDir);
        if (null == splitList || splitList.size() == 0) {
            return;
        }

        try {
            CountDownLatch latch = new CountDownLatch(splitList.size());
            for (List<File> fileList : splitList) {
                // 改造报文
                mftResultBwService.parse(fileList, latch);
            }
            latch.await();
        } catch (Exception e) {
            log.error("舱单报文出错" + e);
        }
    }

    /**
     * @Description: 处理运抵理货回执报文
     * @Author: xuyang
     * @Date: 2022/9/13  13:55
     */
//    @Scheduled(fixedDelay = 60000)
    public void analyzing2() {
        // 获取文件集
        List<List<File>> splitList = PublicUtil.getFileList(msgResultDir2);
        if (null == splitList || splitList.size() == 0) {
            return;
        }

        try {
            CountDownLatch latch = new CountDownLatch(splitList.size());
            for (List<File> fileList : splitList) {
                // 改造报文
                mftResultBwService.parse(fileList, latch);
            }
            latch.await();
        } catch (Exception e) {
            log.error("舱单报文出错" + e);
        }
    }
}
