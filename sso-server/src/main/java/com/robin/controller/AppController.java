package com.robin.controller;

import com.robin.core.model.UserInfo;
import com.robin.core.result.ReturnT;
import com.robin.login.SsoTokenLoginHelper;
import com.robin.service.UserService;
import com.robin.store.SsoLoginStore;
import com.robin.store.SsoSessionIdHelper;
import com.robin.user.SsoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

/**
 * sso server (for app)
 *
 * @author xuxueli 2018-04-08 21:02:54
 */
@Controller
@RequestMapping("/app")
public class AppController {

    @Autowired
    private UserService userService;


    /**
     * Login
     *
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/login")
    @ResponseBody
    public ReturnT<String> login(String username, String password) {

        // valid login
        ReturnT<UserInfo> result = userService.findUser(username, password);
        if (result.getCode() != ReturnT.SUCCESS_CODE) {
            return new ReturnT<String>(result.getCode(), result.getMsg());
        }

        // 1、make sso user
        SsoUser ssoUser = new SsoUser();
        ssoUser.setUserid(String.valueOf(result.getData().getUserid()));
        ssoUser.setUsername(result.getData().getUsername());
        ssoUser.setVersion(UUID.randomUUID().toString().replaceAll("-", ""));
        ssoUser.setExpireMinute(SsoLoginStore.getRedisExpireMinute());
        ssoUser.setExpireFreshTime(System.currentTimeMillis());


        // 2、generate sessionId + storeKey
        String sessionId = SsoSessionIdHelper.makeSessionId(ssoUser);

        // 3、login, store storeKey
        SsoTokenLoginHelper.login(sessionId, ssoUser);

        // 4、return sessionId
        return new ReturnT<String>(sessionId);
    }


    /**
     * Logout
     *
     * @param sessionId
     * @return
     */
    @RequestMapping("/logout")
    @ResponseBody
    public ReturnT<String> logout(String sessionId) {
        // logout, remove storeKey
        SsoTokenLoginHelper.logout(sessionId);
        return ReturnT.SUCCESS;
    }

    /**
     * logincheck
     *
     * @param sessionId
     * @return
     */
    @RequestMapping("/logincheck")
    @ResponseBody
    public ReturnT<SsoUser> logincheck(String sessionId) {

        // logout
        SsoUser ssoUser = SsoTokenLoginHelper.loginCheck(sessionId);
        if (ssoUser == null) {
            return new ReturnT<SsoUser>(ReturnT.FAIL_CODE, "sso not login.");
        }
        return new ReturnT<SsoUser>(ssoUser);
    }

}
