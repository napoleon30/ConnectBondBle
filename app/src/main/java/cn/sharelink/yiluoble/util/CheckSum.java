package cn.sharelink.yiluoble.util;

import static cn.sharelink.yiluoble.util.ItonAdecimalConver.algorismToHEXString;
import static cn.sharelink.yiluoble.util.ItonAdecimalConver.binaryToAlgorism;

/**
 * Created by WangLei on 2017/8/14.
 */

public class CheckSum {

    /**
     * 根据输入的十六进制数据，计算校验位
     */
    public static String checkSum(String string) {
        /**
         * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF,
         * 0xD9}
         * @param src
         * @return byte[]
         */
        String results = null;
        String pingjie = null;
        if (string == null) {
            results = "00";
        } else if (string != null) {
            byte[] bytes = new byte[string.length() / 2]; //数组长度为result.length()/2
            int[] ints = new int[string.length() / 2];
            byte[] tmp = string.getBytes();
            int sum = 0;
            for (int i = 0; i < string.length() / 2; i++) {
                bytes[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
                ints[i] = bytes[i];
                if (ints[i] < 0) {
                    ints[i] += 256;
                }
                sum += ints[i];
//                Log.e("AAAAA", ints[i] + "");
//                Log.e("sum", sum + "");
            }
            int res = 255 - sum + 1; // 十进制计算
//            Log.e("res", res + "");

            if (res > 0) { //如果为正，则校验码为其十六进制数
                results = Integer.toHexString(res);//十进制int转十六进制字符串
                results = results.substring(results.length() - 2);
            } else if (res < 0) {
                String tenToBinary = Integer.toBinaryString(-res);//十进制转二进制字符串
                if (tenToBinary.length() >= 8) {
                    String substr = tenToBinary.substring(tenToBinary.length() - 8);
//                    Log.e("string1", substr);
                    byte[] bytes1 = substr.getBytes();
//                    Log.e("bytes[i]", Arrays.toString(bytes1));
                    for (int i = 0; i < bytes1.length; i++) {
                        if (bytes1[i] == 49) { //49表示1
                            bytes1[i] = 48; //48表示0
//                            Log.e("bytes[i]", bytes1[i] + "");
                        } else if (bytes1[i] == 48) {
                            bytes1[i] = 49;
//                            Log.e("bytes[i]", bytes1[i] + "");
                        }
                    }
                    String sub = new String(bytes1);
//                    Log.e("sub", sub);
                    int in = Integer.parseInt(sub, 2);
                    in = in + 1;
//                    Log.e("in", in + "");
                    results = algorismToHEXString(in);//十进制int转十六进制字符串

                } else if (tenToBinary.length() < 8) {
                    if (tenToBinary.length() == 1) {
                        pingjie = "1000000" + tenToBinary;
                    } else if (tenToBinary.length() == 2) {
                        pingjie = "100000" + tenToBinary;
                    } else if (tenToBinary.length() == 3) {
                        pingjie = "10000" + tenToBinary;
                    } else if (tenToBinary.length() == 4) {
                        pingjie = "1000" + tenToBinary;
                    } else if (tenToBinary.length() == 5) {
                        pingjie = "100" + tenToBinary;
                    } else if (tenToBinary.length() == 6) {
                        pingjie = "10" + tenToBinary;
                    } else if (tenToBinary.length() == 7) {
                        pingjie = "1" + tenToBinary;
                    }
//                    Log.e("pingjie", pingjie);
                    int BinaryToTen = binaryToAlgorism(pingjie); //二进制字符串转十进制int
                    results = algorismToHEXString(BinaryToTen);//十进制int转十六进制字符串
                }

            } else if (res == 0) {
                results = "00";
            }
        }
        return results;
    }

    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0}))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

}
