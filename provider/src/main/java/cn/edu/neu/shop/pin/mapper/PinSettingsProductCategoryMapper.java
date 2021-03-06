package cn.edu.neu.shop.pin.mapper;

import cn.edu.neu.shop.pin.model.PinSettingsProductCategory;
import cn.edu.neu.shop.pin.util.base.BaseMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PinSettingsProductCategoryMapper extends BaseMapper<PinSettingsProductCategory> {

    //获取所有一级商品种类
    List<PinSettingsProductCategory> getAllParentProductCategory();

    //获取所有二级商品种类
    List<PinSettingsProductCategory> getAllSubProductCategory();

    List<JSONObject> getParentProductCategory();

    List<JSONObject> getSubProductCategory(Integer parentId);

}