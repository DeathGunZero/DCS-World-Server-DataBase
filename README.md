# Intro - DCS服务玩家信息管理系统

## 进度

- [x] 完成数据库的设计与部署
- [x] 完成操作数据库用的`Springboot`的逻辑设计
- [x] 完成数据交互用的`Netty`部署
- [x] 完成***DCS***端的触发器设计
- [ ] 完成管理系统前端设计
- [ ] 使用`docker`打包

## 概述

***DCS服务器玩家信息管理系统*** 是基于DCS自带的`LuaSocket`库、`DCS Hook`、`Spring Boot + MySql`以及`Netty`来构建的。

- 环境:
  - `LuaSocket`: 2.0
  - `DCS Hook`
  - `SpringBoot`: 3.0
  - `MySql`: 8.0
  - `Netty`
  - `Hibernate`

- 目的:

  为了将***永久性***的记录进入***服务器的玩家数据***，例如***点数`pts`***， `ucid`，***击杀与死亡计数***，***完成任务的次数***等等。

  > ***DCS***的 `分数触发器`太过于抽象，且再服务器重启后会被清空。

- 如何实现:

  ***DCS*** 在其游戏目录中预置了`LuaSocket 2.0`（官网上是这么说的。我的设想是:

  - 建立`Mysql`数据库用于保存玩家信息
  - 使用`DCS Hook`处理玩家事件
  - 使用`LuaSocket`以及`Netty`来将***DCS***与管理系统相连
  - 使用`SpringBoot`以及`Hibernate`来控制系统以及数据库

## 一些详细设计（碎碎念+笔记）

### 数据传输

***数据传输***是这套系统中最重要的，我们要保证数据传输不会、至少***不太会影响游戏性能***，同时又能简明扼要的***完成任务***。

***我的设计是使用`json`格式传输数据。具体的格式如下:***

```
// 玩家进行事件后
{
    // 请求ID - 具体来说就是用来判别你发送的这个信息是要干什么（记录数据？还是玩家发送了查询的指令？） 
    // 0代表有玩家登录了，开始准备记录数据。
    // 1代表玩家想要查询自己的点数
    // 2代表玩家完成了某种任务或者阵亡，对pts请求的修改
    requestId: 0,
    
    // 玩家唯一标识ID - 这个是玩家的唯一标识，是DCS的一个属性，是根据客户端来的，所以更改游戏名也没有用。
    playerUcId: '51f6sa1df61s6d1af',
    
    // pts - 很容易理解，玩家积攒的点数。至于点数能干什么，就靠地图制作者的脑部啦（比如说扔核弹？）（不
    // *只有服务端发送数据才会有这个属性。
    pts: 0,
    
    // 玩家游戏名 - 呃呃这个应该不用解释了吧。专门放个这个主要是为了每次登录查询玩家名称是否有更改，如果有更改就更新数据库
    name: "DeathGun",
    
    // 对空战果 - 晚点再设计吧，对空战果可以简单的自增，应该没有什么很大的难度
    // *只有服务端发送数据才会有这个属性。
   	splashAA: 0,
    
    // 对地战果 - 同上
    // *只有服务端发送数据才会有这个属性。
    splashAG: 0,
    
    // 对海战果 - 同上
    // *只有服务端发送数据才会有这个属性
    splashSEAD: 0,
    
    // 被击毁的次数 - 菜鸟驿站，想寄就寄
    // *只有服务端发送数据才会有这个属性。
    dead: 0,
    
    // 需要增加的计数器:
    // 当requestId = 2 时 检测这一项并向数据库内添加计数器
    tracker: AA // AA 或者 AG 或者 dead 或者 SEA
    
}
```

#### 数据库的设计:

| 属性名     | 属性类型   | 用途         | 约束             |
| ---------- | ---------- | ------------ | ---------------- |
| id         | int(10)    | 索引         | primary key      |
| ucid       | String(35) | 用户唯一标识 | not null, unique |
| name       | String(25) | 用户名       | not null,        |
| pts        | int(10)    | 玩家点数     | default 0        |
| splashAA   | int(10)    | 对空战果     | default 0        |
| splashAG   | int(10)    | 对地战果     | default 0        |
| splashSEAD | int(10)    | 对海战果     | default 0        |
| dead       | int(10)    | 死亡次数     | default 0        |
| landing    | int(10)    | 成功着陆次数 | default 0        |

### DCS方面的一些设计 - `Lua`

----

我们首先要在玩家***尝试链接服务器***的时候，获取他的一些信息，比如`ucid`，`name`。 随后我们需要开始对数据库进行操作，判断是否需要插入一条新的记录，也就是说------***注册***。我们会用`DCS Hook`来对服务器发起`request`。给服务器发送一个`Json`表明有玩家进入了。

