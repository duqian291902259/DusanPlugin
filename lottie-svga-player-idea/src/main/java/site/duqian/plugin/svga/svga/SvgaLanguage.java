package site.duqian.plugin.svga.svga;

import com.intellij.lang.Language;

class SvgaLanguage extends Language {
    private static final String ID = "svga";
    static final SvgaLanguage INSTANCE = new SvgaLanguage();
    private SvgaLanguage() {
        super(ID);
    }
}
