package com.msgtodo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.SmallTitle

@Composable
fun PrivacyPolicyPage(
    contentPadding: PaddingValues,
    nestedScroll: NestedScrollConnection,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScroll)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        Spacer(Modifier.height(8.dp))

        SmallTitle(text = "数据去哪了")
        GroupCard {
            Note(
                "【会离开设备的】\n" +
                    "· 白名单应用的通知正文\n" +
                    "· 同一会话最近 10 条上文\n" +
                    "· 会话名与发送者\n\n" +
                    "只在「启用大模型提取」打开时发送，发往你自己填的接口地址。\n\n" +
                    "【只存本机的】\n" +
                    "· 消息记录、上下文、待办\n" +
                    "· API 密钥、各项设置\n\n" +
                    "【不会收集】\n" +
                    "· 通讯录、位置、设备标识、应用列表\n\n" +
                    "作者没有服务器 —— 上述内容不经过任何中间方。",
            )
        }
        Gap()

        SmallTitle(text = "用途边界")
        GroupCard {
            Note(
                "· 只处理你本人设备上、你本人收到的通知\n" +
                    "· 关注列表只能本机手工添加，没有远程配置通道\n" +
                    "· 纯自用，不上架、不盈利\n\n" +
                    "严禁用于监控、窥探他人的通信内容。"
            )
        }
        Gap()

        SmallTitle(text = "安全防线（无法关闭）")
        GroupCard {
            Note(
                "无论名单怎么配，消息（含上文）里出现下列内容就一律不外发：\n\n" +
                    "· 「默认账号」「默认密码」及其书写变体\n" +
                    "· 可打开的网址：http/https/ftp 链接、www. 开头、xxx.com 这类域名\n\n" +
                    "局域网地址不拦（192.168.x.x、localhost 等）。\n" +
                    "被拦下的消息照常记录在本机，「消息」页会标明原因。"
            )
        }
        Gap()

        SmallTitle(text = "关于总结范围")
        GroupCard {
            Note(
                "· 两个名单取并集，命中任意一个就总结\n" +
                    "· 匹配忽略大小写、空格和零宽字符\n" +
                    "· 「@所有人」不受名单限制，一律总结\n" +
                    "· 没勾选的消息仍记录在本机，只是不送去总结\n" +
                    "· 改动立刻生效"
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}