总体来讲，就是使用:

- `onPlayerTrySendChat`来获取用户输入的`-myinfo`来执行查询操作

- `onGameEvent`来处理***玩家事件***
  - `eventName`获取***事件名称（类型）***
    - `connect` :玩家***连入***时，发送一段`json`用于注册
    
    - `crash` :玩家***阵亡***时，发送一段`json`用于更新 ***死亡计数器*** 同时扣除分数
    
    - `landing` :玩家***着陆***时，发送一段`json`用于更新 ***着陆计数器*** 同时增加分数
    
    - `kill` :玩家***获得击杀***时，发送一段`json`用于更新 ***相应的计数器*** 同时增加分数
    
      >  获得击杀时，客户端发送的`json`中的`tracker`字段则是需要增加的相应计数器

```lua
package.path  = package.path..";.\\LuaSocket\\?.lua"
package.cpath = package.cpath..";.\\LuaSocket\\?.dll"

socket = require("socket")
host = "127.0.0.1"
port = 23333
c = socket.try(socket.connect(host, port))
c:setoption("tcp-nodelay", true)
c:settimeout(0.2)
file = "/"

runner ={}

function runner.onPlayerTrySendChat(playerID, msg, all)
    -- 用于玩家查询信息的hook
    if(msg == '-myinfo') then
        playerReport = nil
        local playertable = net.get_player_info(playerID)
        local playerucid = tostring(playertable["ucid"])
        local playerName = tostring(playertable["name"])
        local dataSend = "{requestId: 1, ucid: '" .. playerucid .. "'}"
        c:send(dataSend)

        playerReport = receive_message()
        
        if(playerReport) then
            net.send_chat_to(playerReport, playerID)
        end
        return ""
    end

    

end

function runner.onGameEvent(eventName,arg1,arg2,arg3,arg4,arg5,arg6,arg7)
    -- --"friendly_fire", playerID, weaponName, victimPlayerID
    -- --"mission_end", winner, msg
    -- --"kill", killerPlayerID, killerUnitType, killerSide, victimPlayerID, victimUnitType, victimSide, weaponName
    -- --"self_kill", playerID
    -- --"change_slot", playerID, slotID, prevSide
    -- --"connect", playerID, name
    -- --"disconnect", playerID, name, playerSide, reason_code
    -- --"crash", playerID, unit_missionID
    -- --"eject", playerID, unit_missionID
    -- --"takeoff", playerID, unit_missionID, airdromeName
    -- --"landing", playerID, unit_missionID, airdromeName
    -- --"pilot_death", playerID, unit_missionID

    if(eventName) then

        -- 有玩家连接
        if(eventName == "connect") then
            local playertable = net.get_player_info(arg1)
            local playerucid = tostring(playertable["ucid"])
            local playerName = tostring(playertable["name"])
            local playerData = "{requestId: 0, ucid: '" .. playerucid .. "', name: '" .. playerName .. "'}"
            c:send(playerData)
        end

        -- 玩家阵亡 -10
        if(eventName == "crash") then
            local playertable = net.get_player_info(arg1)
            local playerucid = playertable["ucid"]
            
            local ptsAdd = -10
            local output = "你的分数变化: " .. tostring(ptsAdd)
            net.send_chat_to(output, arg1)

            local dataSend = "{ requestId: 2, ucid: '" .. playerucid .. "', ptsAdd: " .. tostring(ptsAdd) .. ", tracker: 'dead' }"
            c:send(dataSend)
            
            net.send_chat_to(receive_message(), arg1)
        end

        -- 玩家成功降落
        if(eventName == "landing") then
            local playertable = net.get_player_info(arg1)
            local playerucid = playertable["ucid"]

            local ptsAdd = 20
            local output = "成功降落! 你的分数变化: +" .. tostring(ptsAdd)
            net.send_chat_to(output, arg1)

            local dataSend = "{ requestId: 2, ucid: '" .. playerucid .. "', ptsAdd: " .. tostring(ptsAdd) .. ", tracker: 'landing' }"
            c:send(dataSend)

        end



        -- 获得击杀
        if(eventName == "kill") then

            grandTarget = {"BTR_D", "bofors40", "T-90", "snr s-125 tr", "Patriot EPP", "Patriot str", "Patriot AMG", "Patriot ECS", "Patriot In", "Patriot cp", "M 818", "M1097 avenger", "Soldier stinger", "M1126 Stryker ICV", "Vulcan", "M1043 HMMWV Armament", "M-1 Abrams", "M978 HEMTT Tanker", "Hummer", "S-300PS 64H6E", "M-113"}
            airTarget = {"MiG-29S", "F-15E", "F-14B", "H-6J"}
            seaTarget = {"USS_Arleigh_Burke_IIA", "KUZNECOW"}


            local playertable = net.get_player_info(arg1)
            local playerucid = playertable["ucid"]

            net.send_chat(tostring(playertable["name"]) .. "获得了击杀: " .. tostring(arg5), true)

            -- Unit.Category = {
            --   AIRPLANE      = 0,
            --   HELICOPTER    = 1,
            --   GROUND_UNIT   = 2,
            --   SHIP          = 3,
            --   STRUCTURE     = 4
            -- }

            -- 击杀地面单位与直升机 +30
            for i, v in pairs(grandTarget) do
                if arg5 == v then
                    local ptsAdd = 30
                    local output = "你获得了对地击杀！ 你的分数变化: +" .. tostring(ptsAdd)
                    net.send_chat_to(output, arg1)

                    local dataSend = "{ requestId: 2, ucid: '" .. playerucid .. "', ptsAdd: " .. tostring(ptsAdd) .. ", tracker: 'AG' }"
                    c:send(dataSend)
                end
            end
			
            -- 击杀海上单位 +250
            for i, v in pairs(seaTarget) do
                if arg 5 == v then
                    local ptsAdd = 250
                    local output = "你获得了对海击杀！ 你的分数变化: +" .. tostring(ptsAdd)
                    net.send_chat_to(output, arg1)

                    local dataSend = "{ requestId: 2, ucid: '" .. playerucid .. "', ptsAdd: " .. tostring(ptsAdd) .. ", tracker: 'SEA' }"
                    c:send(dataSend)
            end

            -- 击杀空中单位 +100
            for i, v in pairs(airTarget) do
                if arg5 == v then
                    local ptsAdd = 100
                    local output = "你获得了对空击杀 你的分数变化: +" .. tostring(ptsAdd)
                    net.send_chat_to(output, arg1)

                    local dataSend = "{ requestId: 2, ucid: '" .. playerucid .. "', ptsAdd: " .. tostring(ptsAdd) .. ", tracker: 'AA' }"
                    c:send(dataSend)
                end
            end

        end

    end
end

function receive_message()
    repeat
        chunk, status, partial = c:receive()
        if(chunk) then
            output = tostring(chunk)
        end
    until (chunk == nil and output ~= nil)
    chunk = nil
    return tostring(output)
end

DCS.setUserCallbacks(runner)
```

