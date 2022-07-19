package site.duqian.plugin.svga.svga;

import com.intellij.lang.Language;

class Svga extends Language {

    private static final String ID = "SVGA";
    static final Svga INSTANCE = new Svga();
    private Svga() {
        super(ID);
    }
}
