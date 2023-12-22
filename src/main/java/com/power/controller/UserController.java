package com.power.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.power.common.Result;
import com.power.common.constant.ResultStatusCode;
import com.power.controller.logincontroller.VerifyCodeController;
import com.power.entity.User;
import com.power.entity.dto.LoginUserDTO;
import com.power.entity.dto.UserDTO;
import com.power.entity.query.UserQuery;
import com.power.mapper.UserMapper;
import com.power.service.UserService;
import com.power.utils.AesUtil;
import com.power.utils.Md5Util;
import com.power.utils.ResultUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;


    /**
     * 用户登录
     * @CrossOrigin // 此注解为局部方法跨域，处理方法比较细，使用比较少
     * @param loginUserDTO
     * @param request
     * @return
     * @throws NoSuchAlgorithmException
     */
    @PostMapping("/login")
    public Result userLogin(@RequestBody LoginUserDTO loginUserDTO,
                            HttpServletRequest request) throws NoSuchAlgorithmException {

        String username = loginUserDTO.getUsername();
        String password = loginUserDTO.getPassword();

        // 解密
        String decryptPwd = AesUtil.decrypt(password);
        // MD5加密
        String md5Pwd = Md5Util.encrypt(decryptPwd);
        loginUserDTO.setPassword(md5Pwd);

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return ResultUtils.error(ResultStatusCode.ERROR_USER_001, "用户名或密码不能为空");
        }
        UserDTO loginUser = userService.userLogin(loginUserDTO, request);
        List<Object> resultList = new ArrayList<>();
        if (loginUser == null) {
            resultList.add(ResultStatusCode.ERROR_USER_002.getCode());
            resultList.add(ResultStatusCode.ERROR_USER_002.getMsg());
            return ResultUtils.success(resultList);
        }
        if (loginUser.getUsername() != null && loginUser.getPassword() != null) {
            resultList.add(ResultStatusCode.OK_0.getCode());
            resultList.add(loginUser);
            return ResultUtils.success(resultList);
        } else {
            resultList.add(ResultStatusCode.EXCEPTION_USER_1001.getCode());
            resultList.add(ResultStatusCode.EXCEPTION_USER_1001.getMsg());
            return ResultUtils.success(resultList);
        }
    }

    /**
     * 生成验证码
     * @param request
     * @param response
     * @throws IOException
     */
    private void verifyShearCaptcha(HttpServletRequest request, HttpServletResponse response){

        response.setContentType("image/jpeg");
        response.setHeader("pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 定义图形验证码的长、宽、验证码字符数、干扰线宽度
        ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(400, 100, 5, 6);
        // 图形验证码写出，可以写出到文件或者写出到流
        ServletOutputStream opt = null;
        try {
            opt = response.getOutputStream();
            shearCaptcha.write(opt);
            // 获取验证码中的文字内容，存储到session中
            request.getSession().setAttribute("verifyCode", shearCaptcha.getCode());
            opt.flush();
            opt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 页面关闭后，复制链接重新打开，token验证
     * @param request
     * @param verifyToken
     * @return
     */
    @PostMapping("/rebuild")
    public Result againLogin(HttpServletRequest request, @RequestBody String verifyToken) {

        String reqToken = request.getHeader("token").toString();
        if (verifyToken != null && !"".equals(verifyToken)) {
            String substringToken = verifyToken.substring(0, verifyToken.length() - 1);
            if (reqToken.equals(substringToken)) {
//            User currentUser = TokenUtils.getCurrentUser();
                String userId = JWT.decode(verifyToken).getAudience().get(0);
                User currentUser = userService.getById(Integer.valueOf(userId));
                return ResultUtils.success(currentUser);
            }
        }
        return null;
    }


    /**
     * 查询所有用户
     * @param userQuery
     * @return
     */
    @PostMapping("/findLoginAll")
    public Result findAll(@RequestBody UserQuery userQuery) {
        /* mybatis的实现方法
        List<User> all = userMapper.findAll();
        if (!all.isEmpty()) {
            for (User person : all) {
                return person.toString();
            }
        }*/

        // mybatis-plus 中查询所有
        IPage<User> loginUser = userService.findOrSearch(userQuery);
        if (loginUser != null) {
            return ResultUtils.success(loginUser);
        }
        return ResultUtils.success();
    }


    /**
     * 新增/更新用户信息(admin才能新增)
     * @param user
     * @return
     */
    @PostMapping("/addLoginUser")
    public Result save(@RequestBody User user) {

        if (user != null) {
            String addResult = userService.addNewUser(user);
            if (!addResult.isBlank()) {
//                List<Object> respData = Arrays.asList();
                List<Object> list = new ArrayList<>();
                list.add(ResultStatusCode.SUCCESS_ADD_LOGIN_USER.getCode());
                list.add(addResult);
                return ResultUtils.success(list);
            }
        }
        return ResultUtils.error(5006, "新增用户失败，信息不能为空");
    }

    // 删除
    @DeleteMapping("/{id}")
    public boolean deleteById(@PathVariable(required = false) Integer id) {
        /* mybatis的实现方法
        int deleteStatus = userService.deleteById(id);
        return deleteStatus;*/

        // mybatis-plus 中删除
        boolean delStatus = userService.removeById(id);
        return delStatus;
    }


    /**
     * 批量删除(管理员权限使用)
     * @param ids
     * @return
     */
    @PostMapping("/batchDel")
    public Result batchDel(@RequestBody List<Integer> ids) {

        String deleteInfo = userService.deleteBatch(ids);
        if (deleteInfo != null && "".equals(deleteInfo)) {
            return ResultUtils.success(deleteInfo);
        }
        return ResultUtils.success();
    }

    // 分页查询 -- mybatis的实现
    /*@GetMapping("/page")
    public Map<String, Object> findPage(@RequestParam Integer pageNum,
                         @RequestParam Integer pageSize,
                         @RequestParam(required = false) String username) {

        Map<String, Object> map = new HashMap<>();
        // 从第几页开始查询
        pageNum = (pageNum - 1) * pageSize;
        List<User> userList = userMapper.findPage(pageNum, pageSize, username);
        // 总条数
        Integer total = userMapper.selectTotal(username);
        map.put("total", total);
        map.put("data", userList);
        return map;
    }*/

    // 分页查询 -- mybatis-plus的实现
    @GetMapping("/page")
    public IPage<User> findPage(@RequestParam Integer pageNum,
                                @RequestParam Integer pageSize,
                                @RequestParam(defaultValue = "") String username,
                                @RequestParam(defaultValue = "") String phone,
                                @RequestParam(defaultValue = "") String address) {

        IPage<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        if (!"".equals(username)) {
            userWrapper.like("username", username);
        }
        if (!"".equals(phone)) {
            userWrapper.like("phone", phone);
        }
        if (!"".equals(address)) {
            userWrapper.like("address", address);
        }
        // 将查询到的数据根据相应的列字段倒叙显示
//        userWrapper.orderByDesc("id");
        IPage<User> userIPage = userService.page(page, userWrapper);
        return userIPage;
    }

    /**
     * 文件的导入
     */
    @PostMapping("/importInfo")
    public boolean importUserInfo(MultipartFile file) throws IOException {

        // 首先获取文件输入流
        InputStream fileInputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(fileInputStream);
        List<User> userList = reader.readAll(User.class);
        System.out.println(userList);
        // 批量存储到数据库
        boolean importStatus = userService.saveBatch(userList);
        return importStatus;
    }

    /**
     * 文件的导出
     */
    @GetMapping("/exportInfo")
    public void exportUserInfo(HttpServletResponse response) throws Exception {

        // 查询到数据库中所有的用户信息
        List<User> userList = userService.list();
        // 默认写出格式为 .xls ; 这里设置为true，写出格式为 .xlsx
        ExcelWriter writer = ExcelUtil.getWriter(true);

        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("password", "密码");
        writer.addHeaderAlias("nickname", "昵称");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("address", "地址");

        // 一次性写出内容，使用默认样式(这里输出的标题和实体类中的属性名一致)，强制输出标题
        writer.write(userList, true);

        // fileName不能为中文，中文需要加下面一行代码，进行自行编码
        String fileName = URLEncoder.encode("用户信息表", "utf-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename="+ fileName +".xlsx");

        ServletOutputStream outputStream = response.getOutputStream();
        // outputStream为OutputStream流，需要写出到的目标流
        writer.flush(outputStream, true);
        // 关闭writer释放内存
        writer.close();
        // 记得关闭输出Servlet流
        outputStream.close();
    }
}
