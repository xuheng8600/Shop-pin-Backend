package cn.edu.neu.shop.pin.mapper;

import static org.junit.Assert.*;

import cn.edu.neu.shop.pin.model.PinRole;
import cn.edu.neu.shop.pin.model.PinUser;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@RunWith(SpringRunner.class)
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PinUserMapperTest {
    @Autowired
    PinUserMapper pinUserMapper;


    @Test
    public void findById() {
        PinUser pinUser = pinUserMapper.findById(1);
        List<PinRole> roles = PinUser.transfer(pinUser.getRoles());
        pinUser.setRoles(roles);
        System.out.println(pinUser);
    }

    @Test
    public void existsById() {
        System.out.println(pinUserMapper.existsById(2));
    }

    @Test
    public void save() {


    }

    @Test
    public void deleteByEmail() {

    }
}