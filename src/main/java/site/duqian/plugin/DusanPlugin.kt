package site.duqian.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages
import site.duqian.plugin.base.*
import site.duqian.plugin.entity.ChatGptResult

/**
 * Description:Hi from 杜小菜
 *
 * Created by 杜小菜 on 2022/9/28 - 09:39.
 * E-mail: duqian2010@gmail.com
 */
class DusanPlugin : AnAction() {
    companion object {
        private const val TAG = "dq-DusanPlugin"
        /**
         *Kotlin is a statically typed, cross-platform, general-purpose programming language developed by JetBrains. It is designed to interoperate fully with Java and is an officially supported language for Android development. It is also the language used by the Spring Boot framework.
         */
        //private const val QUESTION = "What’s Kotlin?"
        /**
         *IDEA plugin development is the process of creating plugins for the IntelliJ IDEA development environment. These plugins can be used to extend the functionality of the IDE, allowing developers to customize their development experience. Plugins can be used to add new features, improve existing features, or even create entirely new applications within the IDE.
         */
        private const val QUESTION = "What’s IDEA plugin development?"
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT)
        val basePath = if (project != null) project.basePath ?: "" else ""
        Messages.showMessageDialog(
            project, "Hello，ChatGpt:$QUESTION", "Question from 杜小菜", Messages.getInformationIcon()
        )

        try {
            testChatGpt(basePath)
            //UIUtils.checkPython(basePath,false)
        } catch (e: Exception) {
            LogUtil.i("execute cmd error = $e")
        }
    }

    /**
     * curl https://api.openai.com/v1/completions \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer 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" \
    -d '{
    "model": "text-davinci-003",
    "prompt": "Q: What’s Kotlin?\nA:",
    "temperature": 0,
    "max_tokens": 100,
    "top_p": 1,
    "frequency_penalty": 0.0,
    "presence_penalty": 0.0,
    "stop": ["\n"]
    }'
     */
    private fun testChatGpt(basePath: String) {
        val cmdList = mutableListOf<String>()
        cmdList.add("curl")
        cmdList.add("https://api.openai.com/v1/completions")
        cmdList.add("\\")
        cmdList.add("-H")
        cmdList.add("Content-type: application/json")
        cmdList.add("\\")
        cmdList.add("-H")
        cmdList.add("Authorization: Bearer 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")
        cmdList.add("\\")
        cmdList.add("-d")
        cmdList.add(
            "{\n" + "    \"model\": \"text-davinci-003\",\n" + "    \"prompt\": \"${QUESTION}\\nA:\",\n" + "    \"temperature\": 0,\n" + "    " + "\"max_tokens\": 100,\n" + "    \"top_p\": 1,\n" + "    \"frequency_penalty\": 0.0,\n" + "    \"presence_penalty\": 0.0,\n" + "    \"stop\": [\"\\n\"]\n" + "    }"
        )
        cmdList.add("-o")
        val savedJsonFileName = "chat_gpt_result.json"
        cmdList.add(savedJsonFileName)

        SysCmdUtil.executeCmd(cmdList, basePath, object : CmdCallback {
            override fun onResult(success: Boolean, cmdResult: String?) {
                LogUtil.i("$TAG testChatGpt result=$cmdResult")
                val path = "$basePath/$savedJsonFileName"
                val result = IOUtil.getFileContent(path, false) ?: ""
                LogUtil.i("$TAG testChatGpt getFileContent=$result,path=$path")

                val chatGptResult: ChatGptResult? = JsonUtil.toBean(result, ChatGptResult::class.java)
                val text = chatGptResult?.choices?.get(0)?.text
                LogUtil.i("$TAG ChatGpt answer=$text")
                if (text?.isEmpty() == false) {
                    UIUtils.showMessageDialog("ChatGpt answer", text, Messages.getInformationIcon())
                    return
                }
                UIUtils.showMessageDialog("ChatGpt error", result, Messages.getErrorIcon())
            }
        })
    }
}