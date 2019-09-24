package cn.rrg.rdv.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import cn.rrg.rdv.javabean.EasyCMDEntry;
import cn.dxl.common.util.FileUtil;

/*
 * 动态按钮的操作工具!
 * */
public class EasyBtnUtil {
    private File cmdXmlFile = new File(Paths.PM3_CMD_FILE);

    /*
     * 可以获得组元素个数
     * */
    public int getGroupCount() {
        try {
            JSONObject jsonObject = JSONObject.parseObject(new String(FileUtil.readBytes(cmdXmlFile)));
            //得到一个组对象，这个数组种存放了一个键值对的数组，也就是一个二维数组!
            JSONArray array = jsonObject.getJSONArray("group");
            return array.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /*
     * 获得组对应的按钮元素个数
     * */
    public int getButtonCount(int group) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(new String(FileUtil.readBytes(cmdXmlFile)));
            //得到一个组对象，这个数组种存放了一个键值对的数组，也就是一个二维数组!
            JSONArray array = jsonObject.getJSONArray("group");
            JSONArray innerArr = array.getJSONArray(group);
            return innerArr.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /*
     * 得到某个按钮
     * */
    public EasyCMDEntry getButton(int group, int buttonPosition) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(new String(FileUtil.readBytes(cmdXmlFile)));
            //得到一个组对象，这个数组种存放了一个键值对的数组，也就是一个二维数组!
            JSONArray array = jsonObject.getJSONArray("group");
            JSONArray innerArr = array.getJSONArray(group);
            EasyCMDEntry entry = new EasyCMDEntry();
            JSONObject jsonObjectInner = innerArr.getJSONObject(buttonPosition);
            entry.setCmdName(jsonObjectInner.getString("name"));
            entry.setCommand(jsonObjectInner.getString("cmd"));
            return entry;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * 得到某个组的所有按钮!
     * */
    public List<EasyCMDEntry> getButtons(int group) {
        List<EasyCMDEntry> ret = new ArrayList<>();
        for (int i = 1; i < getButtonCount(group); i++) {
            EasyCMDEntry entry = getButton(group, i);
            ret.add(entry);
        }
        return ret;
    }

    /*
     * 删除某个组
     * */
    public boolean deteleGroup(int group) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(new String(FileUtil.readBytes(cmdXmlFile)));
            //得到一个组对象，这个数组种存放了一个键值对的数组，也就是一个二维数组!
            JSONArray array = jsonObject.getJSONArray("group");
            array.remove(group);
            JSONWriter writer = new JSONWriter(new FileWriter(cmdXmlFile));
            writer.writeObject(jsonObject);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * 删除某个按钮
     * */
    public boolean deleteButton(int group, int button) {
        try {
            button += 1;
            JSONObject jsonObject = JSONObject.parseObject(new String(FileUtil.readBytes(cmdXmlFile)));
            //得到一个组对象，这个数组种存放了一个键值对的数组，也就是一个二维数组!
            JSONArray array = jsonObject.getJSONArray("group");
            JSONArray innerArr = array.getJSONArray(group);
            innerArr.remove(button);
            JSONWriter writer = new JSONWriter(new FileWriter(cmdXmlFile));
            writer.writeObject(jsonObject);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * 插入某个组!
     * */
    public boolean inertGroup(String name) {
        try {
            //根数组对象
            JSONObject jsonObject = JSONObject.parseObject(new String(FileUtil.readBytes(cmdXmlFile)));
            //得到一个组对象，这个数组种存放了一个键值对的数组，也就是一个二维数组!
            JSONArray array = jsonObject.getJSONArray("group");
            //建立一个新的组，并且放入一个组名键值对元素!
            JSONArray newGroup = new JSONArray();
            JSONObject newObj = new JSONObject();
            newObj.put("name", name);
            newGroup.add(newObj);
            //将我们新建立的组与之放置的组参数对象写入到本地文件中!
            array.add(newGroup);
            JSONWriter writer = new JSONWriter(new FileWriter(cmdXmlFile));
            writer.writeObject(jsonObject);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * 插入某个按钮!
     * */
    public boolean inertButton(int group, String name, String cmd) {
        try {
            //根数组对象
            JSONObject jsonObject = JSONObject.parseObject(new String(FileUtil.readBytes(cmdXmlFile)));
            //得到一个组对象，这个数组种存放了一个键值对的数组，也就是一个二维数组!
            JSONArray array = jsonObject.getJSONArray("group");
            //建立一个新的组，并且放入一个组名键值对元素!
            JSONArray innerArr = array.getJSONArray(group);
            JSONObject newObj = new JSONObject();
            newObj.put("name", name);
            newObj.put("cmd", cmd);
            //将我们新建立的btn放置进组对象当中!
            innerArr.add(newObj);
            JSONWriter writer = new JSONWriter(new FileWriter(cmdXmlFile));
            writer.writeObject(jsonObject);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * 更新某个组的信息!
     * */
    public boolean updateGroup(int group, String name) {
        try {
            //根数组对象
            JSONObject jsonObject = JSONObject.parseObject(new String(FileUtil.readBytes(cmdXmlFile)));
            //得到一个组对象，这个数组种存放了一个键值对的数组，也就是一个二维数组!
            JSONArray array = jsonObject.getJSONArray("group");
            //建立一个新的组，并且放入一个组名键值对元素!
            JSONArray innerArr = array.getJSONArray(group);
            JSONObject innerObj = innerArr.getJSONObject(0);
            innerObj.put("name", name);
            //将我们新建立的btn放置进组对象当中!
            JSONWriter writer = new JSONWriter(new FileWriter(cmdXmlFile));
            writer.writeObject(jsonObject);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * 更新某个按钮的信息!
     * */
    public boolean updateButton(int group, int button, String name, String cmd) {
        try {
            button += 1;
            //根数组对象
            JSONObject jsonObject = JSONObject.parseObject(new String(FileUtil.readBytes(cmdXmlFile)));
            //得到一个组对象，这个数组种存放了一个键值对的数组，也就是一个二维数组!
            JSONArray array = jsonObject.getJSONArray("group");
            //建立一个新的组，并且放入一个组名键值对元素!
            JSONArray innerArr = array.getJSONArray(group);
            JSONObject innerObj = innerArr.getJSONObject(button);
            innerObj.put("name", name);
            innerObj.put("cmd", cmd);
            //将我们新建立的btn放置进组对象当中!
            JSONWriter writer = new JSONWriter(new FileWriter(cmdXmlFile));
            writer.writeObject(jsonObject);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
