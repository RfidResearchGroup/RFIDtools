package cn.rrg.rdv.util;

import java.util.ArrayList;
import java.util.Arrays;

import cn.dxl.common.util.ArrayUtils;
import cn.dxl.common.util.StringUtil;

/*
 * 行对比工具!
 * */
public class LineEqualUtil {

    //存放的是被提交的字符串数组!
    private ArrayList<String[]> mDatas = new ArrayList<>();

    //存放的是被统计到的一些信息!
    private int blockDiffCount = 0;
    private int allDiffCount = 0;

    /*
     * 提交字符串数组!
     * */
    public void putData(String[]... arrs) {
        mDatas.addAll(Arrays.asList(arrs));
    }

    /*
     * 移除指定索引数组!
     * */
    public void removeData(int index) {
        mDatas.remove(index);
    }

    /*
     * 移除所有的数据数组!
     * */
    public void removeAll() {
        mDatas.clear();
        blockDiffCount = 0;
        allDiffCount = 0;
    }

    /*
     * 最终的对比方法!
     * @param left 第一行数据!
     * @param right 第二行数据!
     * @return 对比的结果!
     * */
    public String x(String top, String bottom) {
        if (top == null && bottom == null) return "???";
        if (bottom == null || top == null) return "---";
        //两个块不相同，进行标志位自增,并且可以进行下一轮判断!
        if (top.equalsIgnoreCase(bottom)) return "\n\n";
        else ++blockDiffCount;
        StringBuilder sb = new StringBuilder(32);
        //最终需要在此处取出每个数据所在的数组，然后迭代判断，最终输出结果!
        char[] l = top.toCharArray();
        char[] r = bottom.toCharArray();
        //取最大值
        int max = Math.max(l.length, r.length);
        char[] maxChars = l.length >= r.length ? l : r;
        char[] minChars = r.length <= l.length ? r : l;
        for (int i = 0; i < max; i++) {
            //标志，是否相同，是否需要添加x字符到构造器!
            boolean n;
            //判断是否越界!
            if (i >= minChars.length) {
                n = true;
            } else n = (maxChars[i] != minChars[i]);
            //最终判断是否需要添加x字符填充，不需要则拿空格填充!
            if (n) {
                //追加x填充
                sb.append('x');
                //进行所有的不同之处计数
                ++allDiffCount;
            } else sb.append(' ');
        }
        //在两行中间进行添加结果!
        String ret = sb.toString();
        return StringUtil.isEmpty(ret) ? "\n\n" : ('\n' + ret + '\n');
    }

    /*
     * 最终的源数据集处理结果，取出所有的存放在数组里的数据进行对比!
     * */
    public String[] finalResult() {
        ArrayList<String> rets = new ArrayList<>(64);
        //进行迭代!
        for (int i = 0; i < mDatas.size(); i++) {
            //双重迭代，拿当前第一个冒泡第二个!
            for (int j = (i + 1); j < mDatas.size(); j++) {
                //三重迭代，实际是迭代里面的值了!
                String[] left = mDatas.get(i);
                String[] right = mDatas.get(j);
                //Log.d("***", "当前拿的是那个索引的数据: " + i);
                //Log.d("***", "当前拿的是那个索引的数据: " + j);
                int max = Math.max(left.length, right.length);
                //进行判断，输出的格式为left 在上，right在下,x填充符在中!
                for (int k = 0; k < max; k++) {
                    //判断达到了上限，即将越界，需要严格把控!
                    String top = ArrayUtils.getElement(left, k);
                    //Log.d("***", "当前的对比的上行数据 " + k + ": " + top);
                    String bottom = ArrayUtils.getElement(right, k);
                    //Log.d("***", "当前的对比的下行数据 " + k + ": " + bottom);
                    //进行最终的判断!
                    String xStr = x(top, bottom);
                    //进行最终的存储!
                    String contact = top + xStr + bottom;
                    rets.add(contact);
                }
            }
        }
        return rets.toArray(new String[0]);
    }

    /*
     * 得到统计计数
     * */
    public int getBlockDiffCount() {
        return blockDiffCount;
    }

    /*
     * 获得所有的不同之处的统计!
     * */
    public int getAllDiffCount() {
        return allDiffCount;
    }

    /*
     * 重置统计计数!
     * */
    public void resetCount() {
        blockDiffCount = 0;
        allDiffCount = 0;
    }

}
