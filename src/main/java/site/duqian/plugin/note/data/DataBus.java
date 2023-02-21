package site.duqian.plugin.note.data;

import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;
import java.util.List;

/**
 * EventBus
 */
public class DataBus {
    public static List<MdNote> DATA_LIST = new LinkedList<>();
    private static String[] HEADER = { "标题", "描述", "文件名", "内容"};
    public static DefaultTableModel TABLE_MODEL = new DefaultTableModel(null,HEADER);

    public static void reset() {
        DATA_LIST.clear();
        TABLE_MODEL.setRowCount(0);
    }

    public static void addRow(MdNote mdNote) {
        DATA_LIST.add(mdNote);
        System.out.println(DataBus.DATA_LIST);
        TABLE_MODEL.addRow(mdNote.toRow());
    }
}
