package per.xck.cache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import per.xck.cache.bean.Employee;
import per.xck.cache.mapper.EmployeeMapper;

@Service
public class EmployeeService {

    @Autowired
    EmployeeMapper employeeMapper;

    /**
     * 将方法的运行结果进行缓存，以后再要相同的数据，直接从缓存中获取，不用调用方法
     *
     * CacheManager管理多个Cache组件，对缓存的真正CRUD操作在Cache组件中，每一个缓存组件有自己唯一一个名字
     * 几个属性：
     *      cacheNames/value:指定缓存组件的名字
     *      key：缓存数据使用的key：默认是使用方法参数的值 1-方法的返回值
     *          编写SqEL: #id 参数id的值  #a0 #p0 #root.args[0]
            keyGenerator: key的生成器：可以自己指定key的生成器的组件id
                key/keyGenerator:二选一使用
            cacheManager: 缓存管理器 或者cacheResolver
            condition:指定符合条件的情况下才缓存
            unless:否定缓存，与condition相反  可以获取到结果进行判定
            sync:是否使用异步模式
     * @param id
     * @return
     */
    @Cacheable(cacheNames = "emp",condition = "#id>0")
    public Employee getEmp(Integer id){
        System.out.println("查询" + id + "号员工");
        Employee employee = employeeMapper.getEmpById(id);
        return employee;
    }


    /**
     * @CachePut:既调用方法，又更新缓存数据
     * @retur
     */
    @CachePut(cacheNames = "emp", key = "#employee.id")
    public Employee updateEmp(Employee employee){
        employeeMapper.updateEmp(employee);
        return employee;
    }

    /**
     * @CacheEvict:删除缓存
     * key：指定要清除的数据
     *        allEntries = true：指定清除这个缓存中所有的数据
     *        beforeInvocation = false：缓存的清除是否在方法之前执行
     *            默认代表缓存清除操作是在方法执行之后执行;如果出现异常缓存就不会清除
     *
              beforeInvocation = true：
     *      代表清除缓存操作是在方法运行之前执行，无论方法是否出现异常，缓存都清除
     * @param id
     */

    @CacheEvict(cacheNames = "emp",key = "#id")
    public void deleteEmp(Integer id){
        System.out.println("调用删除");
//        employeeMapper.deleteEmp(id);
    }



    // @Caching 定义复杂的缓存规则
    @Caching(
            cacheable = {
                    @Cacheable(/*value="emp",*/key = "#lastName")
            },
            put = {
                    @CachePut(/*value="emp",*/key = "#result.id"),
                    @CachePut(/*value="emp",*/key = "#result.email")
            }
    )
    public Employee getEmpByLastName(String lastName){
        Employee emp = employeeMapper.getEmpByLastName(lastName);
        return emp;
    }
}
