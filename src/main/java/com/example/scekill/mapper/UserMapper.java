package com.example.scekill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.scekill.pojo.User;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author mzc
 * @since 2021-07-17
 */

@Mapper
@Repository
public interface UserMapper extends BaseMapper<User> {

}
