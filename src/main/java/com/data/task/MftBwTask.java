package com.data.task;


import com.data.service.MftBwService;
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
 * @Description: 舱单报文定时任务
 * @Author: xuyang
 * @Date: 2022/9/8  14:44
 */
@Component
@Slf4j
public class MftBwTask {

    @Value("${msgDir}")
    private String msgDir;

    @Autowired
    private MftBwService mftBwService;

    /**
     * @Description: 舱单报文改造
     * @Author: xuyang
     * @Date: 2022/9/8  14:52
     */
    @Scheduled(fixedDelay = 60000)
    public void analyzing() {
        // 获取文件集
        List<List<File>> splitList = PublicUtil.getFileList(msgDir);
        if (null == splitList || splitList.size() == 0) {
            return;
        }

        try {
            CountDownLatch latch = new CountDownLatch(splitList.size());
            for (List<File> fileList : splitList) {
                // 改造报文
                mftBwService.transform(fileList, latch);
            }
            latch.await();
        } catch (Exception e) {
            log.error("改造舱单报文出错" + e);
        }
    }
}
