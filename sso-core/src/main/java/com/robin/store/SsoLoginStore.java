package com.robin.store;

import com.robin.conf.Conf;
import com.robin.user.SsoUser;
import com.robin.util.JedisUtil;

/**
 * local login store
 *
 * @author xuxueli 2018-04-02 20:03:11
 */
public class SsoLoginStore {

    private static int redisExpireMinute = 1440;    // 1440 minute, 24 hour
    public static void setRedisExpireMinute(int redisExpireMinute) {
        if (redisExpireMinute < 30) {
            redisExpireMinute = 30;
        }
        SsoLoginStore.redisExpireMinute = redisExpireMinute;
    }
    public static int getRedisExpireMinute() {
        return redisExpireMinute;
    }

    /**
     * get
     *
     * @param storeKey
     * @return
     */
    public static SsoUser get(String storeKey) {

        String redisKey = redisKey(storeKey);
        Object objectValue = JedisUtil.getObjectValue(redisKey);
        if (objectValue != null) {
            SsoUser ssoUser = (SsoUser) objectValue;
            return ssoUser;
        }
        return null;
    }

    /**
     * remove
     *
     * @param storeKey
     */
    public static void remove(String storeKey) {
        String redisKey = redisKey(storeKey);
        JedisUtil.del(redisKey);
    }

    /**
     * put
     *
     * @param storeKey
     * @param ssoUser
     */
    public static void put(String storeKey, SsoUser ssoUser) {
        String redisKey = redisKey(storeKey);
        JedisUtil.setObjectValue(redisKey, ssoUser, redisExpireMinute * 60);  // minute to second
    }

    private static String redisKey(String sessionId){
        return Conf.SSO_SESSIONID.concat("#").concat(sessionId);
    }

}
