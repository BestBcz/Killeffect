fork of aKilleffect "is a killeffect plugin of AynClub."

Base on Minecraft 1.7.10

雷击（lightning）和雷暴（thunder）通过 MicetPvP 的比赛接口确定接收者：仅当前比赛的参赛者和观察者接收客户端闪电、声音及雷暴粒子，不生成服务器世界闪电。雷暴持续期间会重新检查比赛归属，已回大厅或进入其他比赛的玩家不会继续接收。未安装/启用 MicetPvP、没有比赛或接口异常时跳过这两种特效。其他击杀特效不受此规则影响。

复用现有 `getMatchHandler().getMatchPlayingOrSpectating(Player)` 接口，无需更新 Practice 插件；普通击杀及 Practice 已有的环境淘汰联动均适用。

鸣谢开源项目：https://github.com/Marcraft9/AKE
