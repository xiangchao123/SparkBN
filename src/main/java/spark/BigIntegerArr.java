package spark;

public class BigIntegerArr {
    /**
     * 计算进位
     *
     * @param bit
     *            数组
     * @param pos
     *            用于判断是否是数组的最高位
     */
    private void carry(int[] bit, int pos) {
        int i, carray = 0;
        for (i = 0; i <= pos; i++)// 从0到pos逐位检查是否需要进位
        {
            bit[i] += carray;// 累加进位
            if (bit[i] <= 9) // 小于9不进位
            {
                carray = 0;
            } else if (bit[i] > 9 && i < pos)// 大于9，但不是最高位
            {
                carray = bit[i] / 10;// 保存进位值
                bit[i] = bit[i] % 10;// 得到该位的一位数
            } else if (bit[i] > 9 && i >= pos)// 大于9，且是最高位
            {
                while (bit[i] > 9)// 循环向前进位
                {
                    carray = bit[i] / 10;// 计算进位值
                    bit[i] = bit[i] % 10;// 当前的第一位数
                    i++;
                    bit[i] = carray;// 在下一位保存进位值
                }
            }
        }
    }

    /**
     * 大整数阶乘
     *
     * @param bigInteger
     *            所计算的大整数
     */
    public String bigFactorial(int bigInteger) {
        int pos = 0;//
        int digit;// 数据长度
        int a, b;
        int m = 0;// 统计输出位数
        int n = 0;// 统计输出行数
        double sum = 0;// 阶乘位数
        for (a = 1; a <= bigInteger; a++)// 计算阶乘位数
        {
            sum += Math.log10(a);
        }
        digit = (int) sum + 1;// 数据长度

        int[] fact = new int[digit];// 初始化一个数组
        fact[0] = 1;// 设个位为 1

        for (a = 2; a <= bigInteger; a++)// 将2^bigInteger逐个与原来的积相乘
        {
            for (b = digit - 1; b >= 0; b--)// 查找最高位{}
            {
                if (fact[b] != 0) {
                    pos = b;// 记录最高位
                    break;
                }
            }

            for (b = 0; b <= pos; b++) {
                fact[b] *= a;// 每一位与i乘
            }
            carry(fact, pos);
        }

        for (b = digit - 1; b >= 0; b--) {
            if (fact[b] != 0) {
                pos = b;// 记录最高位
                break;
            }
        }
        // System.out.println(bigInteger +"阶乘结果为：");
        // for(a = pos ; a >= 0 ; a --)//输出计算结果
        // {
        // System.out.print(fact[a]);
        // m++;
        // if(m % 5 == 0)
        // {
        // System.out.print(" ");
        // }
        // if(40 == m )
        // {
        // System.out.println("");
        // m = 0 ;
        // n ++;
        // if(10 == n )
        // {
        // System.out.print("\n");
        // n = 0;
        // }
        // }
        // }
        // System.out.println("\n"+"阶乘共有: "+(pos+1)+" 位");
        return reverseArray(fact);
    }

    private String reverseArray(int[] fact) {
        StringBuilder sb = new StringBuilder();
        for (int i = fact.length - 1; i >= 0; i--) {
            sb.append(String.valueOf(fact[i]));
        }
        return sb.toString();

    }

//    private Double toLogBig(String intStr) {
//        // String intStr = "";
//        String deciStr = "";
//        int len = intStr.indexOf(".");
//        if (len == 0 || len == intStr.length() - 1) {
//            return 0.0;
//        } else if (len == -1) {
//            deciStr = "0";
//        } else {
//            deciStr = intStr.substring(len + 1, intStr.length());
//            intStr = intStr.substring(1, len);
//        }
//        len = intStr.length();
//        while (len > 1) {
//            if (intStr.startsWith("0")) {
//                intStr = intStr.substring(1, len);
//                len -= 1;
//            } else {
//                break;
//            }
//        }
//        len = deciStr.length();
//        while (len > 1) {
//            if (deciStr.endsWith("0")) {
//                deciStr = deciStr.substring(0, len - 1);
//                len -= 1;
//            } else {
//                break;
//            }
//        }
//        double ret;
//        len = intStr.length();
//        if (len < 16) {
//            if (intStr.length() > 4 || deciStr.length() < 9) {
//                intStr += "." + deciStr;
//                ret = Double.valueOf(intStr);
//                if (ret == 0) {
//                    return 0.0;
//                }
//                ret = Math.log10(ret);
//            } else {
//                len = deciStr.length();
//                intStr = intStr + deciStr.substring(0, len - 8);
//                deciStr = deciStr.substring(len - 8, len);
//                ret = 8 - len;
//                len = intStr.length();
//                while (len > 1) {
//                    if (intStr.startsWith("0")) {
//                        intStr = intStr.substring(1, intStr.length());
//                    } else {
//                        break;
//                    }
//                }
//                len = intStr.length();
//                if (len > 9) {
//                    len -= 8;
//                    deciStr = intStr.substring(8, len + 8) + deciStr;
//                    intStr = intStr.substring(0, 8);
//                    ret += len;
//                }
//                intStr += "." + deciStr;
//                ret += Math.log10(Double.valueOf(intStr));
//            }
//        } else {
//            len = intStr.length();
//            deciStr = intStr.substring(9, len) + deciStr;
//            intStr = intStr.substring(0, 9) + "." + deciStr;
//            len -= 9;
//            ret = Math.log10(Double.valueOf(intStr)) + Double.valueOf(len);
//        }
//        return ret;
//    }
	public static void main(String[] args) {
        BigIntegerArr bi = new BigIntegerArr();
        System.out.println(bi.bigFactorial(5));
//        System.out.println(bi.toLogBig("120"));
        System.out.println(Integer.toBinaryString(2));
    }



}
