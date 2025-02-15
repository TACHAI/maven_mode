package com.bdcom.hws.controller;


import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.StoppedSessionException;
import org.common.model.Barrage;
import org.common.model.client.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.bdcom.hws.service.ImagesService;
import com.bdcom.hws.service.UserService;
import com.bdcom.server.service.BarrageService;

@Api(value="用户模块")
@Controller
@RequestMapping(value="/user")
public class UserController {
	private static Logger logger = Logger.getLogger(UserController.class);
	@Resource
	@Qualifier("userServiceImpl")
	private UserService us;
	@Autowired
	private BarrageService barService;
	@Autowired
	private ImagesService imagesService;
	
	/*@ApiOperation(value = "get User by uId", notes = "通过用户id获取该用户", response = User.class)
	@RequestMapping(value="getUserByUid",method=RequestMethod.GET)
	public User  getUserByUid(@ApiParam(value="用户id",required=true) int uId){
		User user = us.getUserByUid(uId);
		return user;
	}*/

	@RequestMapping(value="/validate",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject login(HttpServletRequest request,HttpServletResponse response,boolean isRememberMe,boolean isCookie,User user){
		logger.info("验证账号密码");
		JSONObject obj = new JSONObject();
		try {
			obj= us.validateAccount(request, response, isRememberMe, isCookie, user);
		} catch (InvalidKeyException e) {
			obj.put("flag", false);
			logger.error("validateAccount方法出现InvalidKeyException异常");
			e.printStackTrace();
			return obj;
		} catch (NoSuchAlgorithmException e) {
			obj.put("flag", false);
			logger.error("validateAccount方法出现NoSuchAlgorithmException异常");
			e.printStackTrace();
			return obj;
		}catch (AuthenticationException e){
			obj.put("flag", false);
			logger.error("验证失败，请重新登录！！！");
			return obj;
		}catch(ExpiredSessionException e){
			
		}catch(StoppedSessionException e){
			
		}
		logger.info("验证成功，欢迎登陆!");
		return obj;
	}
	
	/**
	 * 跳转到此路径表示已经认证成功
	 * @param req
	 * @param userName 用户名
	 * @param isRemenberMe 是否使用cookie
	 * @return 
	 */
	@RequestMapping(value="/index")
	public ModelAndView initIndexPage(){
		logger.info("首页初始化开始");
		JSONObject obj = imagesService.getImages(1, 10);
		List<Barrage> barList = barService.getAllBar();
		obj.put("barList", barList);
		ModelAndView model = new ModelAndView("index",obj);
		logger.info("首页初始化结束");
		return model;
	}
	@RequestMapping(value="/upload")
	@ResponseBody
	public JSONObject upload(@RequestParam MultipartFile[] file){
		logger.info("数据上传开始");
		JSONObject obj = new JSONObject();
		try {
			if(null != file)imagesService.upload(file);
		} catch (Exception e) {
			logger.error("数据上传发生Exception异常");
			obj.put("uploadFlag", false);
			e.printStackTrace();
		}
		obj.put("uploadFlag", true);
		logger.info("数据上传结束");
		
		return obj;
	}
	@RequestMapping(value="/getImagesBar")
	@ResponseBody
	public JSONObject getImagesBar(Integer  imagesId){
		
		JSONObject obj = barService.getBarrageByImagesId(imagesId);
		return obj;
	}
	@RequestMapping(value="/pagination")
	@ResponseBody
	public JSONObject pagination(Integer pageNum){
		JSONObject obj = imagesService.getImages(pageNum, 10);
		return obj;
	}
	@RequestMapping(value="/addBar")
	@ResponseBody
	public JSONObject addBar(Barrage barrage){
		JSONObject obj = new JSONObject();
		boolean flag = barService.addBarrage(barrage);
		obj.put("addBarFlag", flag);
		return obj;
	}
}
