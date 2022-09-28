package site.duqian.plugin.lottie;

import site.duqian.plugin.base.IDataProcessor;

/**
 * Description:Lottie 动画文件解析
 * <p>
 * Created by 杜乾 on 2022/9/28 - 08:09.
 * E-mail: duqian2010@gmail.com
 */
public class LottieJsonProcessor extends IDataProcessor {
    private static final String JSON_ANIMATION_DATA = "{jsonAnimationData}";

    private LottieJsonProcessor() {
    }

    public static LottieJsonProcessor get() {
        return SingleTonHolder.INSTANCE;
    }

    //静态内部类
    private static class SingleTonHolder {
        private static final LottieJsonProcessor INSTANCE = new LottieJsonProcessor();
    }

    @Override
    protected String getTemplateFilePath() {
        return "lottie/localLottie.html";
    }

    @Override
    protected String replaceTemplateContent(String templateContent, String fileContent) {
        return templateContent.replace(JSON_ANIMATION_DATA, fileContent);
    }
}
