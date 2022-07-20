package site.duqian.plugin.note.data;

import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;
import java.util.List;

/**
 * 数据总线
 */
public class DataBus {
    // 使用linked list，因为这是一个编辑删除多余查询的场景
    public static List<MdNote> DATA_LIST = new LinkedList<>();
    private static String[] HEADER = { "标题", "描述", "文件名", "内容"};
    public static DefaultTableModel TABLE_MODEL = new DefaultTableModel(null,HEADER);

    /**
     * 清空数据
     */
    public static void reset() {
        DATA_LIST.clear();
        // 清空table
        TABLE_MODEL.setRowCount(0);
    }

    /**
     * 添加数据
     * @param mdNote 数据
     */
    public static void addRow(MdNote mdNote) {
        DATA_LIST.add(mdNote);
        System.out.println(DataBus.DATA_LIST);
        // 加入到MdPicker的表中
        TABLE_MODEL.addRow(mdNote.toRow());
    }
}
