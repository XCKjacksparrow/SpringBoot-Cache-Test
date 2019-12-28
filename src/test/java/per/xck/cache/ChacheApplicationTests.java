package per.xck.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import per.xck.cache.bean.Employee;
import per.xck.cache.mapper.EmployeeMapper;

@SpringBootTest
class ChacheApplicationTests {

    @Autowired
    EmployeeMapper employeeMapper;


    @Autowired
    StringRedisTemplate stringRedisTemplate;        //  k-v String 操作字符串的

    @Autowired
    RedisTemplate<Object,Object> myRedisTemplate;                    // k-v 对象

    @Test
    public void test1(){
//        stringRedisTemplate.opsForValue().append("msg","hello");
//        String msg = stringRedisTemplate.opsForValue().get("msg");
//        System.out.println(msg);

//        stringRedisTemplate.opsForList().leftPush("mylist","1");
//        stringRedisTemplate.opsForList().leftPush("mylist","2");
    }

    //测试
    @Test
    public void test2(){
        Employee employee = employeeMapper.getEmpById(1);
        System.out.println(employee);
        //默认如果保存对象，使用jdk序列化机制，序列化后的数据保存到redis中
        myRedisTemplate.opsForValue().set("emp-01",employee);
        //1、将数据以json的方式保存
//            1）自己将对象转为json
//            2）redisTemplate默认的序列化规则
        Object o = myRedisTemplate.opsForValue().get("emp-01");
        System.out.println(o);
    }

    @Test
    void contextLoads() {
        Employee employee = employeeMapper.getEmpById(1);
        System.out.println(employee);
    }

}
