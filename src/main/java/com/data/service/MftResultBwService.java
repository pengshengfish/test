package com.data.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @Description: 舱单回执报文
 * @Author: xuyang
 * @Date: 2022/9/13  14:07
 */
public interface MftResultBwService {


    /**
     * @Description: 处理原始预配回执
     * @Author: xuyang
     * @Date: 2022/9/13  14:09
     */
    void parse(List<File> fileList, CountDownLatch latch);

}
