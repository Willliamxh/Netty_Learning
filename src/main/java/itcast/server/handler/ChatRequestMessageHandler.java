package itcast.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import itcast.message.ChatRequestMessage;
import itcast.message.ChatResponseMessage;
import itcast.server.session.SessionFactory;

/**
 * @author XuHan
 * @date 2023/11/29 14:43
 * 这边其实也可以看到泛型的作用，我只想关注ChatRequestMessage的消息，就只在泛型里面加这个类型即可
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        // 我们找到这个消息要发给谁
        String to = msg.getTo();
        // 通过用户名找channel，怎么找呢？ 通过Session，因为我们在login之后 SessionFactory里面存储着channel 和username 的对应关系
        Channel channel = SessionFactory.getSession().getChannel(to);
        // 在线
        if(channel != null){
            // 把消息给传送过去
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(),msg.getContent()));
        }else {// 不在线
            // 向发送者发送一个消息
            ctx.writeAndFlush(new ChatResponseMessage(false,"对方用户不存在或者不在线"));
        }
    }
}

