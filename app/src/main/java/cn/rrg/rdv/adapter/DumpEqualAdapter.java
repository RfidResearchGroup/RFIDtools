package cn.rrg.rdv.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.dxl.mifare.MifareUtils;
import cn.rrg.rdv.R;
import cn.dxl.common.util.ContextUtil;
import cn.dxl.common.util.TextStyleUtil;

public class DumpEqualAdapter extends AbsResIdArrayAdapter<String> {

    public DumpEqualAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //绘制一下视图!
        View view = convertView;
        if (view == null) view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView showInfo = view.findViewById(R.id.txtShowEqualInfo);
        TextView showBlock = view.findViewById(R.id.txtShowEqualBlock);
        TextView showResult = view.findViewById(R.id.txtShowEqualResult);
        //显示块索引值!
        showBlock.setText(String.valueOf(position));
        //判断是否试起始扇区，是的话就显示扇区索引值
        LinearLayout layout = view.findViewById(R.id.layout_sector_label);
        if (MifareUtils.isFirstBlock(position)) {
            layout.setVisibility(View.VISIBLE);
            //填充扇区值!
            TextView txtShowSector = layout.findViewById(R.id.txtShowSector);
            int sector = MifareUtils.blockToSector(position);
            txtShowSector.setText(String.valueOf(sector));
        } else {
            layout.setVisibility(View.GONE);
        }
        //判断是否需要显示尾部的分割线!
        View endDivider = view.findViewById(R.id.view_divider);
        if (MifareUtils.isTrailerBlock(position)) {
            endDivider.setVisibility(View.VISIBLE);
        } else {
            endDivider.setVisibility(View.GONE);
        }
        //判断是否需要显示块数据的分割线!
        View dataBlckDivider = view.findViewById(R.id.view_divider_1);
        if (!MifareUtils.isTrailerBlock(position)) {
            dataBlckDivider.setVisibility(View.VISIBLE);
        } else {
            dataBlckDivider.setVisibility(View.GONE);
        }
        //显示块对比结果.进行样式设置!
        String content = getItem(position);
        if (content != null) {
            String[] contents = content.split("\n");
            //填充显示数据信息!
            StringBuilder sb = new StringBuilder();
            for (int i = 0, j = 0; i < contents.length; i++) {
                //判断是奇数还是偶数，奇数必定有对比
                if (i % 2 == 0) {
                    //实际数据个数计算!
                    ++j;
                    //奇数直接换行,偶数添加索引再换行!
                    sb.append(": ").append(j);
                    if (i != contents.length - 1)
                        sb.append('\n');
                } else {
                    //未达到结尾则需要直接换行!
                    if (i != contents.length - 1)
                        sb.append("\n");
                }
            }
            showInfo.setText(sb.toString());
            //富文本样式构造器!
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            //判断是奇数还是偶数，奇数必定有对比
            for (int i = 0; i < contents.length; i++) {
                //判断是当前是数据行还是对比行!
                String data = contents[i];
                if (i % 2 == 0) {
                    //偶数行是数据行，数据行需要判断尾部块或者厂商块!
                    if (position == 0) {
                        //厂商块!
                        ssb.append(TextStyleUtil.getStyleString(getContext(), data, R.style.ManufacturerStyle));
                        if (i != contents.length - 1) ssb.append('\n');
                    } else if (MifareUtils.isTrailerBlock(position)) {
                        //尾部块!
                        //尾部块需要进行截取设置，无需最终的换行!!!
                        String keyA = data.substring(0, 12);
                        String access = data.substring(12, 20);
                        String keyB = data.substring(20, 32);
                        ssb.append(TextStyleUtil.merge(
                                TextStyleUtil.getStyleString(getContext(), keyA, R.style.KeyStyle),
                                TextStyleUtil.getStyleString(getContext(), access, R.style.AccessStyle),
                                TextStyleUtil.getStyleString(getContext(), keyB, R.style.KeyStyle)
                        ));
                        if (i != contents.length - 1) ssb.append('\n');
                    } else {
                        //数据块!
                        ssb.append(TextStyleUtil.getColorString(data, new ContextUtil(getContext()).getColor(R.color.md_white_1000)));
                        if (i != contents.length - 1) ssb.append('\n');
                    }
                } else {
                    //奇数行是对比行!
                    ssb.append(TextStyleUtil.getColorString(contents[i], new ContextUtil(getContext()).getColor(R.color.md_red_A700)));
                    if (i != contents.length - 1) ssb.append('\n');
                }
            }
            showResult.setText(ssb);
        } else {
            showResult.setText(getContext().getString(R.string.content_null));
        }
        return view;
    }
}
