package cn.edu.neu.shop.pin.controller;

import cn.edu.neu.shop.pin.exception.CheckInFailedException;
import cn.edu.neu.shop.pin.model.PinUser;
import cn.edu.neu.shop.pin.model.PinUserAddress;
import cn.edu.neu.shop.pin.service.*;
import cn.edu.neu.shop.pin.service.security.UserService;
import cn.edu.neu.shop.pin.util.PinConstants;
import cn.edu.neu.shop.pin.util.ResponseWrapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping("/commons/user")
public class UsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private UserProductRecordService userProductRecordService;

    @Autowired
    private UserStoreCollectionService userStoreCollectionService;

    @Autowired
    private UserProductCollectionService userProductCollectionService;

    @Autowired
    private OrderIndividualService orderIndividualService;

    @Autowired
    private UserCreditRecordService userCreditRecordService;

    /**
     * 获取用户信息
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/info")
    public JSONObject getUserInfo(HttpServletRequest httpServletRequest) {
        try {
            PinUser user = userService.whoAmI(httpServletRequest);
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS,
                    userService.getUserInfoByUserId(user.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest
     * @return
     * @author flyhero
     * 获取某用户的默认地址
     */
    @GetMapping("/default-address")
    public JSONObject getDefaultAddress(HttpServletRequest httpServletRequest) {
        PinUser user = userService.whoAmI(httpServletRequest);
        try {
            JSONObject data = new JSONObject();
            data.put("defaultAddress", addressService.getDefaultAddress(user.getId()));
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * 根据用户ID，查询该用户的所有收货地址
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/address")
    public JSONObject getAllAddresses(HttpServletRequest httpServletRequest) {
        try {
            PinUser user = userService.whoAmI(httpServletRequest);
            JSONObject data = new JSONObject();
            data.put("list", addressService.getAllAddresses(user.getId()));
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest
     * @param requestJSON
     * @return
     * @author flyhero
     * 增加地址，增加了对isDefault的检查
     */
    @PostMapping("/address")
    public JSONObject createAddress(HttpServletRequest httpServletRequest, @RequestBody JSONObject requestJSON) {
        try {
            PinUser user = userService.whoAmI(httpServletRequest);
            PinUserAddress address = JSONObject.toJavaObject(requestJSON, PinUserAddress.class);
            address.setUserId(user.getId());
            address.setCreateTime(new Date());
            addressService.createAddress(address);
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest
     * @param requestJSON
     * @return
     * @author flyhero
     * 删除地址
     */
    @DeleteMapping("/address")
    public JSONObject deleteAddress(HttpServletRequest httpServletRequest, @RequestBody JSONObject requestJSON) {
        try {
            PinUser user = userService.whoAmI(httpServletRequest);
            Integer addressId = requestJSON.getInteger("id");
            if (addressId == null) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.PERMISSION_DENIED, "无权限删除", null);
            }
            int code = addressService.deleteAddress(addressId, user.getId());
            if (code == AddressService.STATUS_DELETE_ADDRESS_SUCCESS) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, null);
            } else if (code == AddressService.STATUS_DELETE_ADDRESS_INVALID_ID) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, "删除失败", null);
            } else if (code == AddressService.STATUS_DELETE_ADDRESS_PERMISSION_DENIED) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.PERMISSION_DENIED, "无权限删除", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
        return null;
    }

    /**
     * @param httpServletRequest
     * @param requestJSON
     * @return
     * @author flyhero
     * 更新地址
     */
    @PutMapping("/address")
    public JSONObject updateAddress(HttpServletRequest httpServletRequest, @RequestBody JSONObject requestJSON) {
        try {
            PinUser user = userService.whoAmI(httpServletRequest);
            PinUserAddress addressToUpdate = JSONObject.toJavaObject(requestJSON, PinUserAddress.class);
            int code = addressService.updateAddress(user.getId(), addressToUpdate);
            if (code == AddressService.STATUS_UPDATE_ADDRESS_SUCCESS) { // 更新成功
                return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, null);
            } else if (code == AddressService.STATUS_UPDATE_ADDRESS_INVALID_ID) { // 无地址记录
                return ResponseWrapper.wrap(PinConstants.StatusCode.INVALID_DATA, "无对应地址记录！", null);
            } else if (code == AddressService.STATUS_UPDATE_ADDRESS_PERMISSION_DENIED) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.PERMISSION_DENIED, "无权限删除！", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
        return null;
    }

    /**
     * 获取商品浏览记录
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/product-visit-record")
    public JSONObject getUserProductRecord(HttpServletRequest httpServletRequest) {
        try {
            PinUser user = userService.whoAmI(httpServletRequest);
            JSONObject data = new JSONObject();
            data.put("list", userProductRecordService.getUserProductVisitRecord(user.getId()));
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest 请求对象
     * @return 响应 JSON
     * @author flyhero
     * 获取商品收藏product-collection
     */
    @GetMapping("/product-collection")
    public JSONObject getUserProductCollection(HttpServletRequest httpServletRequest) {
        PinUser user = userService.whoAmI(httpServletRequest);
        try {
            JSONObject data = new JSONObject();
            data.put("list", userProductCollectionService.getUserProductCollection(user.getId()));
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest 请求对象
     * @return 响应 JSON
     * @author flyhero
     * 获取店铺收藏store-collection
     */
    @GetMapping("/store-collection")
    public JSONObject getUserStoreCollection(HttpServletRequest httpServletRequest) {
        PinUser user = userService.whoAmI(httpServletRequest);
        try {
            JSONObject data = new JSONObject();
            data.put("list", userStoreCollectionService.getUserStoreCollection(user.getId()));
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest
     * @param requestJSON
     * @return
     * @author flyhero
     * 添加商品收藏
     */
    @PostMapping("/product-collection")
    public JSONObject addProductToCollection(HttpServletRequest httpServletRequest, @RequestBody JSONObject requestJSON) {
        try {
            PinUser user = userService.whoAmI(httpServletRequest);
            Integer userId = user.getId();
            Integer productId = requestJSON.getInteger("productId");
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS,
                    userProductCollectionService.addProductToCollection(productId, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest
     * @param productId
     * @return
     * @author flyhero
     * 删除商品收藏
     */
    @DeleteMapping("/product-collection/{productId}")
    public JSONObject deleteUserProductCollection(HttpServletRequest httpServletRequest, @PathVariable Integer productId) {
        PinUser user = userService.whoAmI(httpServletRequest);
        try {
            int code = userProductCollectionService.deleteStoreCollection(user.getId(), productId);
            if (code == UserProductCollectionService.STATUS_DELETE_PRODUCT_SUCCESS) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, null);
            } else if (code == UserProductCollectionService.STATUS_DELETE_PRODUCT_PERMISSION_DENIED) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.PERMISSION_DENIED, "无权限删除", null);
            } else if (code == UserProductCollectionService.STATUS_ADD_PRODUCT_INVALID_ID) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, "删除失败", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
        return null;
    }

    /**
     * @param httpServletRequest
     * @param requestJSON
     * @return
     * @author flyhero
     * 添加店铺收藏
     */
    @PostMapping("/store-collection")
    public JSONObject addStoreToCollection(HttpServletRequest httpServletRequest, @RequestBody JSONObject requestJSON) {
        try {
            PinUser user = userService.whoAmI(httpServletRequest);
            Integer userId = user.getId();
            Integer storeId = requestJSON.getInteger("storeId");
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS,
                    userStoreCollectionService.addStoreToCollection(storeId, userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest
     * @param storeId
     * @return
     * @author flyhero
     * 删除店铺收藏
     */
    @DeleteMapping("/store-collection/{storeId}")
    public JSONObject deleteUserStoreCollection(HttpServletRequest httpServletRequest, @PathVariable Integer storeId) {
        PinUser user = userService.whoAmI(httpServletRequest);
        try {
            int code = userProductCollectionService.deleteStoreCollection(user.getId(), storeId);
            if (code == UserStoreCollectionService.STATUS_DELETE_STORE_SUCCESS) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, null);
            } else if (code == UserStoreCollectionService.STATUS_DELETE_STORE_PERMISSION_DENIED) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.PERMISSION_DENIED, "无权限删除", null);
            } else if (code == UserStoreCollectionService.STATUS_DELETE_STORE_INVALID_ID) {
                return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, "删除失败", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
        return null;
    }

    /**
     * @param httpServletRequest
     * @return
     * @author flyhero
     * 签到功能
     */
    @GetMapping("/check-in")
    public JSONObject checkIn(HttpServletRequest httpServletRequest) {
        PinUser user = userService.whoAmI(httpServletRequest);
        try {
            userCreditRecordService.dailyCheckIn(user.getId());
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, null);
        } catch (CheckInFailedException e) {
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest
     * @return
     * @author flyhero
     * 获取用户签到详细信息历史记录
     */
    @GetMapping("/credit-record")
    public JSONObject getUserCreditData(HttpServletRequest httpServletRequest) {
        PinUser user = userService.whoAmI(httpServletRequest);
        try {
            JSONObject data = userCreditRecordService.getUserCreditData(user.getId());
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, PinConstants.ResponseMessage.SUCCESS, data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, e.getMessage(), null);
        }
    }

    /**
     * @param httpServletRequest
     * @return
     * @author flyhero
     * 判断某一用户今日是否已经签到
     */
    @GetMapping("/has-checked-in")
    public JSONObject hasCheckedIn(HttpServletRequest httpServletRequest) {
        PinUser user = userService.whoAmI(httpServletRequest);
        Boolean flag = userCreditRecordService.hasCheckedIn(user.getId());
        if (flag) {
            return ResponseWrapper.wrap(PinConstants.StatusCode.SUCCESS, "已签到", null);
        } else {
            return ResponseWrapper.wrap(PinConstants.StatusCode.INTERNAL_ERROR, "未签到", null);
        }
    }
}
