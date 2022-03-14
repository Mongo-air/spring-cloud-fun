package com.robin.controller;

import com.robin.conf.Conf;
import com.robin.core.model.UserInfo;
import com.robin.core.result.ReturnT;
import com.robin.login.SsoWebLoginHelper;
import com.robin.service.UserService;
import com.robin.store.SsoLoginStore;
import com.robin.store.SsoSessionIdHelper;
import com.robin.user.SsoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * sso server (for web)
 *
 * @author xuxueli 2017-08-01 21:39:47
 */
@Controller
public class WebController {

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {

        // login check
        SsoUser SsoUser = SsoWebLoginHelper.loginCheck(request, response);

        if (SsoUser == null) {
            return "redirect:/login";
        } else {
            model.addAttribute("SsoUser", SsoUser);
            return "index";
        }
    }

    /**
     * Login page
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(Conf.SSO_LOGIN)
    public String login(Model model, HttpServletRequest request, HttpServletResponse response) {

        // login check
        SsoUser SsoUser = SsoWebLoginHelper.loginCheck(request, response);

        if (SsoUser != null) {

            // success redirect
            String redirectUrl = request.getParameter(Conf.REDIRECT_URL);
            if (redirectUrl!=null && redirectUrl.trim().length()>0) {

                String sessionId = SsoWebLoginHelper.getSessionIdByCookie(request);
                String redirectUrlFinal = redirectUrl + "?" + Conf.SSO_SESSIONID + "=" + sessionId;;

                return "redirect:" + redirectUrlFinal;
            } else {
                return "redirect:/";
            }
        }

        model.addAttribute("errorMsg", request.getParameter("errorMsg"));
        model.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
        return "login";
    }

    /**
     * Login
     *
     * @param request
     * @param redirectAttributes
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/doLogin")
    public String doLogin(HttpServletRequest request,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes,
                        String username,
                        String password,
                        String ifRemember) {

        boolean ifRem = (ifRemember!=null&&"on".equals(ifRemember))?true:false;

        // valid login
        ReturnT<UserInfo> result = userService.findUser(username, password);
        if (result.getCode() != ReturnT.SUCCESS_CODE) {
            redirectAttributes.addAttribute("errorMsg", result.getMsg());

            redirectAttributes.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
            return "redirect:/login";
        }

        // 1、make sso user
        SsoUser SsoUser = new SsoUser();
        SsoUser.setUserid(String.valueOf(result.getData().getUserid()));
        SsoUser.setUsername(result.getData().getUsername());
        SsoUser.setVersion(UUID.randomUUID().toString().replaceAll("-", ""));
        SsoUser.setExpireMinute(SsoLoginStore.getRedisExpireMinute());
        SsoUser.setExpireFreshTime(System.currentTimeMillis());


        // 2、make session id
        String sessionId = SsoSessionIdHelper.makeSessionId(SsoUser);

        // 3、login, store storeKey + cookie sessionId
        SsoWebLoginHelper.login(response, sessionId, SsoUser, ifRem);

        // 4、return, redirect sessionId
        String redirectUrl = request.getParameter(Conf.REDIRECT_URL);
        if (redirectUrl!=null && redirectUrl.trim().length()>0) {
            String redirectUrlFinal = redirectUrl + "?" + Conf.SSO_SESSIONID + "=" + sessionId;
            return "redirect:" + redirectUrlFinal;
        } else {
            return "redirect:/";
        }

    }

    /**
     * Logout
     *
     * @param request
     * @param redirectAttributes
     * @return
     */
    @RequestMapping(Conf.SSO_LOGOUT)
    public String logout(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {

        // logout
        SsoWebLoginHelper.logout(request, response);

        redirectAttributes.addAttribute(Conf.REDIRECT_URL, request.getParameter(Conf.REDIRECT_URL));
        return "redirect:/login";
    }


}
