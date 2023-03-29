package com.data.service;


import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public interface ReceiptService {

    void issue(List<File> fileList, CountDownLatch latch) throws IOException, DocumentException;

}
