从命令行获取粘贴板内容 支持adb&root
成品在assets目录下
将main.jar push到/data/local/tmp

查看粘贴板内容
app_process64 -Djava.class.path=/data/local/tmp/main.jar /system/bin com.example.mylibrary.Main

设置粘贴板内容
app_process64 -Djava.class.path=/data/local/tmp/main.jar /system/bin com.example.mylibrary.Main "hello world"

启发
https://github.com/wentiancanye/clipboardcli.git
https://github.com/Genymobile/scrcpy.git
