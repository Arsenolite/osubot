# 白菜。
## 一个依赖于酷Q Websocket插件（Lemoc）的QQ机器人。使用java编写，面向osu群。

# 已停止维护，迁移到cabbageWeb，依赖HTTPAPI。
### 具有的特性：
------
+ 绘制指定玩家的名片（所有组件坐标、字体、字体尺寸、颜色，以及布局、模板都可以自定义）。
+ 绘制指定玩家的今日BP。
+ 每天凌晨4点（北京时间）将所有在机器人查询过的玩家数据，写入数据库，并提供命令，可以把对比数据打印在名片上。（如果请求的那天没有数据，会自动查找最接近的日子。）
+ 查询所有玩家数据中PP超限的玩家。支持设置警戒线（高过警戒线的才查询osu API获取当前PP）。
+ 支持用户组。可以根据用户组自定义名片背景。提供命令修改/重置用户组，还可以直接用命令指定某用户组的图片。


### 如何使用：
------
1. 安装MySQL，JRE。（目前数据库名称和密码写死为root:123456，如果有需求可以修改为读取properties）
2. 安装酷Q及Lemoc插件，打开Lemoc的WebSocket服务器。
2. git clone本项目，在酷Q目录\data\image\下创建resource文件夹，将项目中resources文件夹下的resource.zip解压过去。
3. 根据需求修改配置/源码（当前布局和坐标一一对应，但是可以完全自由调节，如果设计了新布局）
4. 默认使用mvn assembly:assembly命令（我在pom中指定了maven使用这个插件）打jar包。

### 命令列表：
------
`!stat <String:username> #<int:day> `

查询指定玩家数据，并根据day参数进行对比，**绘制图片**并返回。

`day=0`：不对比。

`day=1`：相当于默认。和当天凌晨的数据进行对比。

`day=x`：与x天前（数据库中x-1天前的凌晨4点数据）的数据进行对比。



`!bp <String:username> #<int:rank>`

如果不带rank参数，则返回指定玩家 今天更新的bp。

现在接受#<rank>参数。返回BP榜上100个BP里，指定位置的BP。

`!setid <String:username>`

将某个osu id绑定在你的QQ号上以便使用下列命令：

`!bpme #<int:rank>`

使用绑定的QQ查询BP

`!statme #<int:day>`

使用绑定的id查询玩家信息

`!recent`

使用绑定的id查询最近的一次游戏记录（包括Fail/Retry的记录。普通用户不开放查询其他玩家。）

------
#### 以下命令只有配置文件中指定的管理员QQ发送才有效~~，并且都支持私聊。~~现在除了`!sudo smoke`外所有命令都支持私聊/群聊
------

`!sudo add <String:username1>,<String:username2>,... <String:role>`

将指定玩家加入数据库。如果传入的是单个玩家，则返回一张默认的stat名片。

`!sudo del <String:username1>,<String:username2>,...`

将指定玩家的权限恢复默认。预置的默认值是"creep"，来自Dota 2中的小兵。

以上两条命令会返回重置/修改成功的玩家，新增的玩家，不存在的玩家，以及出现网络错误的玩家。

`!sudo check <String:username>`

只接受单个玩家作为参数，返回玩家的权限。如果玩家没有使用过，也返回相应的提示。

`!sudo 退群 <String:role>`

作为作者恶趣味的产物，"褪裙"也会被识别。

返回指定用户组中，PP超过配置文件中指定的值的玩家。

配置文件的key值请使用：角色名+PP/角色名+RiskPP。

后者作为警戒线，只有高于警戒线的玩家才会在官网查询最新的信息，其他玩家是直接读取数据库当天凌晨4点的信息。推荐设置为比上限少100的值。


~~`!sudo bg <role> <image>`~~

`!sudo bg <String:role> <String:imageURL>`

将指定的图片设置为指定的用户组所用的背景图，图片~~直接以普通表情的形式发送即可~~实际使用会由于QQ压缩大图，严重影响视觉效果。改为使用URL作为参数，推荐使用imgur（不支持puush）。请务必注意图片的尺寸。

返回成功提示/相应的错误信息。

`!sudo recent <String:username>`

查找指定玩家的最近游戏记录。（防止滥用不开放并不代表权限狗不能用啊。）


`!sudo afk <int:day>:<String:role>`

查询指定用户组中所有玩家，如果有最近活跃时间在day天之前就列出，以便进行下一步处理。

`!sudo smoke <int:QQ>:<int:Second>`

在当前群将指定QQ禁言指定的秒数。这时候机器人使用的QQ必须有管理员权限。


