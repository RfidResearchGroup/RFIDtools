package cn.rfidresearchgroup.chameleon.defined;

public interface BasicTypesCallback {
    //布尔型
    interface BoolType {
        void onBool(boolean b);

        void onBools(boolean[] bs);
    }

    class BoolTypeEntry implements BoolType {
        @Override
        public void onBool(boolean b) {

        }

        @Override
        public void onBools(boolean[] bs) {

        }
    }

    //字符型
    interface CharType {
        void onChar(char c);

        void onChars(char[] cs);
    }

    class CharTypeEntry implements CharType {
        @Override
        public void onChar(char c) {

        }

        @Override
        public void onChars(char[] cs) {

        }
    }

    //字节型
    interface ByteType {
        void onByte(byte b);

        void onBytes(byte[] bs);
    }

    class ByteTypeEntry implements ByteType {
        @Override
        public void onByte(byte b) {

        }

        @Override
        public void onBytes(byte[] bs) {

        }
    }

    //短整型
    interface ShortType {
        void onShort(short s);

        void onShorts(short[] ss);
    }

    class ShortTypeEntry implements ShortType {
        @Override
        public void onShort(short s) {

        }

        @Override
        public void onShorts(short[] ss) {

        }
    }

    //整形
    interface IntegerType {
        void onInt(int i);

        void onInts(int[] is);
    }

    class IntegerTypeEntry implements IntegerType {
        @Override
        public void onInt(int i) {

        }

        @Override
        public void onInts(int[] is) {

        }
    }

    //长整型
    interface LongType {
        void onLong(long l);

        void onLongs(long[] ls);
    }

    class LongTypeEntry implements LongType {
        @Override
        public void onLong(long l) {

        }

        @Override
        public void onLongs(long[] ls) {

        }
    }

    //单精度浮点
    interface FloatType {
        void onFloat(float f);

        void onFloats(float[] fs);
    }

    class FloatTypeEntry implements FloatType {
        @Override
        public void onFloat(float f) {

        }

        @Override
        public void onFloats(float[] fs) {

        }
    }

    //双精度浮点
    interface DoubleType {
        void onDouble(double d);

        void onDoubles(double[] ds);
    }

    class DoubleTypeEntry implements DoubleType {

        @Override
        public void onDouble(double d) {

        }

        @Override
        public void onDoubles(double[] ds) {

        }
    }

    //字符串型!
    interface StringType {
        void onString(String str);

        void onStrings(String[] str);
    }

    class StringTypeEntry implements StringType {

        @Override
        public void onString(String str) {

        }

        @Override
        public void onStrings(String[] str) {

        }
    }

    //对象类型!
    interface ObjectType {
        void onObject(Object obj);

        void onObjects(Object[] obj);
    }

    class ObjectTypeEntry implements ObjectType {
        @Override
        public void onObject(Object obj) {

        }

        @Override
        public void onObjects(Object[] obj) {

        }
    }
}
