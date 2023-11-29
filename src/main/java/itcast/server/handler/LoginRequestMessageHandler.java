package itcast.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import itcast.message.LoginRequestMessage;
import itcast.message.LoginResponseMessage;
import itcast.server.service.UserService;
import itcast.server.service.UserServiceFactory;
import itcast.server.session.SessionFactory;

/**
 * @author XuHan
 * @date 2023/11/29 14:40
 * 自定义Handler的时候必须添加
 */
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();
        UserService userService = UserServiceFactory.getUserService();
        boolean login = userService.login(username, password);
        LoginResponseMessage responseMessage;
        if (login) {
            // 绑定用户和channel
            SessionFactory.getSession().bind(ctx.channel(), username);
            responseMessage = new LoginResponseMessage(true, "登陆成功");
        } else {
            responseMessage = new LoginResponseMessage(false, "用户名或者密码错误");
        }
        // 然后再出站 解码 日志 半包粘包处理（倒着来）
        ctx.writeAndFlush(responseMessage);
    }
}
/**

 */