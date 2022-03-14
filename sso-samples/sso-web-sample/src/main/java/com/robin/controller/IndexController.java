package com.robin.controller;

import com.robin.conf.Conf;
import com.robin.entity.ReturnT;
import com.robin.user.SsoUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xuxueli 2017-08-01 21:39:47
 */
@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request) {

        SsoUser ssoUser = (SsoUser) request.getAttribute(Conf.SSO_USER);
        model.addAttribute("ssoUser", ssoUser);
        return "index";
    }

    @RequestMapping("/json")
    @ResponseBody
    public ReturnT<SsoUser> json(Model model, HttpServletRequest request) {
        SsoUser ssoUser = (SsoUser) request.getAttribute(Conf.SSO_USER);
        return new ReturnT(ssoUser);
    }

}