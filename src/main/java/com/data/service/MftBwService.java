package com.data.service;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Description: 舱单报文
 * @Author: xuyang
 * @Date: 2022/9/8  15:16
 */
public interface MftBwService {

    /**
     * @Description: 异步改造报文
     * @Author: xuyang
     * @Date: 2022/9/8  15:33
     */
    void transform(List<File> fileList, CountDownLatch latch);

}
