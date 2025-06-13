


import com.RyanDemoApplication;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ryan.business.entity.User;
import com.ryan.business.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;


@SpringBootTest(classes = RyanDemoApplication.class)
class InsertTests {

    @Autowired
    private UserMapper userMapper;


    @Test
    public void  insert(){
        User user = new User();
        user.setName("刘强东");
        user.setAge(37);
        user.setEmail("lqd@jd.com");
        user.setManagerId(1087982257332887553L);
        int rows = userMapper.insert(user);
        System.out.println("影响行数"+rows);

    }
    
    @Test
    public void selectByIdTest() {
        User user = userMapper.selectById(1435065643693645826L);
        System.out.println(user);
    }
    
    @Test
    public void selectByIds() {
        List<Long> ids = Arrays.asList(1088248166370832385L, 1094590409767661570L, 1435065643693645826L);
        List<User> users = userMapper.selectBatchIds(ids);
        users.forEach(System.out::println);
    }
    
    @Test
    public void selectByMapTest() {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("name", "王天风");
        columnMap.put("age", 25);
        columnMap.put("manager_id", 1087982257332887553L);
        List<User> users = userMapper.selectByMap(columnMap);
        users.forEach(System.out::println);
    }
    
    @Test
    public void queryWrapperTest() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", "天风").lt("age", 26);
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);
        
    }
    
    @Test
    public void queryWrapper2Test() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", "雨").between("age", 20, 40).isNotNull("email");
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);
    }
    
    @Test
    public void queryWrapper3Test() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("name", "王").or().ge("age", 25).orderByDesc("age").orderByAsc("id");
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);
    }
    
    @Test
    public void disjointTest() {
        List<String> list1 = Arrays.asList("1", "2");
        List<String> list2 = Arrays.asList("4", "3");
        boolean disjoint = Collections.disjoint(list1, list2);
        System.out.println(0);
    }
    
}

