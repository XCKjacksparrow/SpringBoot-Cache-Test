# Springboot整合cache

## 一、搭建基本环境
1. 导入数据库文件 创建出department和employee表
2. 创建javaBean封装数据
3. 整合MyBatis操作数据库
    1. 配置数据源信息
    2. 使用注解版的MyBatis：
        1）、@MapperScan指定需要扫描的mapper接口所在的包(@MapperScan("per.xck.cache.mapper"))
##  二、快速体验缓存
1. 开启基于注解的缓存(@EnableCaching)
2. 标注缓存注解即可
    @Cacheable
    @CacheEvict
    @CachePut

将方法的运行结果进行缓存，以后再要相同的数据，直接从缓存中获取，不用调用方法

CacheManager管理多个Cache组件，对缓存的真正CRUD操作在Cache组件中，每一个缓存组件有自己唯一一个名字

几个属性：
*   cacheNames/value:指定缓存组件的名字
*   key：缓存数据使用的key：默认是使用方法参数的值 1-方法的返回值

        编写SqEL: #id 参数id的值  #a0 #p0 #root.args[0]
*   keyGenerator: key的生成器：可以自己指定key的生成器的组件id

        key/keyGenerator:二选一使用
*   cacheManager: 缓存管理器 或者cacheResolver
*   condition:指定符合条件的情况下才缓存
*   unless:否定缓存，与condition相反  可以获取到结果进行判定
*   sync:是否使用异步模式

原理
    1、自动配置类：CacheAutoConfiguration
    2、缓存的配置类
 *   org.springframework.boot.autoconfigure.cache.GenericCacheConfiguration
 *   org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration
 *   org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration
 *   org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration
 *   org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration
 *   org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration
 *   org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration
 *   org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration
 *   org.springframework.boot.autoconfigure.cache.GuavaCacheConfiguration
 *   org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration【默认】
 *   org.springframework.boot.autoconfigure.cache.NoOpCacheConfiguration
    3、哪个配置类默认生效：SimpleCacheConfiguration；
    4、给容器中注册了一个CacheManager：ConcurrentMapCacheManager
    5、可以获取和创建ConcurrentMapCache类型的缓存组件；他的作用将数据保存在ConcurrentMap中；

运行流程：
@Cacheable:

    1、方法运行之前，先去查询Cache（缓存组件），按照cacheNames指定的名字获取；
    
    （CacheManager先获取相应的缓存），第一次获取缓存如果没有Cache组件会自动创建。
    
    2、去Cache中查找缓存的内容，使用一个key，默认就是方法的参数；
    
    key是按照某种策略生成的；默认是使用keyGenerator生成的，默认使用SimpleKeyGenerator生成key；
    SimpleKeyGenerator生成key；
     *          SimpleKeyGenerator生成key的默认策略；
     *                  如果没有参数；key=new SimpleKey()；
     *                  如果有一个参数：key=参数的值
     *                  如果有多个参数：key=new SimpleKey(params)；
    3、没有查到缓存就调用目标方法；
    
    4、将目标方法返回的结果，放进缓存中
    
    @Cacheable标注的方法执行之前先来检查缓存中有没有这个数据，默认按照参数的值作为key去查询缓存，
    如果没有就运行方法并将结果放入缓存；以后再来调用就可以直接使用缓存中的数据；

核心：
    1）、使用CacheManager【ConcurrentMapCacheManager】按照名字得到Cache【ConcurrentMapCache】组件
    2）、key使用keyGenerator生成的，默认是SimpleKeyGenerator

@CachePut:既调用方法，又更新缓存数据

运行时机：

    1. 先调用目标方法
    2. 将目标方法的结果缓存起来    
@CacheEvict:删除缓存

    key：指定要清除的数据
        allEntries = true：指定清除这个缓存中所有的数据
        beforeInvocation = false：缓存的清除是否在方法之前执行
            默认代表缓存清除操作是在方法执行之后执行;如果出现异常缓存就不会清除

        beforeInvocation = true：
        代表清除缓存操作是在方法运行之前执行，无论方法是否出现异常，缓存都清除
        
集成Redis

```xml
<dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```
```
    @Autowired  
    StringRedisTemplate stringRedisTemplate;        //  k-v String 操作字符串的

    @Autowired
    RedisTemplate redisTemplate;                    // k-v 对象
```
Redis常见的五大数据类型：
    String（字符串）、List（列表）、Set（集合）、Hash（散列）、ZSet（有序集合）
    
    stringRedisTemplate.opsForValue()
```
    @Test
    public void test1(){
//        字符串操作
//        stringRedisTemplate.opsForValue().append("msg","hello");
//        String msg = stringRedisTemplate.opsForValue().get("msg");
//        System.out.println(msg);
          列表操作
//        stringRedisTemplate.opsForList().leftPush("mylist","1");
//        stringRedisTemplate.opsForList().leftPush("mylist","2");
    }


@Configuration
public class MyRedisConfig {

    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(factory).cacheDefaults(cacheConfiguration).build();
    }
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
//            2）redisTemplate默认的序列化规则; 改变默认的序列化规则
        Object o = myRedisTemplate.opsForValue().get("emp-01");
        System.out.println(o);
    }
```
    
原理：
   
    CacheManager==>Cache缓存组件在时机给缓存中存取数据
    
    1）、引入redis的starter，容器中保存的是RedisCacheManager
    
    2）、RedisCacheManager帮我们创建RedisCache来作为缓存组件，RedisCache通过操作redis缓存数据
    
    3）、默认保存数据 k-v 都是Object 利用序列化保存 如何保存json
        1.引入redis的starter、cacheManager变为RedisCacheManager
        2.默认创建的RedisCacheManager操作redis的时候使用的是 RedisTemplate
        3.RedisTemplate默认使用的jdk的序列化机制
    4)、自定义CacheManager