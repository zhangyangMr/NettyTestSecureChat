# 简介

聚拢链netty包example中的SecureChat例子测试代码



# 注意事项

1.本项目未能解决如何把本地依赖包如何打包到jar包中。本项目的解决方式，是把需要的本地jar包放入到/opt/jdk1.8.0_261/jre/lib/ext中进行解决的。

2.需要自己拉取聚拢链的julongchain-netty-tcnative-infosec，julongchain-netty-infosec代码，自己编译安装到本地mvn仓库。

3.编译tcnative包时需要查看本地/lib64/文件夹下是否有libc.so.6文件，没有的话需要自行解决