```
{requestId: 0, ucid: "eee6a222e156f7f1599e435a9e4163a8", name: "DeathGun"} // 用于测试的数据.
```

### 服务端处理接收到的信息 - `Java`

我们选择使用`Hibernate`操纵数据库，关于数据源的设置:

> 在最后我会使用docker来进行打包，所以数据源并不需要更具使用者来更改。但是在开发试用阶段，需要根据运行环境来进行更改。

```yaml
# application.yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url:  jdbc:mysql://localhost:3306/dcs?serverTimeZone=GMT%2B8&useSSL=true
    username: root
    password: chentianhao
    type: com.zaxxer.hikari.HikariDataSource
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        new_generator_mappings: false
        format_sql: true
        show_sql: false
    database-platform: mysql


logging:
  level:
    com.oneonline.teaching.admin: debug

```

#### `Java`项目总体结构

```
- src/main/java # 项目类路径
	- cn.deathgun.dcsluasocket # 项目包
		- dao  # 存放 Repositroy
		- po  # 存放实体类 Player
		- service  # 事务层 存放事务层接口
			- impl  # 事务层实现类
		- utils  # 各种工具，其中包含了JSONOBject，R集。Swagger以及Netty的配置和Bean也在其中
- resources  # 资源目录 存放2个springboot配置文件以及log4j配置

```

#### 代码解构

> 范例，并不展示全部代码

- `Dao`层

  - 我们使用如下的代码来对用户进行注册，使用`Hibernate`以及`JPA`的`@Query`注解来执行。

  ```java
  @Repository
  @Transactional
  public interface PlayerRepository extends JpaRepository<Player, Integer> {
      @Modifying
      @Query(value = "INSERT INTO player(ucid, name) values(:ucid, :name)", nativeQuery = true)
      void insertNewPlayer(@Param("ucid") String ucid, @Param("name") String name);
  }
  ```

- `Po`层

  - `Hibernate`的`Entity`

  ```java
  package cn.deathgun.dcsluasocket.po;
  
  import jakarta.persistence.Entity;
  import jakarta.persistence.Table;
  import lombok.Data;
  
  import jakarta.persistence.*;
  import lombok.Data;
  import lombok.Generated;
  
  @Entity
  @Data
  @Table(name = "player", schema = "dcs")
  public class Player {
  
      private static final long serialVersionUID = 1L;
  
      @Id
      @GeneratedValue(strategy = GenerationType.AUTO)
      @Column(name = "id")
      private Integer id;
  
      @Column(name = "ucid")
      private String ucid;
  
      @Column(name = "name")
      private String name;
  
      @Column(name = "pts")
      private Integer pts;
  
      @Column(name="splashAA")
      private Integer splashAA;
  
      @Column(name = "splashAG")
      private Integer splashAG;
  
      @Column(name = "splashSEAD")
      private Integer splashSEAD;
  
      @Column(name = "dead")
      private Integer dead;
  
      @Column(name = "landing")
      private Integer landing;
  }
  
  ```

