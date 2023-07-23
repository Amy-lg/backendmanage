package com.power.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.power.entity.User;
import com.power.mapper.UserMapper;
import com.power.service.UserService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    // 查询所有用户
    @GetMapping
    public List<User> findAll() {
        /* mybatis的实现方法
        List<User> all = userMapper.findAll();
        if (!all.isEmpty()) {
            for (User person : all) {
                return person.toString();
            }
        }*/

        // mybatis-plus 中查询所有
        List<User> users = userService.list();
        return users;
    }

    // 新增/更新用户信息
    @PostMapping
    public boolean save(@RequestBody User user) {
        boolean update = userService.updateUser(user);
        return update;
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

    // 批量删除
    @PostMapping("/batchDel")
    public boolean batchDel(@RequestBody List<Integer> ids) {
        boolean batStatus = userService.removeBatchByIds(ids);
        return batStatus;
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
