/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.securechat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.example.telnet.TelnetServer;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextGMBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.File;

/**
 * Simple SSL chat server modified from {@link TelnetServer}.
 */
public final class SecureChatServer {

    static final int PORT = Integer.parseInt(System.getProperty("port", "8992"));
    static final String TRUST_CERT = "MIICTTCCAfKgAwIBAgIKZCTXgL0MKPOtBzAMBggqgRzPVQGDdQUAMF0xCzAJBgNV\n" +
            "BAYTAkNOMTAwLgYDVQQKDCdDaGluYSBGaW5hbmNpYWwgQ2VydGlmaWNhdGlvbiBB\n" +
            "dXRob3JpdHkxHDAaBgNVBAMME0NGQ0EgVEVTVCBDUyBTTTIgQ0EwHhcNMTIxMjI1\n" +
            "MTIyNTA2WhcNMzIwNzIzMTIyNTA2WjBcMQswCQYDVQQGEwJDTjEwMC4GA1UECgwn\n" +
            "Q2hpbmEgRmluYW5jaWFsIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MRswGQYDVQQD\n" +
            "DBJDRkNBIFRFU1QgU00yIE9DQTEwWTATBgcqhkjOPQIBBggqgRzPVQGCLQNCAAQz\n" +
            "uFgJbedY55u6NToJElGWzPT+9UF1dxcopnerNO3fqRd4C1lDzz9LJZSfmMyNYaky\n" +
            "YC+6zh9G6/aPXW1Od/RFo4GYMIGVMB8GA1UdIwQYMBaAFLXYkG9c8Ngz0mO9frLD\n" +
            "jcZPEnphMAwGA1UdEwQFMAMBAf8wOAYDVR0fBDEwLzAtoCugKYYnaHR0cDovLzIx\n" +
            "MC43NC40Mi4zL3Rlc3RyY2EvU00yL2NybDEuY3JsMAsGA1UdDwQEAwIBBjAdBgNV\n" +
            "HQ4EFgQUa/4Y2o9COqa4bbMuiIM6NKLBMOEwDAYIKoEcz1UBg3UFAANHADBEAiAR\n" +
            "kDmkQ0Clio48994IUs63nA8k652O2C4+7EQs1SSbuAIgcwNUrHJyEYX8xT5BKl9T\n" +
            "lJOefzCNNJW5Z0f3Y/SjaG0=";
    static final String ENC_CERT = "MIIC0DCCAnOgAwIBAgIFEAMxBWAwDAYIKoEcz1UBg3UFADBcMQswCQYDVQQGEwJD\n" +
            "TjEwMC4GA1UECgwnQ2hpbmEgRmluYW5jaWFsIENlcnRpZmljYXRpb24gQXV0aG9y\n" +
            "aXR5MRswGQYDVQQDDBJDRkNBIFRFU1QgU00yIE9DQTEwHhcNMTYxMDIxMDkxNzE1\n" +
            "WhcNMTkxMDIxMDkxNzE1WjB1MQswCQYDVQQGEwJjbjEXMBUGA1UECgwOQ0ZDQSBU\n" +
            "ZXN0IE9DQTExGjAYBgNVBAsMEUxvY2FsIFJBIFRFU1RPQ0ExMRQwEgYDVQQLDAtX\n" +
            "ZWIgU2VydmVyczEbMBkGA1UEAwwSd3d3LmViYW5rOTY1MTguY29tMFkwEwYHKoZI\n" +
            "zj0CAQYIKoEcz1UBgi0DQgAEW23hZkRRR/FYHnVMAEyjzt8rbO+j8aSLe67MAs+C\n" +
            "64UlVhS0uZBW7mCvvM/Ug3CGiIgT7cwNtVZVHMg7EK8bQaOCAQUwggEBMB8GA1Ud\n" +
            "IwQYMBaAFGv+GNqPQjqmuG2zLoiDOjSiwTDhMEgGA1UdIARBMD8wPQYIYIEchu8q\n" +
            "AQEwMTAvBggrBgEFBQcCARYjaHR0cDovL3d3dy5jZmNhLmNvbS5jbi91cy91cy0x\n" +
            "NC5odG0wDAYDVR0TAQH/BAIwADA4BgNVHR8EMTAvMC2gK6AphidodHRwOi8vdWNy\n" +
            "bC5jZmNhLmNvbS5jbi9TTTIvY3JsMTAwNS5jcmwwDgYDVR0PAQH/BAQDAgM4MB0G\n" +
            "A1UdDgQWBBTYrX9ThAvgMhdt/MbbtD6zHWEp+zAdBgNVHSUEFjAUBggrBgEFBQcD\n" +
            "AgYIKwYBBQUHAwEwDAYIKoEcz1UBg3UFAANJADBGAiEA1KTh3GO8dHoV4AB9rVP3\n" +
            "kXa6YiuA+ejjFJkvwaxyyFoCIQC2VLoIdXbbnLO4Ld7JvbHJWelp1sYoK4xEarGz\n" +
            "GAqCpQ==";
    static final String ENC_KEY = "MHgCAQEEIQDsnQ/bup2+WH1xj2XElJXkxS5vD4PXCDNk0scgabNIp6AKBggqgRzP\n" +
            "VQGCLaFEA0IABFtt4WZEUUfxWB51TABMo87fK2zvo/Gki3uuzALPguuFJVYUtLmQ\n" +
            "Vu5gr7zP1INwhoiIE+3MDbVWVRzIOxCvG0E=";
    static final String SIGN_CERT = "MIICzjCCAnOgAwIBAgIFEAMxBVkwDAYIKoEcz1UBg3UFADBcMQswCQYDVQQGEwJD\n" +
            "TjEwMC4GA1UECgwnQ2hpbmEgRmluYW5jaWFsIENlcnRpZmljYXRpb24gQXV0aG9y\n" +
            "aXR5MRswGQYDVQQDDBJDRkNBIFRFU1QgU00yIE9DQTEwHhcNMTYxMDIxMDkxNzE1\n" +
            "WhcNMTkxMDIxMDkxNzE1WjB1MQswCQYDVQQGEwJjbjEXMBUGA1UECgwOQ0ZDQSBU\n" +
            "ZXN0IE9DQTExGjAYBgNVBAsMEUxvY2FsIFJBIFRFU1RPQ0ExMRQwEgYDVQQLDAtX\n" +
            "ZWIgU2VydmVyczEbMBkGA1UEAwwSd3d3LmViYW5rOTY1MTguY29tMFkwEwYHKoZI\n" +
            "zj0CAQYIKoEcz1UBgi0DQgAEpeJbEJ3nEpm3v7Yhm9bc/uCJBW9YWeD89aH1u+Pp\n" +
            "dQtnsFqh3nfQVwel15MoxB8KirUCjQbZ9jRd8POklZrOEqOCAQUwggEBMB8GA1Ud\n" +
            "IwQYMBaAFGv+GNqPQjqmuG2zLoiDOjSiwTDhMEgGA1UdIARBMD8wPQYIYIEchu8q\n" +
            "AQEwMTAvBggrBgEFBQcCARYjaHR0cDovL3d3dy5jZmNhLmNvbS5jbi91cy91cy0x\n" +
            "NC5odG0wDAYDVR0TAQH/BAIwADA4BgNVHR8EMTAvMC2gK6AphidodHRwOi8vdWNy\n" +
            "bC5jZmNhLmNvbS5jbi9TTTIvY3JsMTAwNS5jcmwwDgYDVR0PAQH/BAQDAgbAMB0G\n" +
            "A1UdDgQWBBRrj81BmUI076WmThf7IVGmWapoODAdBgNVHSUEFjAUBggrBgEFBQcD\n" +
            "AgYIKwYBBQUHAwEwDAYIKoEcz1UBg3UFAANHADBEAiAu316RMaa2nfscBSVeDeB6\n" +
            "OGNjSeYo7apL1RLi02UcogIgeFaG4y4TUvjeuMV2Ly1DCO3N/1jHEX/AKponBKaL\n" +
            "p0o=";
    static final String SIGN_KEY = "MHcCAQEEIHloT3sonhjsO4PWtk7Af76igvXzs05ZU3QF5Wght2GioAoGCCqBHM9V\n" +
            "AYItoUQDQgAEpeJbEJ3nEpm3v7Yhm9bc/uCJBW9YWeD89aH1u+PpdQtnsFqh3nfQ\n" +
            "Vwel15MoxB8KirUCjQbZ9jRd8POklZrOEg==";


    public static void main(String[] args) throws Exception {
//        SelfSignedCertificate ssc = new SelfSignedCertificate();
//        /*SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
//            .build();*/
        SslContext sslCtx = SslContextGMBuilder.forServer(ENC_CERT, ENC_KEY, SIGN_CERT, SIGN_KEY, null)
                /* 默认协商出来的是ECDHE_SM4_SM3算法，所以必须是双向SSL，并且客户端和服务端必须要有加密证书和签名证书 */
                .clientAuth(ClientAuth.REQUIRE)
                .build();

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new SecureChatServerInitializer(sslCtx));

            b.bind(PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