- `Service`层

  - 这一层调用了`Dao`层的方法，完成用户的一些事务操作

  ```java
  package cn.deathgun.dcsluasocket.service.impl;
  
  import cn.deathgun.dcsluasocket.dao.PlayerRepository;
  import cn.deathgun.dcsluasocket.po.Player;
  import cn.deathgun.dcsluasocket.service.PlayerService;
  import jakarta.annotation.Resource;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.stereotype.Component;
  import org.springframework.stereotype.Service;
  
  
  @Service("playerService")
  public class PlayerServiceImpl implements PlayerService {
  
      @Autowired
      private PlayerRepository playerDao;
  
      @Override
      public void registeNewPlayer(String ucid, String name) {
          playerDao.insertNewPlayer(ucid, name);
  
      }
  }
  ```

- `Netty` 处理信息

  - 使用`NettyHandler`类中的`channelRead`方法来读取客户端发送来的消息并加以处理

  ```java
  		// 解析发送的文本
          JSONObject userInput = JSONObject.parseObject(msg);
  
          // 用户请求类型id
          Integer userRequestId = userInput.getInteger("requestId");
  
          // 用户名称
          String userName = userInput.getString("name");
  
          // 唯一标识id
          String ucid = userInput.getString("ucid");
  
          // 查询数据库是否有该用户
          Player playerInfo = playerService.checkPlayInfoByUcid(ucid);
  
          switch (userRequestId) {
              // 用户登录 - requestid为0
              case 0 -> {
                  // 如果数据库中没有这个玩家就注册
                  if (playerInfo == null) {
                      playerService.registeNewPlayer(ucid, userName);
                  } else { // 如果数据库有数据
                      // 检验玩家名称是否更改
                      if (!Objects.equals(userName, playerInfo.getName())) {
                          playerService.updatePlayerName(ucid, userName);
                      }
                  }
              }
  ```

### 单位的TypeName集

#### 地面单位

| TypeName             | Name                                     |
| -------------------- | ---------------------------------------- |
| BTR_D                | BTR-RD 装甲运输车                        |
| bofors40             | 博福斯40毫米高炮                         |
| T-90                 | T-90 主战坦克                            |
| snr s-125 tr         | SA-3 "涅瓦河" SNR-125 "低击" 跟踪雷达    |
| Patriot EPP          | "爱国者" 防空导弹 EPP-III 供电车         |
| Patriot str          | "爱国者" 防空导弹 AN/MPQ-33 搜素跟踪雷达 |
| Patriot AMG          | "爱国者" 防空导弹 AN/MRC-137 通信天线车  |
| Patriot ECS          | "爱国者" 防空导弹 AN/MSQ-104 作战控制站  |
| Patriot In (或者ln?) | "爱国者" 防空导弹 M901 发射车            |
| Patriot cp           | "爱国者" 防空导弹 信息协调中心           |
| M 818                | M939 重型卡车                            |
| M1097 avenger        | M1097 "复仇者" 野战防空系统              |
| Soldier stinger      | "毒刺" 便携式防空导弹 发射员             |
| M1126 Stryker ICV    | M1126 "斯特瑞克" 装甲运输车              |
| Vulcan               | M163 "火神" 自行高炮                     |
| M1043 HMMWV Armament | M1043 "悍马" 装甲运输车                  |
| M-1 Abrams           | M1A2 "艾布拉姆斯" 主战坦克               |
| M978 HEMTT Tanker    | M978 油罐卡车                            |
| Hummer               | M1025 "悍马" 装甲运输车                  |
| S-300PS 64H6E        | S-300PS 地空导弹 64H6E 搜索雷达          |
| M-113                | M-113 装甲运输车                         |

#### 空中单位

| TypeName | Name    |
| -------- | ------- |
| MiG-29S  | MiG-29S |
| F-15E    | F-15E   |
| F-14B    | F-14B   |
| H-6J     | 轰6     |
|          |         |
|          |         |
|          |         |
|          |         |
|          |         |
|          |         |
|          |         |
|          |         |

#### 海上单位

| TypeName              | Name             |
| --------------------- | ---------------- |
| USS_Arleigh_Burke_IIA | 阿利伯克级       |
| KUZNECOW              | 库兹涅佐夫号航母 |
|                       |                  |

