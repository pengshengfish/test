package com.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.entity.BaseResultXml;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 舱单回执报文表 Mapper 接口
 * </p>
 *
 * @author ps
 * @since 2022-09-13
 */
public interface BaseResultXmlMapper extends BaseMapper<BaseResultXml> {

    /**
     * @Description: 获取最多20条当前企业的回执
     * @Author: xuyang
     * @Date: 2022/9/14  9:58
     */
    List<BaseResultXml> listResultBwByOrgCode(@Param("orgCode") String orgCode);

}
