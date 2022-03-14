package com.robin.service;

import com.robin.core.model.UserInfo;
import com.robin.core.result.ReturnT;

public interface UserService {

    public ReturnT<UserInfo> findUser(String username, String password);

}
