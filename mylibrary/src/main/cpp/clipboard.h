//
// Created by ITEK on 2023/4/5.
//

#ifndef VULKANTEST_CLIPBOARD_H
#define VULKANTEST_CLIPBOARD_H


#include <string>

//获取粘贴板内容
std::string getClipboardText();
//设置粘贴板内容
bool setClipboardText(const std::string &text);

#endif //VULKANTEST_CLIPBOARD_H
