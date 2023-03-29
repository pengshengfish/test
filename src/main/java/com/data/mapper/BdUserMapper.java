package com.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.entity.BdUser;

import java.util.List;

/**
 * <p>
 * 大数据贸易用户表 Mapper 接口
 * </p>
 *
 * @author ps
 * @since 2022-10-12
 */
public interface BdUserMapper extends BaseMapper<BdUser> {

    /**
     * @Description: 根据通道用户表的组织机构代码查询出用户表的数据
     * @Author: xuyang
     * @Date: 2022/10/12  17:32
     */
    List<BdUser> listByDeptNo(String deptNo);
}
