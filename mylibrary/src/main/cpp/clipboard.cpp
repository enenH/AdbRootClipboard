#include <unistd.h>
#include <sys/stat.h>
#include "Utils.h"
#include "clipboard.h"
#include "java_clipboard.h"

#define JAVA_FILE  "/data/adb/.java_enen"
#define JAVA_PATH "/system/bin"

static bool init_java() {
    if (access(JAVA_FILE, F_OK) == 0) {
        //对比文件大小是否相同
        struct stat file_stat{};
        if (stat(JAVA_FILE, &file_stat) == 0) {
            off_t file_size = file_stat.st_size;
            if (file_size == sizeof(java_clipboard)) {
                return true;
            }
        }
    }
    return WriteFile(JAVA_FILE, (void *) java_clipboard, sizeof(java_clipboard));
}

static std::string exec_command(const std::string &command) {
    char buffer[128];
    std::string result;
    // Open pipe to file
    FILE *pipe = popen(command.c_str(), "r");
    if (!pipe) {
        return {};
    }
    // read till end of process:
    while (!feof(pipe)) {
        // use buffer to read and add to result
        if (fgets(buffer, 128, pipe) != nullptr) {
            result += buffer;
        }
    }
    pclose(pipe);
    return result;
}


std::string getClipboardText() {
    init_java();

    std::string str = exec_command(
            "app_process64 -Djava.class.path=" JAVA_FILE " " JAVA_PATH " com.example.mylibrary.Main");
    if (!str.empty()) {
        if (str.back() == '\n')
            str.pop_back(); // 删除最后一个字符
    }
    return str;
}

bool setClipboardText(const std::string &text) {
    init_java();

    return exec_command(
            "app_process64 -Djava.class.path=" JAVA_FILE " " JAVA_PATH " com.example.mylibrary.Main " + text)
           == "Successed";
}
