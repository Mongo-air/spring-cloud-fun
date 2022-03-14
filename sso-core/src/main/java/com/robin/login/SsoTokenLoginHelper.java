package com.robin.login;

import com.robin.conf.Conf;
import com.robin.store.SsoLoginStore;
import com.robin.store.SsoSessionIdHelper;
import com.robin.user.SsoUser;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xuxueli 2018-11-15 15:54:40
 */
public class SsoTokenLoginHelper {

    /**
     * client login
     *
     * @param sessionId
     * @param ssoUser
     */
    public static void login(String sessionId, SsoUser ssoUser) {

        String storeKey = SsoSessionIdHelper.parseStoreKey(sessionId);
        if (storeKey == null) {
            throw new RuntimeException("parseStoreKey Fail, sessionId:" + sessionId);
        }

        SsoLoginStore.put(storeKey, ssoUser);
    }

    /**
     * client logout
     *
     * @param sessionId
     */
    public static void logout(String sessionId) {

        String storeKey = SsoSessionIdHelper.parseStoreKey(sessionId);
        if (storeKey == null) {
            return;
        }

        SsoLoginStore.remove(storeKey);
    }
    /**
     * client logout
     *
     * @param request
     */
    public static void logout(HttpServletRequest request) {
        String headerSessionId = request.getHeader(Conf.SSO_SESSIONID);
        logout(headerSessionId);
    }


    /**
     * login check
     *
     * @param sessionId
     * @return
     */
    public static SsoUser loginCheck(String  sessionId){

        String storeKey = SsoSessionIdHelper.parseStoreKey(sessionId);
        if (storeKey == null) {
            return null;
        }

        SsoUser ssoUser = SsoLoginStore.get(storeKey);
        if (ssoUser != null) {
            String version = SsoSessionIdHelper.parseVersion(sessionId);
            if (ssoUser.getVersion().equals(version)) {

                // After the expiration time has passed half, Auto refresh
                if ((System.currentTimeMillis() - ssoUser.getExpireFreshTime()) > ssoUser.getExpireMinute()/2) {
                    ssoUser.setExpireFreshTime(System.currentTimeMillis());
                    SsoLoginStore.put(storeKey, ssoUser);
                }

                return ssoUser;
            }
        }
        return null;
    }


    /**
     * login check
     *
     * @param request
     * @return
     */
    public static SsoUser loginCheck(HttpServletRequest request){
        String headerSessionId = request.getHeader(Conf.SSO_SESSIONID);
        return loginCheck(headerSessionId);
    }


}
