package com.zuel.syzc.spring.constant.enums;


public enum ResultCodeEnum
{

	SITES_OPEN("101","网页打开成功"),
	INTERNTE_FAILURE("102","网络错误，请重试"),
	UNKOWN_ERROE("103","未知的错误"),
	REQUEST_NO_PARAM_ID_ERROR("104","页面请求参数错误"),
	DB_SYS_ERROR("105","数据库错误"),
	SERVER_ERROR("500","服务器内部错误"),

	//连接
	OK("200","OK"),
	//增
	DB_ADD_FAILURE("202","添加失败"),
	//删
	DB_DELETE_SUCCESS("211","删除成功"),
	//改
	DB_UPDATE_ERROR("209","修改失败"),
	DB_UPDATE_SUCCESS("208","修改成功"),
	//查
	DB_FIND_FAILURE("209","查找失败，没有该条记录"),
	DB_FIND_SUCCESS("210","查找成功"),
	//请求参数
	PARA_WORNING_NULL("301","必要请求参数为空"),
	PARA_FORMAT_ERROR("302","请求的参数格式错误"),
	PARA_NUM_ERROR("303","请求的参数个数错误"),

	//系统功能
	LOGIN_SUCCESS("400","登录成功"),

	LOGIN_ERROR("401","登录失败_账号或密码错误"),
	REGISTER_SUCCESS("400","注册成功"),
	REGISTER_ERROR("400","注册失败"),
	USER_HAVE_REGISTER("401","该用户名已被注册"),
	NO_EXIST_USER("402","用户不存在"),
	NO_ENOUGH_MES("403","登录失败_账号或密码为空"),
	LOGOUT_SUCCESS("404","退出登录成功"),
	NO_LOGIN_USER("405","退出登录失败_用户未登录"),
	USER_ALREADY_LOGIN("411","用户已登陆"),;

	private String code;
	private String desc;

	ResultCodeEnum(String code, String desc)
	{
		this.code = code;
		this.desc = desc;
	}

	public String getCode()
	{
		return code;
	}

	public String getDesc()
	{
		return desc;
	}


}
