package cn.rrg.rdv.javabean;

/**
 * 组织命令与其名字对应的实体!
 *
 * @author DXL
 */
public class EasyCMDEntry {
    //命令标志
    private String cmdName;
    //命令内容
    private String command;

    public String getCmdName() {
        return cmdName;
    }

    public EasyCMDEntry setCmdName(String cmdName) {
        this.cmdName = cmdName;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public EasyCMDEntry setCommand(String command) {
        this.command = command;
        return this;
    }
}
