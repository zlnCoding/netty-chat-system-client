package zln.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * 描述:
 * 客户端1
 *
 * @auth zln
 * @create 2018-01-25 9:32
 */
public class Client {
    static NioEventLoopGroup nioEventLoopGroup = null;
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            //创建group
            nioEventLoopGroup = new NioEventLoopGroup();
            //创建辅助类
            Bootstrap bootstrap = new Bootstrap();
            //将group添加到辅助类中
            bootstrap.group(nioEventLoopGroup)
                    //设置客户端管道
                    .channel(NioSocketChannel.class)
                    //设置客户端处理类
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new ClientHandle());
                        }
                    });
            //连接服务端
            System.out.println("请输入ip地址及端口号,格式:ip:port");
            Scanner ipport = new Scanner(System.in);
            String[] conf = ipport.nextLine().split(":");

            ChannelFuture sync = bootstrap.connect(new InetSocketAddress(conf[0], Integer.parseInt(conf[1]))).sync();
            System.out.println("客户端启动成功,绑定地址:"+conf[0]+",绑定端口"+conf[1]);
            //向服务端写数据
            while (true) {
                Scanner scanner = new Scanner(System.in);

                String s = scanner.nextLine();
                if ("exit".equals(s)) break;
                sync.channel().writeAndFlush(Unpooled.copiedBuffer(s.getBytes()));
            }
            //异步监听关闭方法 阻塞方法
            sync.channel().closeFuture().sync();
            //关闭group
            nioEventLoopGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }finally {

            //关闭group
            nioEventLoopGroup.shutdownGracefully();
        }


    }
}
