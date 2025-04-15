package site.duqian.plugin.lottie;

import site.duqian.plugin.base.IDataProcessor;

/**
 * Description:Parse Lottie file and play
 * <p>
 * Created by Davy on 2022/9/28 - 08:09.
 * E-mail: duqian2010@gmail.com
 */
public class LottieJsonProcessor extends IDataProcessor {
    private static final String JSON_ANIMATION_DATA = "{jsonAnimationData}";

    private LottieJsonProcessor() {
    }

    public static LottieJsonProcessor get() {
        return SingleTonHolder.INSTANCE;
    }

    private static class SingleTonHolder {
        private static final LottieJsonProcessor INSTANCE = new LottieJsonProcessor();
    }

    @Override
    protected String getTemplateFilePath() {
        return "lottie/localLottie.html";
    }

    @Override
    protected String replaceTemplateContent(String templateContent, String fileContent) {
        fileContent = fileContent .replace("\n","");
        return templateContent.replace(JSON_ANIMATION_DATA, fileContent);
    }
}
